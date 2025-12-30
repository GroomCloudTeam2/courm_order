package com.groom.ecommerce.review.application.service;

import com.groom.ecommerce.global.infrastructure.client.AiWebClient;
import com.groom.ecommerce.review.domain.entity.ProductRatingEntity;
import com.groom.ecommerce.review.domain.entity.ReviewCategory;
import com.groom.ecommerce.review.domain.entity.ReviewEntity;
import com.groom.ecommerce.review.domain.repository.ProductRatingRepository;
import com.groom.ecommerce.review.domain.repository.ReviewRepository;
import com.groom.ecommerce.review.presentation.dto.request.CreateReviewRequest;
import com.groom.ecommerce.review.presentation.dto.request.UpdateReviewRequest;
import com.groom.ecommerce.review.presentation.dto.response.ProductReviewResponse;
import com.groom.ecommerce.review.presentation.dto.response.ReviewResponse;
import com.groom.ecommerce.review.presentation.dto.response.PaginationResponse;

import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ProductRatingRepository productRatingRepository;
	private final AiWebClient aiWebClient;

	/**
	 * 리뷰 작성: AI 카테고리 분류 및 평점 반영
	 */
	@Transactional
	public ReviewResponse createReview(UUID orderId, UUID productId, UUID userId, CreateReviewRequest request) {
		reviewRepository.findByOrderIdAndProductId(orderId, productId)
			.ifPresent(r -> { throw new IllegalStateException("이미 리뷰가 존재합니다."); });

		// 1. FastAPI 서버를 통해 카테고리 분류
		String aiCategoryStr = classifyComment(request.getContent());
		ReviewCategory category = ReviewCategory.fromAiCategory(aiCategoryStr);

		// 2. 리뷰 엔티티 생성 및 저장
		ReviewEntity review = ReviewEntity.builder()
			.orderId(orderId)
			.productId(productId)
			.userId(userId)
			.rating(request.getRating())
			.content(request.getContent())
			.category(category)
			.build();
		reviewRepository.save(review);

		// 3. 평점 통계 업데이트 (새 별점 추가)
		ProductRatingEntity ratingEntity = productRatingRepository.findByProductId(productId)
			.orElseGet(() -> {
				// 엔티티가 없으면 새로 생성 (productId 설정 필수)
				// ProductRatingEntity에 productId 설정 생성자/메서드가 있다고 가정
				return new ProductRatingEntity(productId);
			});

		ratingEntity.updateRating(request.getRating());
		productRatingRepository.save(ratingEntity);

		return ReviewResponse.fromEntity(review);
	}

	/**
	 * 리뷰 수정: 별점 변경 시 통계 재계산 필요
	 */
	@Transactional
	public ReviewResponse updateReview(UUID orderId, UUID productId, UUID userId, UpdateReviewRequest request) {
		ReviewEntity review = reviewRepository.findByOrderIdAndProductId(orderId, productId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

		// 본인 확인 로직 (컨트롤러에서 넘겨받은 userId와 비교)
		if (!review.getUserId().equals(userId)) {
			throw new SecurityException("수정 권한이 없습니다.");
		}

		// 별점이 변경되었다면 통계 수정 로직 필요 (이 부분은 단순 updateRating으로는 부족함)
		// 실제 구현 시에는 기존 점수를 빼고 새 점수를 더하는 별도 메서드 추천
		if (request.getRating() != null && !review.getRating().equals(request.getRating())) {
			// (생략) 평점 보정 로직 호출
			review.updateRating(request.getRating());
		}

		if (request.getContent() != null && !request.getContent().isBlank()) {
			String categoryStr = classifyComment(request.getContent());
			ReviewCategory category = ReviewCategory.fromAiCategory(categoryStr);
			review.updateContentAndCategory(request.getContent(), category);
		}

		return ReviewResponse.fromEntity(review);
	}

	/**
	 * 리뷰 삭제: 평점 통계 마이너스 처리
	 */
	@Transactional
	public void deleteReview(UUID reviewId, UUID userId) {
		ReviewEntity review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		if (!review.getUserId().equals(userId)) {
			throw new SecurityException("삭제 권한이 없습니다.");
		}

		// 평점 통계에서 제외 (별도의 차감 메서드 구현 권장)
		ProductRatingEntity ratingEntity = productRatingRepository.findByProductId(review.getProductId())
			.orElseThrow(() -> new IllegalStateException("상품 통계 정보가 없습니다."));

		// ratingEntity.removeRating(review.getRating()); // 이전 답변의 추천 메서드 활용

		reviewRepository.delete(review);
	}

  /*
	@Transactional
	public void deleteReview(UUID reviewId, UUID currentUserId) {
		ReviewEntity review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		// 본인 확인: 토큰의 ID와 리뷰 작성자 ID 비교
		if (!review.getUserId().equals(currentUserId)) {
			throw new AccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
		}

		// 평점 차감 로직 실행 후 삭제
		ProductRatingEntity ratingEntity = productRatingRepository.findByProductId(review.getProductId())
			.orElseThrow(() -> new IllegalStateException("상품 통계 정보가 없습니다."));

		ratingEntity.removeRating(review.getRating());
		reviewRepository.delete(review);
	}
	*/


	/**
	 * 상품별 리뷰 목록 조회 (페이징 반영)
	 */
	public ProductReviewResponse getProductReviews(UUID productId, int page, int size) {
		// 1. 최신순 정렬을 포함한 페이징 설정
		Pageable pageable = PageRequest.of(page, size, Sort.by("reviewId").descending());

		// 2. DB 조회
		Page<ReviewEntity> reviewPage = reviewRepository.findAllByProductId(productId, pageable);
		ProductRatingEntity ratingEntity = productRatingRepository.findByProductId(productId)
			.orElseGet(() -> new ProductRatingEntity()); // 기본 생성자 확인 필요

		// 3. 변환 및 반환
		return ProductReviewResponse.builder()
			.avgRating(ratingEntity.getAvgRating())
			.reviewCount(ratingEntity.getReviewCount())
			.aiReview(ratingEntity.getAiReview())
			.reviews(reviewPage.getContent().stream()
				.map(ReviewResponse::fromEntity)
				.collect(Collectors.toList()))
			.pagination(PaginationResponse.builder()
				.totalElements(reviewPage.getTotalElements())
				.totalPages(reviewPage.getTotalPages())
				.currentPage(reviewPage.getNumber())
				.isLast(reviewPage.isLast())
				.build())
			.build();
	}

	/**
	 * 특정 주문 상품에 대한 단건 리뷰 조회
	 */
	public ReviewResponse getReview(UUID orderId, UUID productId) {
		ReviewEntity review = reviewRepository.findByOrderIdAndProductId(orderId, productId)
			.orElseThrow(() -> new IllegalArgumentException("해당 주문 상품에 대한 리뷰를 찾을 수 없습니다."));

		return ReviewResponse.fromEntity(review);
	}

	private String classifyComment(String comment) {
		try {
			AiWebClient.AiResponse response = aiWebClient.classifyComment(comment);
			return (response != null) ? response.getCategory() : null;
		} catch (Exception e) {
			log.error("AI 서버 통신 실패: {}", e.getMessage());
			return null;
		}
	}
}
