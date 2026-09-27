package com.jpmorgan.interview.controller;

import com.jpmorgan.interview.domain.FraudAlert;
import com.jpmorgan.interview.repository.FraudAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fraud-alerts")
public class FraudAlertController {

    private final FraudAlertRepository fraudAlertRepository;

    public FraudAlertController(FraudAlertRepository fraudAlertRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<List<FraudAlert>> getAlerts(@PathVariable String transactionId) {
        return ResponseEntity.ok(fraudAlertRepository.findByTransactionIdOrderByCreatedAtDesc(transactionId));
    }
}
