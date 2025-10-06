package com.swp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {
	private Long materialId;

	@NotBlank(message = "Tên nguyên liệu không được để trống")
	@Size(min = 2, max = 150, message = "Tên nguyên liệu phải từ 2-150 ký tự")
	private String name;

	@NotNull(message = "Số lượng không được để trống")
	@Min(value = 0, message = "Số lượng phải >= 0")
	private Integer stock;

	@NotBlank(message = "Đơn vị không được để trống")
	@Size(max = 50, message = "Đơn vị không quá 50 ký tự")
	private String unit;

	@NotNull(message = "Giá không được để trống")
	@DecimalMin(value = "0.00", inclusive = true, message = "Giá phải >= 0")
	private BigDecimal cost;

	private LocalDate expiryDate;
}


