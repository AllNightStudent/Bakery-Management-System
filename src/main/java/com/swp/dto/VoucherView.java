package com.swp.dto;


import com.swp.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VoucherView {
    private Promotion promotion;
    private boolean eligible;           // true = đơn đủ điều kiện
    private BigDecimal discountAmount;  // tiền sẽ giảm nếu áp dụng
}

