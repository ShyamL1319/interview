package com.jpmorgan.interview.dto;

import com.jpmorgan.interview.enums.FraudDecisionType;
import com.jpmorgan.interview.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private String initiationLocation;
    private String confirmationLocation;
    private Integer retryCount;
    private FraudDecisionType fraudDecision;
    private String fraudReason;
    private LocalDateTime initiatedAt;
    private LocalDateTime confirmedAt;
}
