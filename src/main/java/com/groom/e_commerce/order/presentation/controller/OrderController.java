package com.groom.e_commerce.order.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groom.e_commerce.order.application.service.OrderService;
import com.groom.e_commerce.order.presentation.dto.request.OrderCreateRequest;

import lombok.RequiredArgsConstructor;

@Tag(name = "주문 API", description = "주문 생성 및 조회 관련 API") // 1. API 그룹 이름
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "주문 생성", description = "상품 정보와 배송지를 입력받아 주문을 생성합니다.") // 2. 메서드 설명
	@PostMapping
	public ResponseEntity<UUID> createOrder(
		// @RequestHeader("X-User-Id") UUID userId, // 나중에 게이트웨이에서 헤더로 넘어옴
		@RequestBody OrderCreateRequest request) {

		// 임시로 유저 ID 하드코딩 (로그인 구현 전이라면)
		UUID buyerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

		UUID orderId = orderService.createOrder(buyerId, request);

		return ResponseEntity.ok(orderId);
	}
}
