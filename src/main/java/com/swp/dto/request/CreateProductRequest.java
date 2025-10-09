package com.swp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProductRequest {

    @NotBlank
    private String name;

    private String shortDescription;

    private String description;

    @NotNull
    private Long categoryId;

    private MultipartFile imageFile;

    @NotNull
    private java.util.List<VariantRequest> variants;

    @lombok.Data
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

