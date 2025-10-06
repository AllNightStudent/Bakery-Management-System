package com.swp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "materials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "material_id")
	private Long materialId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer stock;

	@Column(nullable = false)
	private String unit;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal cost;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;
}


