package com.swp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(name = "method", nullable = false)
    private String method; // VNPAY, COD, CREDIT_CARD

    @Column(name = "status", nullable = false)
    private String status; // PENDING, SUCCESS, FAILED

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    // VNPay specific fields
    @Column(name = "vnpay_txn_ref", unique = true)
    private String vnpayTxnRef;

    @Column(name = "vnpay_transaction_no")
    private String vnpayTransactionNo;

    @Column(name = "vnpay_response_code")
    private String vnpayResponseCode;

    @Column(name = "vnpay_bank_code")
    private String vnpayBankCode;

    @Column(name = "vnpay_pay_date")
    private String vnpayPayDate;
}

