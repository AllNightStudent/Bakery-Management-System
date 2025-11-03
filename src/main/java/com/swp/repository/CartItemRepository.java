package com.swp.repository;

import com.swp.entity.CartEntity;
import com.swp.entity.CartItemEntity;
import com.swp.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    CartItemEntity findByCartAndProductVariantId(CartEntity cart, ProductVariantEntity productVariant);
}