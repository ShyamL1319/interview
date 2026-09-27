package com.jpmorgan.interview;

import com.jpmorgan.interview.dto.PaymentConfirmRequest;
import com.jpmorgan.interview.dto.PaymentInitiateRequest;
import com.jpmorgan.interview.dto.PaymentResponse;
import com.jpmorgan.interview.enums.FraudDecisionType;
import com.jpmorgan.interview.enums.PaymentStatus;
import com.jpmorgan.interview.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void shouldBlockPaymentWhenLocationDiffersFromConfirmation() {
        PaymentInitiateRequest initiateRequest = new PaymentInitiateRequest(
                "txn-1001",
                "acct-1",
                new java.math.BigDecimal("120.00"),
                "USD",
                "US-NY",
                "device-1",
                "1.1.1.1"
        );

        PaymentResponse initiated = paymentService.initiatePayment(initiateRequest);

        PaymentResponse confirmed = paymentService.confirmPayment(
                new PaymentConfirmRequest(
                        initiated.getTransactionId(),
                        "CA-TOR",
                        "device-1",
                        "1.1.1.1"
                )
        );

        assertThat(initiated.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(confirmed.getPaymentStatus()).isEqualTo(PaymentStatus.BLOCKED);
        assertThat(confirmed.getFraudDecision()).isEqualTo(FraudDecisionType.BLOCKED);
        assertThat(confirmed.getFraudReason()).contains("location");
    }

    @Test
    void shouldBlockPaymentAfterRepeatedAttempts() {
        PaymentResponse first = paymentService.initiatePayment(new PaymentInitiateRequest(
                "txn-2001",
                "acct-2",
                new java.math.BigDecimal("50.00"),
                "USD",
                "US-CHI",
                "device-2",
                "2.2.2.2"
        ));

        PaymentResponse second = paymentService.initiatePayment(new PaymentInitiateRequest(
                "txn-2002",
                "acct-2",
                new java.math.BigDecimal("50.00"),
                "USD",
                "US-CHI",
                "device-2",
                "2.2.2.2"
        ));

        PaymentResponse third = paymentService.initiatePayment(new PaymentInitiateRequest(
                "txn-2003",
                "acct-2",
                new java.math.BigDecimal("50.00"),
                "USD",
                "US-CHI",
                "device-2",
                "2.2.2.2"
        ));

        PaymentResponse fourth = paymentService.initiatePayment(new PaymentInitiateRequest(
                "txn-2004",
                "acct-2",
                new java.math.BigDecimal("50.00"),
                "USD",
                "US-CHI",
                "device-2",
                "2.2.2.2"
        ));

        assertThat(first.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(second.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(third.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(fourth.getPaymentStatus()).isEqualTo(PaymentStatus.BLOCKED);
        assertThat(fourth.getFraudDecision()).isEqualTo(FraudDecisionType.BLOCKED);
    }
}
