package com.swp.controller.admin;

import com.swp.entity.OrderEntity;
import com.swp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {

    private final OrderService orderService;

    @GetMapping
    public String orderList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            ModelMap model) {

        // Validate page size
        if (size <= 0) size = 10;
        if (size > 100) size = 100; // Max 100 items per page
        
        // Get orders with pagination and filters
        Page<OrderEntity> orderPage = orderService.searchAndFilterOrders(
                search, status, page, size);

        // Add data to model
        model.put("orders", orderPage.getContent());
        model.put("currentPage", page);
        model.put("totalPages", orderPage.getTotalPages());
        model.put("totalElements", orderPage.getTotalElements());
        model.put("size", size);
        model.put("search", search);
        model.put("status", status);
        
        // Calculate statistics
        long totalOrders = orderService.countAll();
        long pendingCount = orderService.countByStatus("PENDING");
        long confirmedCount = orderService.countByStatus("CONFIRMED");
        long deliveredCount = orderService.countByStatus("COMPLETED");
        long cancelledCount = orderService.countByStatus("CANCELLED");
        
        model.put("totalOrders", totalOrders);
        model.put("pendingCount", pendingCount);
        model.put("confirmedCount", confirmedCount);
        model.put("deliveredCount", deliveredCount);
        model.put("cancelledCount", cancelledCount);

        return "admin/orders";
    }
    
    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long id, 
                                                     @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error");
        }
    }
}

