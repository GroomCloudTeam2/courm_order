package com.groom.e_commerce.payment.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groom.e_commerce.payment.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	Optional<Payment> findByPaymentKey(String paymentKey);

	Optional<Payment> findByOrderId(String orderId);

	boolean existsByOrderId(String orderId);

	boolean existsByPaymentKey(String paymentKey);
}
