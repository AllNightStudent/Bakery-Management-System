package com.swp.controller;

import com.swp.entity.*;
import com.swp.repository.OrderRepository;
import com.swp.repository.ProductVariantRepository;
import com.swp.repository.PromotionRepository;
import com.swp.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductVariantRepository productVariantRepository;

    //Hùng thêm
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/checkout")
    public String checkout(@RequestParam(value = "fromCart", required = false) Boolean fromCart,
                           HttpSession session, Model model) {
        UserEntity currentUser = userService.getCurrentUser();

        // Nếu đi từ giỏ hàng, xóa trạng thái "mua ngay" trong session
        if (Boolean.TRUE.equals(fromCart)) {
            session.removeAttribute("buyNowVariantId");
            session.removeAttribute("buyNowQuantity");
        }

        // Kiểm tra xem có phải "mua ngay" không
        Long buyNowVariantId = (Long) session.getAttribute("buyNowVariantId");
        Integer buyNowQuantity = (Integer) session.getAttribute("buyNowQuantity");

        List<CartItemEntity> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;   // tổng tiền hàng, chưa trừ voucher

        if (buyNowVariantId != null && buyNowQuantity != null) {
            // Trường hợp "Mua ngay"
            ProductVariantEntity variant = productVariantRepository.findById(buyNowVariantId)
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));

            // Tạo CartItemEntity tạm để hiển thị + tính tiền
            CartItemEntity tempItem = new CartItemEntity();
            tempItem.setProductVariantId(variant);
            tempItem.setQuantity(buyNowQuantity);

            items.add(tempItem);

            subtotal = variant.getPrice().multiply(BigDecimal.valueOf(buyNowQuantity));

            model.addAttribute("isBuyNow", true);
        } else {
            // Trường hợp thanh toán từ giỏ hàng
            CartEntity cart = cartService.findCartByUser(currentUser);
            items = cartItemService.findAllByCart(cart);

            if (items.isEmpty()) {
                return "redirect:/cart";
            }

            // Tính tổng tiền hàng (subtotal)
            for (CartItemEntity item : items) {
                BigDecimal itemPrice = item.getProductVariantId().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemPrice);
            }

            model.addAttribute("isBuyNow", false);
        }

        // ================== ÁP DỤNG VOUCHER Ở ĐÂY ==================
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        BigDecimal finalTotal = subtotal;

        // Lấy voucher đã chọn từ session (id)
        Long selectedVoucherId = (Long) session.getAttribute("selectedVoucherId");
        String voucherCode = (String) session.getAttribute("selectedVoucherCode");

        if (selectedVoucherId != null) {
            Promotion voucher = promotionRepository.findById(selectedVoucherId)
                    .orElse(null);
            if (voucher != null) {
                // Dùng PromotionService để tính số tiền giảm, nếu đủ điều kiện
                voucherDiscount = promotionService.calculateDiscountForItems(voucher, items);

                if (voucherDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    finalTotal = subtotal.subtract(voucherDiscount);
                    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                        finalTotal = BigDecimal.ZERO;
                    }
                }
            }
        }

        // ================== ĐẨY DỮ LIỆU SANG VIEW ==================
        model.addAttribute("cartItems", items);
        model.addAttribute("subtotal", subtotal);               // tổng trước khi giảm
        model.addAttribute("voucherDiscount", voucherDiscount); // tiền giảm
        model.addAttribute("totalAmount", finalTotal);          // tổng sau khi giảm
        model.addAttribute("user", currentUser);

        // Để fill vào ô input mã voucher
        model.addAttribute("voucherCode", voucherCode);

        return "checkout";
    }


    @PostMapping("/create")
    public String createOrder(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam("customerAddress") String customerAddress,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session,
            Model model) {

        try {
            UserEntity currentUser = userService.getCurrentUser();
            OrderEntity order;

            // Kiểm tra xem có phải "mua ngay" không
            Long buyNowVariantId = (Long) session.getAttribute("buyNowVariantId");
            Integer buyNowQuantity = (Integer) session.getAttribute("buyNowQuantity");

            // ==== 1. TẠO ORDER NHƯ CŨ ====
            if (buyNowVariantId != null && buyNowQuantity != null) {
                // Trường hợp "Mua ngay" - tạo order trực tiếp
                ProductVariantEntity variant = productVariantRepository.findById(buyNowVariantId)
                        .orElseThrow(() -> new RuntimeException("Product variant not found"));

                order = orderService.createDirectOrder(
                        currentUser, variant, buyNowQuantity,
                        customerName, customerPhone, customerAddress, note);

                // Xóa session sau khi tạo order
                session.removeAttribute("buyNowVariantId");
                session.removeAttribute("buyNowQuantity");
            } else {
                // Trường hợp thanh toán từ giỏ hàng
                CartEntity cart = cartService.findCartByUser(currentUser);
                order = orderService.createOrderFromCart(
                        cart, customerName, customerPhone, customerAddress, note);
            }

            // ==== 2. ÁP DỤNG VOUCHER VÀ CẬP NHẬT LẠI TOTAL TRONG ORDER ====

            // Lấy voucher đã chọn từ session
            Long selectedVoucherId = (Long) session.getAttribute("selectedVoucherId");
            if (selectedVoucherId != null) {
                Promotion voucher = promotionRepository.findById(selectedVoucherId)
                        .orElse(null);

                if (voucher != null && order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {

                    // Tính subtotal lại từ orderItems
                    BigDecimal subtotal = BigDecimal.ZERO;
                    List<CartItemEntity> fakeCartItems = new ArrayList<>();

                    for (OrderItemEntity oi : order.getOrderItems()) {
                        ProductVariantEntity variant = oi.getProductVariant();
                        int quantity = oi.getQuantity();

                        // subtotal = sum(variant.price * quantity)
                        BigDecimal lineTotal = variant.getPrice()
                                .multiply(BigDecimal.valueOf(quantity));
                        subtotal = subtotal.add(lineTotal);

                        // Tạo CartItemEntity giả để tái sử dụng hàm calculateDiscountForItems(...)
                        CartItemEntity ci = new CartItemEntity();
                        ci.setProductVariantId(variant);
                        ci.setQuantity(quantity);
                        fakeCartItems.add(ci);
                    }

                    // Tính tiền giảm bằng service hiện tại của bạn
                    BigDecimal voucherDiscount = promotionService.calculateDiscountForItems(voucher, fakeCartItems);
                    if (voucherDiscount.compareTo(BigDecimal.ZERO) < 0) {
                        voucherDiscount = BigDecimal.ZERO;
                    }

                    BigDecimal finalTotal = subtotal.subtract(voucherDiscount);
                    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                        finalTotal = BigDecimal.ZERO;
                    }

                    // Ghi lại totalAmount đã trừ voucher vào order
                    order.setTotalAmount(finalTotal);


                    orderRepository.save(order); // cần có hàm này trong OrderService
                }
            }


            return "redirect:/order/payment?orderId=" + order.getOrderId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        }
    }


    @GetMapping("/payment")
    public String payment(@RequestParam("orderId") Long orderId, Model model) {
        OrderEntity order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        //Của Hùng
        BigDecimal subtotal = BigDecimal.ZERO;
        if (order.getOrderItems() != null) {
            for (OrderItemEntity item : order.getOrderItems()) {
                BigDecimal price = item.getProductVariant().getPrice();
                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(lineTotal);
            }
        }

        // totalAmount là số tiền cuối cùng đã lưu trong order (đã trừ voucher nếu có)
        BigDecimal totalAmount = order.getTotalAmount();

        // Tiền giảm (nếu >0 thì coi là giảm từ voucher)
        BigDecimal discount = subtotal.subtract(totalAmount);
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO; // phòng trường hợp dữ liệu lệch
        }

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        return "payment";
    }

    @GetMapping("/list")
    public String viewOrders(Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        List<OrderEntity> orders = orderService.findByUser(currentUser);

        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/detail/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model) {
        OrderEntity order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        model.addAttribute("orderItems", order.getOrderItems());
        return "order-detail";
    }
}