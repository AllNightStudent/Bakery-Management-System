package com.swp.service;

import com.swp.dto.ProductRevenueDTO;
import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;


    public List<ProductRevenueDTO> getTop10ProductsByRevenue() {
        var rows = orderItemRepository.findTopProductsByRevenue(PageRequest.of(0, 10));
        return rows.stream()
                .map(r -> new ProductRevenueDTO(
                        ((Number) r[0]).longValue(),        // productId → Long
                        (String) r[1],                      // productName → String
                        (BigDecimal) r[2],                  // totalRevenue → BigDecimal
                        ((Number) r[3]).longValue()         // totalQuantity → Long
                ))
                .toList();
    }
    public int countOrders() {
        int year = YearMonth.now().getYear();
        int month = YearMonth.now().getMonthValue();
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        List<OrderEntity> orderList = orderRepository.findOrdersBetween(start, end);
        return orderList.size();
    }

    public BigDecimal totalRevenue() {
        int year = YearMonth.now().getYear();
        int month = YearMonth.now().getMonthValue();
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        List<OrderEntity> orderList = orderRepository.findOrdersBetween(start, end);
        BigDecimal total = BigDecimal.ZERO;
        for(OrderEntity order: orderList){
            total = total.add(order.getTotalAmount());
        }
        return total;
    }

    public Map<String, BigDecimal> calculateMonthlyRevenue(List<OrderEntity> allOrders) {
        DateTimeFormatter ym = DateTimeFormatter.ofPattern("yyyy-MM");
        return allOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getTotalAmount() != null)
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))   // <-- chỉ lấy COMPLETED
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().format(ym),
                        TreeMap::new,
                        Collectors.mapping(
                                OrderEntity::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    public List<OrderEntity> orderList() {
        return orderRepository.findAll();
    }
}

