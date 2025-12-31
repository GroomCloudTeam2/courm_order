package com.groom.e_commerce.order.presentation.dto.response;

import com.groom.e_commerce.order.domain.entity.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
	UUID productId,
	String productName,
	BigDecimal unitPrice,
	int quantity,
	BigDecimal subtotal
) {
	// Entity -> DTO 변환 메서드 (Static Factory Method)
	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(
			item.getProductId(),
			item.getProductTitle(),
			BigDecimal.valueOf(item.getUnitPrice()),
			item.getQuantity(),
			BigDecimal.valueOf(item.getSubtotal())
		);
	}
}
