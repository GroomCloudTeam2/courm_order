package com.groom.e_commerce.payment.domain.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "p_payment_cancel", indexes = {
	@Index(name = "idx_cancel_payment_key", columnList = "paymentKey")
})
public class PaymentCancel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String cancelId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(nullable = false, length = 200)
	private String paymentKey;

	@Column(nullable = false)
	private Long cancelAmount;

	@Column(nullable = false, length = 200)
	private String cancelReason;

	private OffsetDateTime canceledAt;

	protected PaymentCancel() {
	}

	public PaymentCancel(String paymentKey, Long cancelAmount, String cancelReason, OffsetDateTime canceledAt) {
		this.paymentKey = paymentKey;
		this.cancelAmount = cancelAmount;
		this.cancelReason = cancelReason;
		this.canceledAt = canceledAt;
	}

	void setPayment(Payment payment) {
		this.payment = payment;
	}

	// getters
	public String getCancelId() {
		return cancelId;
	}

	public Payment getPayment() {
		return payment;
	}

	public String getPaymentKey() {
		return paymentKey;
	}

	public Long getCancelAmount() {
		return cancelAmount;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public OffsetDateTime getCanceledAt() {
		return canceledAt;
	}
}
