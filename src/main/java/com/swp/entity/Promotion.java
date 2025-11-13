package com.swp.entity;

import com.swp.entity.enums.DiscountType;
import com.swp.entity.enums.ProductScopeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Có hoặc không, tuỳ bạn dùng mã giảm giá hay auto apply
    @Column(unique = true)
    private String code;

    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENT / AMOUNT

    // Nếu PERCENT = 10 nghĩa là 10%
    // Nếu AMOUNT = 20000 nghĩa là giảm 20k
    private BigDecimal discountValue;

    // Điều kiện theo giá trị đơn hàng
    private BigDecimal minOrderValue;   // có thể null
    private BigDecimal maxOrderValue;   // có thể null

    // Điều kiện theo tổng số lượng sản phẩm
    private Integer minTotalQuantity;   // có thể null

    // Điều kiện theo loại sản phẩm
    @Enumerated(EnumType.STRING)
    private ProductScopeType productScopeType; // ALL / CATEGORY / PRODUCT

    // Ví dụ ManyToMany với Category (điều chỉnh lại tên entity cho khớp)
    @ManyToMany
    @JoinTable(
            name = "promotion_categories",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categories;   // nếu dùng CATEGORY

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean active;
}
