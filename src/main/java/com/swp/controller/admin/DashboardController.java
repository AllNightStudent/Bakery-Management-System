package com.swp.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    // Moved products listing to ProductAdminController. Keep only dashboard here.
}
