package com.jpmorgan.interview.domain;

import com.jpmorgan.interview.enums.FraudDecisionType;
import com.jpmorgan.interview.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String initiationLocation;

    @Column
    private String confirmationLocation;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private Integer retryCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecisionType fraudDecision;

    @Column
    private String fraudReason;
}
