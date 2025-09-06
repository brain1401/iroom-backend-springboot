package com.iroomclass.springbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 웹 관련 설정
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate 빈 설정
     * 외부 API 호출용 (CallbackTimeoutHandler 포함)
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // AI 서버 상태 조회용 타임아웃 설정
        factory.setConnectTimeout(5000);    // 5초 연결 타임아웃
        factory.setReadTimeout(10000);      // 10초 읽기 타임아웃
        
        return new RestTemplate(factory);
    }
}