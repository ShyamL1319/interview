package com.jpmorgan.interview.controller;

import com.jpmorgan.interview.dto.PaymentConfirmRequest;
import com.jpmorgan.interview.dto.PaymentInitiateRequest;
import com.jpmorgan.interview.dto.PaymentResponse;
import com.jpmorgan.interview.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        return new ResponseEntity<>(paymentService.initiatePayment(request), HttpStatus.CREATED);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPayment(transactionId));
    }
}
