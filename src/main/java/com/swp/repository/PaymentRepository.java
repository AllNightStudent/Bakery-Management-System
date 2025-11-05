package com.swp.repository;

import com.swp.entity.OrderEntity;
import com.swp.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrder(OrderEntity order);

    Optional<PaymentEntity> findByVnpayTxnRef(String vnpayTxnRef);

    Optional<PaymentEntity> findByOrderOrderId(Long orderId);

    // Admin transaction management methods
    Page<PaymentEntity> findByStatus(String status, Pageable pageable);

    Page<PaymentEntity> findByVnpayTxnRefContaining(String vnpayTxnRef, Pageable pageable);

    @Query("SELECT p FROM PaymentEntity p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:vnpayTxnRef IS NULL OR p.vnpayTxnRef LIKE %:vnpayTxnRef%)")
    Page<PaymentEntity> findByStatusAndVnpayTxnRef(
            @Param("status") String status,
            @Param("vnpayTxnRef") String vnpayTxnRef,
            Pageable pageable);

    List<PaymentEntity> findByStatus(String status);
}