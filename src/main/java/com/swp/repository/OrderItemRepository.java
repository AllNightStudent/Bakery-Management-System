package com.swp.repository;

import com.swp.dto.ProductRevenueDTO;
import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrder(OrderEntity order);

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

    @Query(value = """
        SELECT 
            DATE(o.order_date) AS d,
            COUNT(DISTINCT o.order_id) AS cnt,
            COALESCE(SUM(oi.quantity * oi.price), 0) AS rev
        FROM orders o
        LEFT JOIN order_items oi ON oi.order_id = o.order_id
        WHERE o.order_date >= :from AND o.order_date < :to
        GROUP BY DATE(o.order_date)
        ORDER BY DATE(o.order_date)
        """, nativeQuery = true)
    List<Object[]> aggregateDailyRevenue(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}
