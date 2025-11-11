package com.swp.dto;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating, // vẫn giữ cho hợp lệ 1..5
        String title,                             // không ràng buộc độ dài
        String content,                           // không ràng buộc độ dài
        Long orderItemId,
        Boolean anonymous
) {}


