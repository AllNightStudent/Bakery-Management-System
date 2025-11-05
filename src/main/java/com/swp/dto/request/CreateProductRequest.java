package com.swp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProductRequest {

    @NotBlank
    @Size(min = 2, max = 100, message = "Tên sản phẩm phải từ 2-100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả ngắn không được vượt quá 255 ký tự")
    private String shortDescription;

    @Size(max = 2000, message = "Mô tả chi tiết không được vượt quá 2000 ký tự")
    private String description;

    @NotNull
    private Long categoryId;

    private MultipartFile imageFile;

    @NotNull
    private java.util.List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        @NotBlank
        private String sku;

        @NotNull
        @Min(0)
        private Integer weight;

        @NotNull
        private BigDecimal price;

        @NotNull
        @Min(0)
        private Integer stock;

        private LocalDate expiryDate;
    }
}

