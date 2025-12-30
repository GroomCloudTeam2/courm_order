package com.groom.ecommerce.review.domain.entity;

import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID reviewId;

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private UUID productId;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false)
	@Min(1)
	@Max(5)
	private Integer rating;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ReviewCategory category;

	@Builder
	public ReviewEntity(UUID orderId,
		UUID productId,
		UUID userId,
		Integer rating,
		String content,
		ReviewCategory category) {
		this.orderId = orderId;
		this.productId = productId;
		this.userId = userId;
		this.rating = rating;
		this.content = content;
		this.category = category;
	}

	// 엔티티 변경 메서드
	public void updateRating(Integer rating) {
		this.rating = rating;
	}

	public void updateContentAndCategory(String content, ReviewCategory category) {
		this.content = content;
		this.category = category;
	}
}
