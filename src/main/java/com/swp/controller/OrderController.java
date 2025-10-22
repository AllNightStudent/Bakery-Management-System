package com.swp.controller;

import com.swp.entity.*;
import com.swp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        CartEntity cart = cartService.findCartByUser(currentUser);
        List<CartItemEntity> items = cartItemService.findAllByCart(cart);

        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        // Tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemEntity item : items) {
            BigDecimal itemPrice = item.getProductVariantId().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemPrice);
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
            Model model) {

        try {
            UserEntity currentUser = userService.getCurrentUser();
            CartEntity cart = cartService.findCartByUser(currentUser);

            // Tạo Order từ Cart
            OrderEntity order = orderService.createOrderFromCart(
                    cart, customerName, customerPhone, customerAddress, note);

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