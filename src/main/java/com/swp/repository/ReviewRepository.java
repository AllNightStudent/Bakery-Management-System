package com.swp.repository;

import com.swp.entity.OrderItemEntity;
import com.swp.entity.ProductEntity;
import com.swp.entity.Review;
import com.swp.entity.UserEntity;
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

    Optional<Review> findByOrderItemAndUser(OrderItemEntity orderItem, UserEntity user);

    boolean existsByOrderItemAndUser(OrderItemEntity oi, UserEntity user);

    @Query("""
        SELECT r FROM Review r
        JOIN r.product p
        JOIN r.user u
        WHERE (:stars IS NULL OR r.rating = :stars)
          AND (:keyword IS NULL OR
               lower(coalesce(r.title, ''))  LIKE lower(concat('%', :keyword, '%')) OR
               lower(r.content)              LIKE lower(concat('%', :keyword, '%')) OR
               lower(coalesce(u.email, ''))  LIKE lower(concat('%', :keyword, '%')) OR
               lower(coalesce(p.name, ''))   LIKE lower(concat('%', :keyword, '%'))
          )
        ORDER BY r.createdAt DESC
        """)
    Page<Review> search(@Param("stars") Integer stars,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
