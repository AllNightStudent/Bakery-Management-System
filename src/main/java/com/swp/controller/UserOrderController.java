package com.swp.controller;

import com.swp.entity.OrderEntity;
import com.swp.entity.UserEntity;
import com.swp.service.OrderService;
import com.swp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/orders")
    public String listOrders(Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        List<OrderEntity> orders = orderService.findByUser(currentUser);

        model.addAttribute("orders", orders);
        return "user-order";
    }
}