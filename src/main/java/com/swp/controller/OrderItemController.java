package com.swp.controller;

import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import com.swp.service.OrderItemService;
import com.swp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderService orderService; // dùng để lấy OrderEntity theo ID
    private final OrderItemService orderItemService;

    /**
     * Xem chi tiết đơn hàng theo orderId
     * URL: /order/{orderId}/details
     */
    @GetMapping("/orders/{orderId}/details")
    public String viewOrderDetails(@PathVariable("orderId") Long orderId, Model model) {
        // Lấy Order
        OrderEntity order = orderService.findOrderById(orderId);
        if (order == null) {
            // xử lý nếu không tìm thấy order
            return "redirect:/orders"; // hoặc hiển thị trang lỗi
        }

        // Lấy danh sách OrderItem
        List<OrderItemEntity> items = orderItemService.findByOrder(order);

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalAmount", total);


        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "order-detail"; // Thymeleaf template order-details.html
    }
}