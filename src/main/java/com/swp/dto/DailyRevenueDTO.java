package com.swp.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DailyRevenueDTO {
    private LocalDate date;
    private Long countOrder;
    private BigDecimal revenue;
}
