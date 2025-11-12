// ReviewReply.java
package com.swp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "review_replies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewReply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 2000)
    private String content;

    // ghi nhận admin thực hiện (tuỳ bạn, có thể dùng UserEntity admin)
    @Column(name = "admin_email", length = 200)
    private String adminEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
}
