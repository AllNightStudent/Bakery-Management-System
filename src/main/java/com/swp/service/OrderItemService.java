package com.swp.service;

import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import com.swp.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public List<OrderItemEntity> findByOrder(OrderEntity order) {
        return orderItemRepository.findByOrder(order);
    }
}