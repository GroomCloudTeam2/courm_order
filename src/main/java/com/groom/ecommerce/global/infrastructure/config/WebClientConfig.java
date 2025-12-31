package com.groom.ecommerce.global.infrastructure.config;

import com.groom.ecommerce.global.infrastructure.client.AiWebClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

	@Bean
	public AiWebClient aiWebClient() {
		return new AiWebClient();
	}
}
