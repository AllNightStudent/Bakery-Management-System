package com.swp.controller;

import com.swp.entity.*;
import com.swp.repository.ProductVariantRepository;
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

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        UserEntity currentUser = userService.getCurrentUser();

        // Kiểm tra xem có phải "mua ngay" không
        Long buyNowVariantId = (Long) session.getAttribute("buyNowVariantId");
        Integer buyNowQuantity = (Integer) session.getAttribute("buyNowQuantity");

        List<CartItemEntity> items;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (buyNowVariantId != null && buyNowQuantity != null) {
            // Trường hợp "Mua ngay"
            ProductVariantEntity variant = productVariantRepository.findById(buyNowVariantId)
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));

            // Tạo CartItemEntity tạm để hiển thị
            CartItemEntity tempItem = new CartItemEntity();
            tempItem.setProductVariantId(variant);
            tempItem.setQuantity(buyNowQuantity);

            items = new ArrayList<>();
            items.add(tempItem);

            totalAmount = variant.getPrice().multiply(BigDecimal.valueOf(buyNowQuantity));

            model.addAttribute("isBuyNow", true);
        } else {
            // Trường hợp thanh toán từ giỏ hàng
            CartEntity cart = cartService.findCartByUser(currentUser);
            items = cartItemService.findAllByCart(cart);

            if (items.isEmpty()) {
                return "redirect:/cart";
            }

            // Tính tổng tiền
            for (CartItemEntity item : items) {
                BigDecimal itemPrice = item.getProductVariantId().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(itemPrice);
            }

            model.addAttribute("isBuyNow", false);
        }

        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", currentUser);
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

            // Redirect đến trang thanh toán
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

