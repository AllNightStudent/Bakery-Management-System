package com.swp.service;

import com.swp.entity.*;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.OrderRepository;
import com.swp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public OrderEntity findOrderById(Long orderId) {
        return orderRepository.findById(orderId).get();
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

    // Admin order management methods
    public Page<OrderEntity> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        return orderRepository.findAll(pageable);
    }

    public Page<OrderEntity> searchAndFilterOrders(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

        // Handle empty strings as null
        String searchTerm = (search != null && !search.trim().isEmpty())
                ? search.trim() : null;
        String filterStatus = (status != null && !status.trim().isEmpty()) ? status : null;

        return orderRepository.searchAndFilterOrders(filterStatus, searchTerm, pageable);
    }

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get statistics for dashboard
    public long countByStatus(String status) {
        return orderRepository.findByStatus(status).size();
    }

    public long countAll() {
        return orderRepository.count();
    }
}