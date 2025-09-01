package com.iroomclass.springbackend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 서버 연동을 위한 WebClient 설정
 * 
 * <p>외부 AI 서버와의 통신을 위한 WebClient 설정을 제공합니다.
 * 글자인식 및 채점 API와의 연동에 사용됩니다.</p>
 * 
 * @author 이룸클래스 
 * @since 2025
 */
@Configuration
@ConfigurationProperties(prefix = "ai.server")
@Data
@Slf4j
public class AiServerConfig {
    
    /**
     * AI 서버 기본 URL
     */
    private String baseUrl = "http://localhost:8000";
    
    /**
     * 연결 타임아웃 (초)
     */
    private int connectTimeout = 30;
    
    /**
     * 응답 타임아웃 (초)
     */
    private int responseTimeout = 60;
    
    /**
     * 최대 메모리 크기 (파일 업로드용, MB)
     */
    private int maxInMemorySize = 50;

    /**
     * AI 서버용 WebClient 빈 생성
     * 
     * @return 설정된 WebClient 인스턴스
     */
    @Bean("aiServerWebClient")
    public WebClient aiServerWebClient() {
        log.info("AI 서버 WebClient 설정 시작: baseUrl={}, connectTimeout={}초, responseTimeout={}초", 
                baseUrl, connectTimeout, responseTimeout);
        
        // 메모리 크기 증가 (파일 업로드용)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize * 1024 * 1024))
            .build();
        
        WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(strategies)
            .codecs(configurer -> {
                // 응답 타임아웃 설정
                configurer.defaultCodecs().maxInMemorySize(maxInMemorySize * 1024 * 1024);
            })
            .build();
            
        log.info("AI 서버 WebClient 설정 완료");
        return webClient;
    }
    
    /**
     * 글자인식 API 경로 반환
     * 
     * @return 글자인식 API 경로
     */
    public String getTextRecognitionPath() {
        return "/text-recognition";
    }
    
    /**
     * 채점 API 경로 반환
     * 
     * @return 채점 API 경로
     */
    public String getGradingPath() {
        return "/grading";
    }
}