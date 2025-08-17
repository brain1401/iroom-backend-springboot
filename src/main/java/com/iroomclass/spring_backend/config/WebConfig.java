package com.iroomclass.spring_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 웹 관련 설정
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate 빈 설정
     * 외부 API 호출용
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}