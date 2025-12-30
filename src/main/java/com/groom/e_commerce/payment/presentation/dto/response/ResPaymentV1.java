package com.groom.e_commerce.payment.presentation.dto.response;

import java.time.OffsetDateTime;

import com.groom.e_commerce.payment.domain.entity.Payment;
import com.groom.e_commerce.payment.infrastructure.api.toss.dto.response.TossPaymentResponse;

public record ResPaymentV1(
	String paymentKey,
	String orderId,
	String status,
	String method,
	Long approvedAmount,
	Long canceledAmount,
	String currency,
	String orderName,
	String customerName,
	OffsetDateTime requestedAt,
	OffsetDateTime approvedAt
) {
	public static ResPaymentV1 from(Payment payment) {
		return new ResPaymentV1(
			payment.getPaymentKey(),
			payment.getOrderId(),
			payment.getStatus().name(),
			payment.getMethod().name(),
			payment.getApprovedAmount(),
			payment.getCanceledAmount(),
			payment.getCurrency(),
			payment.getOrderName(),
			payment.getCustomerName(),
			payment.getRequestedAt(),
			payment.getApprovedAt()
		);
	}

	public static ResPaymentV1 fromToss(TossPaymentResponse toss) {
		return new ResPaymentV1(
			toss.paymentKey(),
			toss.orderId(),
			toss.status(),
			toss.method(),
			toss.totalAmount(),
			0L,
			toss.currency(),
			toss.orderName(),
			toss.customerName(),
			toss.requestedAt(),
			toss.approvedAt()
		);
	}
}
