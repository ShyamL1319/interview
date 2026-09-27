package com.jpmorgan.interview.repository;

import com.jpmorgan.interview.domain.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    List<PaymentEvent> findByTransactionIdOrderByEventTimeDesc(String transactionId);

    long countByTransactionId(String transactionId);
}
