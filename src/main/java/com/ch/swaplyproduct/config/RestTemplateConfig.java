package com.ch.swaplyproduct.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 내부 서비스 간 HTTP 통신용 RestTemplate Bean.
 * payment-service 의 RestTemplateConfig 와 동일한 패턴.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
