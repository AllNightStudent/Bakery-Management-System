package com.swp.service;

import com.swp.dto.DailyRevenueDTO;
import com.swp.dto.ProductRevenueDTO;
import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<DailyRevenueDTO> getDailyRevenue(LocalDate start, LocalDate end) {
        LocalDate effectiveStart;
        LocalDate effectiveEnd;

        if (start == null || end == null) {
            // Mặc định: hôm nay
            LocalDate today = LocalDate.now();
            effectiveStart = today;
            effectiveEnd   = today;
        } else {
            // Dùng đúng khoảng người dùng chọn
            effectiveStart = start;
            effectiveEnd   = end;
        }

        // [from, toExclusive)
        LocalDateTime from = effectiveStart.atStartOfDay();
        LocalDateTime toExclusive = effectiveEnd.plusDays(1).atStartOfDay();

        var rows = orderItemRepository.aggregateDailyRevenue(from, toExclusive);

        // Map kết quả theo ngày
        Map<LocalDate, DailyRevenueDTO> data = rows.stream().collect(Collectors.toMap(
                r -> ((java.sql.Date) r[0]).toLocalDate(),      // d
                r -> new DailyRevenueDTO(
                        ((java.sql.Date) r[0]).toLocalDate(),    // date
                        ((Number) r[1]).longValue(),             // countOrder
                        (java.math.BigDecimal) r[2]              // revenue
                )
        ));

        // Trả về danh sách đầy đủ từ start..end (kể cả ngày trống → 0)
        List<DailyRevenueDTO> result = new ArrayList<>();
        for (LocalDate d = effectiveStart; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
            DailyRevenueDTO dto = data.getOrDefault(d,
                    new DailyRevenueDTO(d, 0L, BigDecimal.ZERO));
            result.add(dto);
        }
        return result;
    }

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

