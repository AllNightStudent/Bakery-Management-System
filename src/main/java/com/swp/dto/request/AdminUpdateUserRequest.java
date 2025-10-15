package com.swp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotNull(message = "ID người dùng không được để trống")
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String phone;

    private String password; // có thể bỏ trống nếu không muốn đổi mật khẩu

    @NotNull(message = "Vai trò không được để trống")
    private Long roleId;
}
