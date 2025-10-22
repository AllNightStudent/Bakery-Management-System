package com.swp.repository;

import com.swp.entity.OrderEntity;
import com.swp.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrder(OrderEntity order);

    Optional<PaymentEntity> findByVnpayTxnRef(String vnpayTxnRef);

    Optional<PaymentEntity> findByOrderOrderId(Long orderId);
}
