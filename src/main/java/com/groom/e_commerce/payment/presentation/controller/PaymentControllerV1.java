package com.groom.e_commerce.payment.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groom.e_commerce.payment.application.port.in.CancelPaymentUseCase;
import com.groom.e_commerce.payment.application.port.in.ConfirmPaymentUseCase;
import com.groom.e_commerce.payment.application.port.in.GetPaymentUseCase;
import com.groom.e_commerce.payment.presentation.dto.request.ReqCancelPaymentV1;
import com.groom.e_commerce.payment.presentation.dto.request.ReqConfirmPaymentV1;
import com.groom.e_commerce.payment.presentation.dto.response.ResCancelResultV1;
import com.groom.e_commerce.payment.presentation.dto.response.ResPaymentV1;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentControllerV1 {

	private final ConfirmPaymentUseCase confirmPaymentUseCase;
	private final CancelPaymentUseCase cancelPaymentUseCase;
	private final GetPaymentUseCase getPaymentUseCase;

	public PaymentControllerV1(ConfirmPaymentUseCase confirmPaymentUseCase,
		CancelPaymentUseCase cancelPaymentUseCase,
		GetPaymentUseCase getPaymentUseCase) {
		this.confirmPaymentUseCase = confirmPaymentUseCase;
		this.cancelPaymentUseCase = cancelPaymentUseCase;
		this.getPaymentUseCase = getPaymentUseCase;
	}

	// 결제 승인(토스 confirm)
	@PostMapping("/confirm")
	public ResponseEntity<ResPaymentV1> confirm(@Valid @RequestBody ReqConfirmPaymentV1 request) {
		return ResponseEntity.ok(confirmPaymentUseCase.confirm(request));
	}

	// 결제 조회(토스 조회 or 내부 조회)
	@GetMapping("/{paymentKey}")
	public ResponseEntity<ResPaymentV1> getByPaymentKey(@PathVariable String paymentKey) {
		return ResponseEntity.ok(getPaymentUseCase.getByPaymentKey(paymentKey));
	}

	// 주문ID로 결제 조회(내부)
	@GetMapping("/by-order/{orderId}")
	public ResponseEntity<ResPaymentV1> getByOrderId(@PathVariable String orderId) {
		return ResponseEntity.ok(getPaymentUseCase.getByOrderId(orderId));
	}

	// 결제 취소(토스 cancel)
	@PostMapping("/{paymentKey}/cancel")
	public ResponseEntity<ResCancelResultV1> cancel(@PathVariable String paymentKey,
		@Valid @RequestBody ReqCancelPaymentV1 request) {
		return ResponseEntity.ok(cancelPaymentUseCase.cancel(paymentKey, request));
	}
}
