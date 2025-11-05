package com.swp.service;

import com.swp.entity.OrderEntity;
import com.swp.entity.OrderItemEntity;
import com.swp.entity.PaymentEntity;
import com.swp.entity.ProductVariantEntity;
import com.swp.repository.PaymentRepository;
import com.swp.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public PaymentEntity createPayment(OrderEntity order, String method) {
        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .method(method)
                .status("PENDING")
                .transactionDate(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public void setVnpayTxnRef(Long paymentId, String vnpayTxnRef) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            PaymentEntity payment = paymentOpt.get();
            payment.setVnpayTxnRef(vnpayTxnRef);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void updatePaymentStatus(String vnpayTxnRef, String responseCode,
                                    String transactionNo, String bankCode, String payDate) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findByVnpayTxnRef(vnpayTxnRef);

        if (paymentOpt.isPresent()) {
            PaymentEntity payment = paymentOpt.get();
            payment.setVnpayResponseCode(responseCode);
            payment.setVnpayTransactionNo(transactionNo);
            payment.setVnpayBankCode(bankCode);
            payment.setVnpayPayDate(payDate);

            if ("00".equals(responseCode)) {
                payment.setStatus("SUCCESS");
                payment.setTransactionDate(LocalDateTime.now());
                orderService.updateOrderStatus(payment.getOrder().getOrderId(), "CONFIRMED");
                OrderEntity order = payment.getOrder();
                for (OrderItemEntity item : order.getOrderItems()) {
                    ProductVariantEntity variant = item.getProductVariant();
                    int currentStock = variant.getStock();
                    int newStock = currentStock - item.getQuantity();
                    if (newStock < 0) {
                        throw new IllegalStateException("Không đủ hàng trong kho cho sản phẩm: " + variant.getSku());
                    }
                    variant.setStock(newStock);
                    productVariantRepository.save(variant);
                }
            } else {
                payment.setStatus("FAILED");
                orderService.updateOrderStatus(payment.getOrder().getOrderId(), "CANCELLED");
            }

            paymentRepository.save(payment);
        }
    }

    public Optional<PaymentEntity> findByVnpayTxnRef(String vnpayTxnRef) {
        return paymentRepository.findByVnpayTxnRef(vnpayTxnRef);
    }

    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderOrderId(orderId);
    }

    // Admin transaction management methods
    public Page<PaymentEntity> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        return paymentRepository.findAll(pageable);
    }

    public Page<PaymentEntity> searchAndFilterPayments(String vnpayTxnRef, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        // Handle empty strings as null
        String searchTxn = (vnpayTxnRef != null && !vnpayTxnRef.trim().isEmpty())
                ? vnpayTxnRef.trim() : null;
        String filterStatus = (status != null && !status.trim().isEmpty()) ? status : null;

        // If both filters are present
        if (searchTxn != null || filterStatus != null) {
            return paymentRepository.findByStatusAndVnpayTxnRef(filterStatus, searchTxn, pageable);
        }

        // No filters, return all
        return paymentRepository.findAll(pageable);
    }

    public List<PaymentEntity> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<PaymentEntity> findById(Long id) {
        return paymentRepository.findById(id);
    }

    // Get statistics for dashboard
    public long countByStatus(String status) {
        return paymentRepository.findByStatus(status).size();
    }

    public long countAll() {
        return paymentRepository.count();
    }
}