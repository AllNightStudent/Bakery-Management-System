package com.swp.repository;


import com.swp.entity.OrderItemEntity;
import com.swp.entity.ProductEntity;
import com.swp.entity.Review;
import com.swp.entity.UserEntity;
import com.swp.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByProductAndStatus(ProductEntity product, ReviewStatus status);

    Optional<Review> findByOrderItemAndUser(OrderItemEntity orderItem, UserEntity user);

    boolean existsByOrderItemAndUser(OrderItemEntity oi, UserEntity user);
}

