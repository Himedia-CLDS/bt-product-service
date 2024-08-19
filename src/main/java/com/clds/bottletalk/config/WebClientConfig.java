package com.clds.bottletalk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${redis.baseUrl}")
    String baseUrl;
    @Value("${redis.port}")
    String port;

    @Bean
    public WebClient webClient() {
        String redisBaseUrl = baseUrl+":"+port;
        return WebClient.builder()
                .baseUrl(redisBaseUrl)
                .build();
    }
}
