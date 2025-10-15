package com.swp.repository;

import com.swp.entity.OrderEntity;
import com.swp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserOrderByOrderDateDesc(UserEntity user);

    List<OrderEntity> findByStatus(String status);
}

