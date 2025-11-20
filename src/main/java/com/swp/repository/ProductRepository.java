package com.swp.repository;

import com.swp.entity.CategoryEntity;
import com.swp.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsByName(String name);

    // Search by category
    @Query("SELECT p FROM ProductEntity p WHERE p.categoryId.name LIKE %:category%")
    Page<ProductEntity> findByCategoryNameContaining(@Param("category") String category, Pageable pageable);

    // Search by product name
    @Query("SELECT p FROM ProductEntity p WHERE p.name LIKE %:search% OR p.shortDescription LIKE %:search%")
    Page<ProductEntity> findByNameOrShortDescriptionContaining(@Param("search") String search, Pageable pageable);

    // Search by category and name
    @Query("SELECT p FROM ProductEntity p WHERE (p.categoryId.name LIKE %:category% OR :category = '') AND (p.name LIKE %:search% OR p.shortDescription LIKE %:search% OR :search = '')")
    Page<ProductEntity> findByCategoryAndName(@Param("category") String category, @Param("search") String search, Pageable pageable);

    // Find products by IDs
    @Query("SELECT p FROM ProductEntity p WHERE p.productId IN :ids")
    Page<ProductEntity> findProductsByIds(@Param("ids") List<Long> ids, Pageable pageable);

    //find all with variant
    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.variants WHERE p.active = TRUE")
    List<ProductEntity> findAllActiveWithVariants();

    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.variants WHERE p.productId = :id")
    ProductEntity findAllWithVariantsById(@Param("id") Long id);

    List<ProductEntity> findByActiveIsNull();

    boolean existsByCategoryId(CategoryEntity categoryId);
}