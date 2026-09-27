package com.jpmorgan.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmRequest(
        @NotBlank String transactionId,
        @NotBlank String confirmationLocation,
        @NotBlank String deviceId,
        @NotBlank String ipAddress
) {
}
