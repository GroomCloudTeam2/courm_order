package com.groom.e_commerce.global.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groom.e_commerce.review.domain.entity.ReviewCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AiWebClient {

	private final WebClient webClient;

	public AiWebClient(WebClient aiWebClientInstance) {
		this.webClient = aiWebClientInstance;
	}

	/**
	 * 코멘트를 AI에 보내 분류(Category)와 확신도(confidence)를 반환
	 */
	public AiResponse classifyComment(String comment) {
		try {
			// AI 서버로 POST 요청
			var response = webClient.post()
				.uri("/infer")
				.bodyValue(new AiRequest(comment))
				.retrieve()
				.bodyToMono(String.class) // JSON 문자열로 수신
				.block();

			ObjectMapper mapper = new ObjectMapper();
			var jsonNode = mapper.readTree(response);

			String aiCategory = jsonNode.get("category").asText();
			double confidence = jsonNode.get("confidence").asDouble();

			// String -> ReviewCategory 변환
			return new AiResponse(aiCategory, confidence);

		} catch (WebClientResponseException e) {
			// AI 서버 응답 에러 처리
			return new AiResponse(ReviewCategory.ERR, 0.0);
		} catch (Exception e) {
			// JSON 파싱 등 기타 에러 처리
			return new AiResponse(ReviewCategory.ERR, 0.0);
		}
	}

	// ======= 요청/응답 DTO =======

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AiRequest {
		private String comment;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AiResponse {

		private ReviewCategory category;
		private double confidence;

		public AiResponse(String aiCategory, double confidence) {
			this.category = switch (aiCategory) {
				case "디자인/외형" -> ReviewCategory.DESIGN;
				case "성능/기능" -> ReviewCategory.PERFORMANCE;
				case "편의성/사용감" -> ReviewCategory.CONVENIENCE;
				case "가격/구성" -> ReviewCategory.PRICE;
				case "품질/내구성" -> ReviewCategory.QUALITY;
				default -> ReviewCategory.ERR;
			};
			this.confidence = confidence;
		}
	}
}
