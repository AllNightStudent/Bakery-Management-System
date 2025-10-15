package com.swp.service;

import com.swp.entity.*;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemService cartItemService;

    @Transactional
    public OrderEntity createOrderFromCart(CartEntity cart, String customerName, String customerPhone,
                                           String customerAddress, String note) {
        List<CartItemEntity> cartItems = cartItemService.findAllByCart(cart);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemEntity cartItem : cartItems) {
            BigDecimal itemPrice = cartItem.getProductVariantId().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemPrice);
        }

        // Tạo Order
        OrderEntity order = OrderEntity.builder()
                .user(cart.getUser())
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .totalAmount(totalAmount)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .note(note)
                .build();

        order = orderRepository.save(order);

        // Tạo Order Items
        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (CartItemEntity cartItem : cartItems) {
            ProductVariantEntity variant = cartItem.getProductVariantId();
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(cartItem.getQuantity())
                    .price(variant.getPrice())
                    .productName(variant.getProduct().getName())
                    .build();
            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);

        return order;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Optional<OrderEntity> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            OrderEntity order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    public Optional<OrderEntity> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<OrderEntity> findByUser(UserEntity user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<OrderEntity> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public OrderEntity createDirectOrder(UserEntity user, ProductVariantEntity variant, int quantity,
                                         String customerName, String customerPhone,
                                         String customerAddress, String note) {
        // Tính tổng tiền
        BigDecimal totalAmount = variant.getPrice().multiply(BigDecimal.valueOf(quantity));

        // Tạo Order
        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .totalAmount(totalAmount)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .note(note)
                .build();

        order = orderRepository.save(order);

        // Tạo Order Item
        OrderItemEntity orderItem = OrderItemEntity.builder()
                .order(order)
                .productVariant(variant)
                .quantity(quantity)
                .price(variant.getPrice())
                .productName(variant.getProduct().getName())
                .build();

        orderItemRepository.save(orderItem);

        List<OrderItemEntity> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        return order;
    }
}

