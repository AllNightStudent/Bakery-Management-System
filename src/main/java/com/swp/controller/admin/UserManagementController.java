package com.swp.controller.admin;

import com.swp.dto.request.AdminCreateUserRequest;
import com.swp.dto.request.AdminUpdateUserRequest;
import com.swp.entity.UserEntity;
import com.swp.service.AuthService;
import com.swp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public String userList(@RequestParam(required = false) Long roleId,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String search,
                           ModelMap model) {

        List<UserEntity> users = (roleId == null)
                ? userService.findAll()
                : userService.findByRole(roleId);

        if (users == null) users = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.toLowerCase().trim();
            users = users.stream()
                    .filter(user ->
                            (user.getName() != null && user.getName().toLowerCase().contains(keyword)) ||
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword))
                    )
                    .toList();
        }

        int totalUsers = users.size();
        int totalPages = (int) Math.ceil((double) totalUsers / size);
        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int start = page * size;
        int end = Math.min(start + size, totalUsers);
        List<UserEntity> pageUsers = users.subList(start, end);

        model.put("users", pageUsers);
        model.put("roles", userService.getAllRoles());
        model.put("currentPage", page);
        model.put("totalPages", totalPages);
        model.put("search", search);
        model.put("roleId", roleId);
        model.addAttribute("adminCreateUserRequest", new AdminCreateUserRequest());
        model.addAttribute("adminUpdateUserRequest", new AdminUpdateUserRequest());

        return "admin/users";
    }

    @PostMapping("/toggleStatus")
    public String toggleUserStatus(@RequestParam Long id) {
        userService.toggleStatus(id);
        return "redirect:/admin/user";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            boolean deleted = userService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok("deleted")
                    : ResponseEntity.badRequest().body("not_found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("error");
        }
    }

    @PostMapping("/create")
    public String createUserByAdmin(
            @Valid @ModelAttribute("adminCreateUserRequest") AdminCreateUserRequest request,
            BindingResult bindingResult,
            ModelMap model) {

        if (bindingResult.hasErrors()) {
            model.put("roles", userService.getAllRoles());
            model.put("users", userService.findAll());
            model.put("currentPage", 0);
            model.put("totalPages", 0);
            model.put("search", "");
            model.put("roleId", null);
            return "admin/users";
        }

        try {
            authService.createUserByAdmin(request);
            model.put("successMessage", "Tạo người dùng thành công!");
        } catch (Exception e) {
            model.put("errorMessage", e.getMessage());
        }

        return "redirect:/admin/user";
    }

    @PostMapping("/update")
    public String updateUserByAdmin(
            @Valid @ModelAttribute("adminUpdateUserRequest") AdminUpdateUserRequest request,
            BindingResult bindingResult,
            ModelMap model) {

        // Luôn load danh sách users và roles
        List<UserEntity> users = userService.findAll();
        model.put("users", users);
        model.put("roles", userService.getAllRoles());
        model.put("currentPage", 0);
        model.put("totalPages", (int) Math.ceil((double) users.size() / 10));
        model.put("search", "");
        model.put("roleId", null);

        // Nếu có lỗi validate, giữ dữ liệu và mở modal
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminUpdateUserRequest", request); // giữ dữ liệu
            model.addAttribute("showUpdateModal", true); // JS mở modal
            return "admin/users"; // ko redirect, trả về view luôn
        }

        try {
            authService.updateUserByAdmin(request);
            // Nếu thành công, redirect và hiển thị message
            return "redirect:/admin/user?successUpdate=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("adminUpdateUserRequest", request); // giữ dữ liệu
            model.addAttribute("showUpdateModal", true); // mở modal
            return "admin/users";
        }
    }





}
