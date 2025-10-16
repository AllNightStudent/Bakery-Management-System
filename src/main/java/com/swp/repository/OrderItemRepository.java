package com.swp.repository;

import com.swp.dto.ProductRevenueDTO;
import com.swp.entity.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    @Query(value = """
        SELECT 
            oi.product_id                                AS productId,
            MAX(oi.product_name)                         AS productName,
            SUM(oi.quantity * oi.price)                  AS totalRevenue,
            SUM(oi.quantity)                             AS totalQuantity
        FROM order_items oi
        GROUP BY oi.product_id
        ORDER BY totalRevenue DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT oi.product_id)
        FROM order_items oi
        """,
            nativeQuery = true)
    List<Object[]> findTopProductsByRevenue(Pageable pageable);


}
