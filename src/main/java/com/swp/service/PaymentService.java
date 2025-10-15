package com.swp.service;

import com.swp.entity.OrderEntity;
import com.swp.entity.PaymentEntity;
import com.swp.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

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
                // Cập nhật order status
                orderService.updateOrderStatus(payment.getOrder().getOrderId(), "CONFIRMED");
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
}

