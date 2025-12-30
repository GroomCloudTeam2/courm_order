package com.groom.e_commerce.payment.domain.repository;

import java.util.Optional;

import com.groom.e_commerce.payment.domain.entity.Payment;

public interface PaymentRepository {

	Payment save(Payment payment);

	Optional<Payment> findById(String paymentId);

	Optional<Payment> findByOrderId(String orderId);

	boolean existsByOrderId(String orderId);

	Optional<Payment> findByPaymentKey(String paymentKey);

	Optional<Payment> findByIdWithLock(String paymentId);

	Optional<Payment> findByPaymentKeyWithLock(String paymentKey);
}
