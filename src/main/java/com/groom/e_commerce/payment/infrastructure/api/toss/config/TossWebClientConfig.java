package com.groom.e_commerce.payment.infrastructure.api.toss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TossWebClientConfig {

	@Bean
	public WebClient tossWebClient(@Value("${toss.payments.base-url}") String baseUrl) {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.build();
	}
}
