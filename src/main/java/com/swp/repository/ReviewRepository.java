package com.swp.repository;


import com.swp.entity.OrderItemEntity;
import com.swp.entity.ProductEntity;
import com.swp.entity.Review;
import com.swp.entity.UserEntity;
import com.swp.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
   SELECT r FROM Review r
   LEFT JOIN FETCH r.user
   LEFT JOIN FETCH r.product
   WHERE r.reviewId = :id
""")
    Optional<Review> findDetail(@Param("id") Long id);

    long countByProductAndStatus(ProductEntity product, ReviewStatus status);

    Optional<Review> findByOrderItemAndUser(OrderItemEntity orderItem, UserEntity user);

    boolean existsByOrderItemAndUser(OrderItemEntity oi, UserEntity user);

    @Query("""
              SELECT r FROM Review r
              JOIN r.product p
              JOIN r.user u
              WHERE (:status IS NULL OR r.status = :status)
                AND (:productId IS NULL OR p.productId = :productId)
                AND (:stars IS NULL OR r.rating = :stars)
                AND (:q IS NULL OR
                     lower(coalesce(r.title,''))        like lower(concat('%', :q, '%')) OR
                     lower(cast(r.content as string))   like lower(concat('%', :q, '%')) OR
                     lower(coalesce(u.email,''))        like lower(concat('%', :q, '%')) OR
                     lower(coalesce(p.name,''))         like lower(concat('%', :q, '%'))
                )
              ORDER BY r.createdAt DESC
            """)
    Page<Review> search(ReviewStatus status, Long productId, Integer stars, String q, Pageable pageable);


}

