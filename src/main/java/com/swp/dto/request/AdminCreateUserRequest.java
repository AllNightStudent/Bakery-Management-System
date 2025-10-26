package com.swp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Size(min = 5, message = "Mật khẩu phải ít nhất 5 ký tự")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String phone;

    @NotNull(message = "Vai trò không được để trống")
    private Long roleId; // ADMIN, USER, MODERATOR,...
}
