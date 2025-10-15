package com.swp.repository;

import com.swp.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
    boolean existsBySku(String sku);
    List<ProductVariantEntity> findByProductProductId(Long productId);

    // Search variants by SKU
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.sku LIKE %:sku%")
    List<ProductVariantEntity> findBySkuContaining(@Param("sku") String sku);
}


