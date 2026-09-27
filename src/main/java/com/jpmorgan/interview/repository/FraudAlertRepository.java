package com.jpmorgan.interview.repository;

import com.jpmorgan.interview.domain.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    List<FraudAlert> findByTransactionIdOrderByCreatedAtDesc(String transactionId);
}
