package com.swp.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRevenueDTO {
    private Long productId;           // oi.product_id
    private String productName;       // MAX(oi.product_name)
    private BigDecimal revenue;  // SUM(oi.quantity * oi.price)
    private Long quantity;       // SUM(oi.quantity)
}

