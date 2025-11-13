package com.swp.dto;

import com.swp.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PromotionResult {
    private Promotion promotion;     // null nếu không có khuyến mãi
    private BigDecimal subtotal;     // tổng tiền hàng (không ship)
    private BigDecimal discount;     // tiền giảm
    private BigDecimal finalTotal;   // subtotal - discount
}
