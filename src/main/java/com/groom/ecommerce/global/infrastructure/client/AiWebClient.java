package com.groom.ecommerce.global.infrastructure.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AiWebClient {

	private final WebClient webClient;

	public AiWebClient() {
		this.webClient = WebClient.builder()
			.baseUrl("http://localhost:8000") // AI 컨테이너 주소
			.build();
	}

	public AiResponse classifyComment(String comment) {
		try {
			return webClient.post()
				.uri("/infer")
				.bodyValue(new AiRequest(comment))
				.retrieve()
				.bodyToMono(AiResponse.class)
				.block(); // 블로킹 호출, 테스트용으로 적합
		} catch (WebClientResponseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class AiRequest {
		private String comment;

		public AiRequest(String comment) {
			this.comment = comment;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

	public static class AiResponse {
		private String category;
		private double confidence;

		public AiResponse() {
		} // Jackson용 기본 생성자

		public AiResponse(String category) {
			this.category = category;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public double getConfidence() {
			return confidence;
		}

		public void setConfidence(double confidence) {
			this.confidence = confidence;
		}
	}
}
