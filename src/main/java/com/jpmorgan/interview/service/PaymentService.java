package com.jpmorgan.interview.service;

import com.jpmorgan.interview.config.FraudProperties;
import com.jpmorgan.interview.domain.FraudAlert;
import com.jpmorgan.interview.domain.PaymentEvent;
import com.jpmorgan.interview.domain.PaymentTransaction;
import com.jpmorgan.interview.dto.PaymentConfirmRequest;
import com.jpmorgan.interview.dto.PaymentInitiateRequest;
import com.jpmorgan.interview.dto.PaymentResponse;
import com.jpmorgan.interview.enums.FraudDecisionType;
import com.jpmorgan.interview.enums.PaymentStatus;
import com.jpmorgan.interview.exception.BusinessException;
import com.jpmorgan.interview.repository.FraudAlertRepository;
import com.jpmorgan.interview.repository.PaymentEventRepository;
import com.jpmorgan.interview.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudProperties fraudProperties;

    public PaymentService(PaymentTransactionRepository paymentTransactionRepository,
                          PaymentEventRepository paymentEventRepository,
                          FraudAlertRepository fraudAlertRepository,
                          FraudProperties fraudProperties) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.fraudProperties = fraudProperties;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        if (paymentTransactionRepository.existsById(request.transactionId())) {
            throw new BusinessException("Payment with transactionId already exists");
        }

        List<PaymentTransaction> recentTransactions = paymentTransactionRepository
                .findByAccountIdOrderByInitiatedAtDesc(request.accountId())
                .stream()
                .filter(tx -> tx.getInitiatedAt().isAfter(LocalDateTime.now().minusMinutes(fraudProperties.getRetryWindowMinutes())))
                .collect(Collectors.toList());

        int recentAttempts = recentTransactions.size();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(request.transactionId())
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency())
                .paymentStatus(recentAttempts >= fraudProperties.getMaxAttempts() ? PaymentStatus.BLOCKED : PaymentStatus.INITIATED)
                .initiationLocation(request.initiationLocation())
                .deviceId(request.deviceId())
                .ipAddress(request.ipAddress())
                .initiatedAt(LocalDateTime.now())
                .retryCount(recentAttempts)
                .fraudDecision(recentAttempts >= fraudProperties.getMaxAttempts() ? FraudDecisionType.BLOCKED : FraudDecisionType.SAFE)
                .fraudReason(recentAttempts >= fraudProperties.getMaxAttempts() ? "Repeated payment attempts detected for this account" : "")
                .build();

        paymentTransactionRepository.save(transaction);

        PaymentEvent event = PaymentEvent.builder()
                .transactionId(transaction.getTransactionId())
                .eventType(transaction.getPaymentStatus().name())
                .location(transaction.getInitiationLocation())
                .deviceId(transaction.getDeviceId())
                .ipAddress(transaction.getIpAddress())
                .eventTime(LocalDateTime.now())
                .metadata(recentAttempts >= fraudProperties.getMaxAttempts() ? "repeated-attempt-block" : "initiation")
                .build();

        paymentEventRepository.save(event);

        if (transaction.getPaymentStatus() == PaymentStatus.BLOCKED) {
            FraudAlert alert = FraudAlert.builder()
                    .transactionId(transaction.getTransactionId())
                    .ruleCode("RETRY_LIMIT_EXCEEDED")
                    .decisionType(FraudDecisionType.BLOCKED)
                    .description("Repeated payment attempts detected for this account")
                    .createdAt(LocalDateTime.now())
                    .build();
            fraudAlertRepository.save(alert);
        }

        return toResponse(transaction);
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new BusinessException("Payment transaction not found"));

        if (transaction.getPaymentStatus() == PaymentStatus.BLOCKED || transaction.getPaymentStatus() == PaymentStatus.REJECTED) {
            return toResponse(transaction);
        }

        if (transaction.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException("Payment already confirmed");
        }

        transaction.setConfirmationLocation(request.confirmationLocation());
        transaction.setConfirmedAt(LocalDateTime.now());
        transaction.setPaymentStatus(PaymentStatus.CONFIRMED);

        if (hasLocationMismatch(transaction)) {
            blockPayment(transaction, "LOCATION_MISMATCH", "Payment confirmation location differs from initiation location");
        } else {
            if (hasRetryLimitExceeded(transaction)) {
                blockPayment(transaction, "RETRY_LIMIT_EXCEEDED", "Payment exceeded allowed retry threshold");
            } else {
                transaction.setPaymentStatus(PaymentStatus.CONFIRMED);
                transaction.setFraudDecision(FraudDecisionType.SAFE);
                transaction.setFraudReason("");
            }
        }

        paymentTransactionRepository.save(transaction);

        PaymentEvent event = PaymentEvent.builder()
                .transactionId(transaction.getTransactionId())
                .eventType(transaction.getPaymentStatus().name())
                .location(request.confirmationLocation())
                .deviceId(request.deviceId())
                .ipAddress(request.ipAddress())
                .eventTime(LocalDateTime.now())
                .metadata("confirmation")
                .build();

        paymentEventRepository.save(event);

        return toResponse(transaction);
    }

    public PaymentResponse getPayment(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Payment transaction not found"));
        return toResponse(transaction);
    }

    private boolean hasLocationMismatch(PaymentTransaction transaction) {
        return !Objects.equals(transaction.getInitiationLocation(), transaction.getConfirmationLocation());
    }

    private boolean hasRetryLimitExceeded(PaymentTransaction transaction) {
        List<PaymentEvent> events = paymentEventRepository.findByTransactionIdOrderByEventTimeDesc(transaction.getTransactionId());
        long attempts = events.size();
        return attempts >= fraudProperties.getMaxAttempts();
    }

    private void blockPayment(PaymentTransaction transaction, String ruleCode, String reason) {
        transaction.setPaymentStatus(PaymentStatus.BLOCKED);
        transaction.setFraudDecision(FraudDecisionType.BLOCKED);
        transaction.setFraudReason(reason);

        if (fraudProperties.isAutoBlock()) {
            FraudAlert alert = FraudAlert.builder()
                    .transactionId(transaction.getTransactionId())
                    .ruleCode(ruleCode)
                    .decisionType(FraudDecisionType.BLOCKED)
                    .description(reason)
                    .createdAt(LocalDateTime.now())
                    .build();
            fraudAlertRepository.save(alert);
        }
    }

    private PaymentResponse toResponse(PaymentTransaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .paymentStatus(transaction.getPaymentStatus())
                .initiationLocation(transaction.getInitiationLocation())
                .confirmationLocation(transaction.getConfirmationLocation())
                .retryCount(transaction.getRetryCount())
                .fraudDecision(transaction.getFraudDecision())
                .fraudReason(transaction.getFraudReason())
                .initiatedAt(transaction.getInitiatedAt())
                .confirmedAt(transaction.getConfirmedAt())
                .build();
    }
}
