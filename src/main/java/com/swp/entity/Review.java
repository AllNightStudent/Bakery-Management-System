package com.swp.entity;


import com.swp.entity.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "reviews",
        indexes = {
                @Index(name="idx_reviews_product_status_created", columnList = "product_id,status,created_at"),
                @Index(name="idx_reviews_user", columnList = "user_id")
        })
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Ràng buộc vào item đã mua để xác thực verified
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_item_id")
    private OrderItemEntity orderItem; // có thể null nếu cho phép review không-verified

    @Column(nullable = false) private Integer rating; // 1..5
    @Column(length = 150) private String title;
    @Lob @Column(nullable = false) private String content;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;


    @CreationTimestamp @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at")
    private LocalDateTime updatedAt;
}

