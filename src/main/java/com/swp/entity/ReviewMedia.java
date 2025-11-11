package com.swp.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name="review_media", indexes = @Index(name="idx_review_media_review", columnList="review_id"))
public class ReviewMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // đường dẫn public của file

    @Column(nullable = false, length = 10)
    private String mediaType; // "image"
}

