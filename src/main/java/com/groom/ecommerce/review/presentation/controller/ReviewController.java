package com.groom.ecommerce.review.presentation.controller;

import com.groom.ecommerce.review.application.service.ReviewService;
import com.groom.ecommerce.review.presentation.dto.request.CreateReviewRequest;
import com.groom.ecommerce.review.presentation.dto.request.UpdateReviewRequest;
import com.groom.ecommerce.review.presentation.dto.response.ReviewResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	// 리뷰 작성
	@PostMapping("/{orderId}/items/{productId}")
	public ReviewResponse createReview(
		@PathVariable UUID orderId,
		@PathVariable UUID productId,
		//@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam UUID userId, //테스트용 userID
		@RequestBody CreateReviewRequest request
	) {
		return reviewService.createReview(orderId, productId, userId,request);
		//return reviewService.createReview(orderId, productId,userDetails.getUserId(),request);
	}

	// 개별 리뷰 상세 조회 (필요 시)
	@GetMapping("/{orderId}/items/{productId}/review")
	public ReviewResponse getReview(
		@PathVariable UUID orderId,
		@PathVariable UUID productId
	) {
		return reviewService.getReview(orderId, productId);
	}

	// 리뷰 수정
	@PutMapping("/{orderId}/items/{productId}/review")
	public ReviewResponse updateReview(
		@PathVariable UUID orderId,
		@PathVariable UUID productId,
		@RequestParam UUID userId, // 본인 확인용 추가
		@RequestBody UpdateReviewRequest request
	) {
		return reviewService.updateReview(orderId, productId, userId, request);
	}

	// 리뷰 삭제 추가
	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 삭제 성공 시 204 반환
	public void deleteReview(
		@PathVariable UUID reviewId,
		@RequestParam UUID userId // 본인 확인용
	) {
		reviewService.deleteReview(reviewId, userId);
	}
/*
	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReview(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		reviewService.deleteReview(reviewId, userDetails.getUserId());
	}
 */
}
