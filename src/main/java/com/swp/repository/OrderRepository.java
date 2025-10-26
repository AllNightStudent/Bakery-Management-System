package com.swp.repository;

import com.swp.entity.OrderEntity;
import com.swp.entity.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserOrderByOrderDateDesc(UserEntity user);


    List<OrderEntity> findByStatus(String status);

    @Query("SELECT o FROM OrderEntity o WHERE o.orderDate > :start AND o.orderDate < :end")
    List<OrderEntity> findOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT o FROM OrderEntity o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:search IS NULL OR " +
            "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.customerAddress) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OrderEntity> searchAndFilterOrders(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

}
