package com.groom.e_commerce.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReqConfirmPaymentV1(
	@NotBlank String paymentKey,
	@NotBlank String orderId,
	@NotNull @Positive Long amount
) {
}
