package com.swp.controller.admin;

import com.swp.dto.DailyRevenueDTO;
import com.swp.dto.ProductRevenueDTO;
import com.swp.entity.OrderEntity;
import com.swp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("admin/reports")
@RequiredArgsConstructor
public class SaleReportController {
    private final ReportService reportService;

    public void monthlyReport(Model model) {
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        List<OrderEntity> allOrders = reportService.orderList(); // Hoặc service lấy dữ liệu
        Map<String, BigDecimal> monthlyMap = reportService.calculateMonthlyRevenue(allOrders);

        List<YearMonth> last5Months = IntStream.rangeClosed(0, 4)
                .map(i -> 4 - i) // đảo ngược
                .mapToObj(i -> YearMonth.now().minusMonths(i))
                .toList();


        List<String> labels = last5Months.stream()
                .map(m -> m.format(ymFmt))                                  // "yyyy-MM"
                .toList();

        List<BigDecimal> values = labels.stream()
                .map(k -> monthlyMap.getOrDefault(k, BigDecimal.ZERO))
                .toList();

        model.addAttribute("months", labels);
        model.addAttribute("values", values);

    }
    @GetMapping
    public String getReport(Model model) {
        BigDecimal totalRevenue = reportService.totalRevenue();
        long totalOrders = reportService.countOrders();
        double avgOrderValue = (totalOrders == 0) ? 0.0
                : totalRevenue.doubleValue() / totalOrders;

        List<ProductRevenueDTO> topProducts = reportService.getTop10ProductsByRevenue();
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("avgOrderValue", avgOrderValue);

        // Chart (last 5 months)
        monthlyReport(model);

        // Daily table: today by default
        LocalDate today = LocalDate.now();
        List<DailyRevenueDTO> daily = reportService.getDailyRevenue(today, today);
        model.addAttribute("daily", daily);
        model.addAttribute("start", today);
        model.addAttribute("end", today);

        return "admin/sale-report";
    }

    @PostMapping
    public String filter(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        // Nếu thiếu 1 trong 2, mặc định hôm nay
        if (start == null || end == null) {
            LocalDate today = LocalDate.now();
            start = today;
            end = today;
        }

        // Summary cards
        BigDecimal totalRevenue = reportService.totalRevenue();
        long totalOrders = reportService.countOrders();
        double avgOrderValue = (totalOrders == 0) ? 0.0
                : totalRevenue.doubleValue() / totalOrders;

        List<ProductRevenueDTO> topProducts = reportService.getTop10ProductsByRevenue();
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("avgOrderValue", avgOrderValue);

        // Chart (last 5 months)
        monthlyReport(model);

        // Daily table: full range [start..end]
        List<DailyRevenueDTO> daily = reportService.getDailyRevenue(start, end);
        model.addAttribute("daily", daily);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        return "admin/sale-report";
    }
}
