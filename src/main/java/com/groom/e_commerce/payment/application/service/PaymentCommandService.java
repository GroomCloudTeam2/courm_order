package com.groom.e_commerce.payment.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groom.e_commerce.payment.application.port.in.CancelPaymentUseCase;
import com.groom.e_commerce.payment.application.port.in.ConfirmPaymentUseCase;
import com.groom.e_commerce.payment.application.port.out.TossPaymentPort;
import com.groom.e_commerce.payment.domain.entity.Payment;
import com.groom.e_commerce.payment.domain.entity.PaymentCancel;
import com.groom.e_commerce.payment.domain.model.PaymentMethod;
import com.groom.e_commerce.payment.domain.repository.PaymentRepository;
import com.groom.e_commerce.payment.infrastructure.api.toss.dto.request.TossCancelRequest;
import com.groom.e_commerce.payment.infrastructure.api.toss.dto.request.TossConfirmRequest;
import com.groom.e_commerce.payment.infrastructure.api.toss.dto.response.TossCancelResponse;
import com.groom.e_commerce.payment.infrastructure.api.toss.dto.response.TossPaymentResponse;
import com.groom.e_commerce.payment.presentation.dto.request.ReqCancelPaymentV1;
import com.groom.e_commerce.payment.presentation.dto.request.ReqConfirmPaymentV1;
import com.groom.e_commerce.payment.presentation.dto.response.ResCancelResultV1;
import com.groom.e_commerce.payment.presentation.dto.response.ResPaymentV1;
import com.groom.e_commerce.payment.presentation.exception.PaymentException;

@Service
@Transactional
public class PaymentCommandService implements ConfirmPaymentUseCase, CancelPaymentUseCase {

	private final PaymentRepository paymentRepository;
	private final TossPaymentPort tossPaymentPort;

	public PaymentCommandService(PaymentRepository paymentRepository, TossPaymentPort tossPaymentPort) {
		this.paymentRepository = paymentRepository;
		this.tossPaymentPort = tossPaymentPort;
	}

	@Override
	public ResPaymentV1 confirm(ReqConfirmPaymentV1 request) {
		// 1) 내부 중복 방지(멱등)
		Payment existing = paymentRepository.findByPaymentKey(request.paymentKey()).orElse(null);
		if (existing != null && existing.isAlreadyDone()) {
			return ResPaymentV1.from(existing);
		}

		// 2) 토스 승인 호출
		TossPaymentResponse toss = tossPaymentPort.confirm(
			new TossConfirmRequest(request.paymentKey(), request.orderId(), request.amount())
		);

		// 3) 내부 저장/업데이트
		Payment payment = existing;
		if (payment == null) {
			payment = new Payment(toss.orderId(), toss.paymentKey(), toss.totalAmount());
		}

		payment.markApproved(
			toss.totalAmount(),
			mapMethod(toss.method()),
			toss.currency(),
			toss.orderName(),
			toss.customerName(),
			toss.requestedAt(),
			toss.approvedAt()
		);

		Payment saved = paymentRepository.save(payment);
		return ResPaymentV1.from(saved);
	}

	@Override
	public ResCancelResultV1 cancel(String paymentKey, ReqCancelPaymentV1 request) {
		Payment payment = paymentRepository.findByPaymentKey(paymentKey)
			.orElseThrow(() -> new PaymentException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."));

		if (payment.getStatus().name().equals("CANCELED")) {
			throw new PaymentException(HttpStatus.CONFLICT, "ALREADY_CANCELED", "이미 전액 취소된 결제입니다.");
		}

		// 부분취소 검증
		Long cancelAmount =
			request.cancelAmount() == null ? (payment.getApprovedAmount() - payment.getCanceledAmount()) :
				request.cancelAmount();
		if (cancelAmount <= 0) {
			throw new PaymentException(HttpStatus.BAD_REQUEST, "INVALID_CANCEL_AMOUNT", "취소 금액이 올바르지 않습니다.");
		}
		if (payment.getCanceledAmount() + cancelAmount > payment.getApprovedAmount()) {
			throw new PaymentException(HttpStatus.BAD_REQUEST, "EXCEED_CANCEL_AMOUNT", "취소 가능 금액을 초과했습니다.");
		}

		// 토스 취소 호출
		TossCancelResponse tossCancel = tossPaymentPort.cancel(paymentKey,
			new TossCancelRequest(request.cancelReason(), cancelAmount)
		);

		// 토스 응답에서 취소 이력 1건을 대표로 저장
		PaymentCancel cancel = new PaymentCancel(
			tossCancel.paymentKey(),
			cancelAmount,
			request.cancelReason(),
			tossCancel.canceledAt()
		);
		payment.addCancel(cancel);

		Payment saved = paymentRepository.save(payment);

		return ResCancelResultV1.of(saved.getPaymentKey(), saved.getStatus().name(), saved.getCanceledAmount());
	}

	private PaymentMethod mapMethod(String tossMethod) {
		if (tossMethod == null)
			return PaymentMethod.UNKNOWN;
		return switch (tossMethod.toUpperCase()) {
			case "CARD" -> PaymentMethod.CARD;
			case "EASY_PAY" -> PaymentMethod.EASY_PAY;
			case "TRANSFER" -> PaymentMethod.TRANSFER;
			case "MOBILE_PHONE" -> PaymentMethod.MOBILE;
			default -> PaymentMethod.UNKNOWN;
		};
	}
}
