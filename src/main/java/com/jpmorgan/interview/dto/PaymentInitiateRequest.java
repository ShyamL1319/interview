package com.jpmorgan.interview.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentInitiateRequest(
        @NotBlank String transactionId,
        @NotBlank String accountId,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String initiationLocation,
        @NotBlank String deviceId,
        @NotBlank String ipAddress
) {
}
