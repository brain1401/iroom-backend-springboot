package com.iroomclass.springbackend.domain.textrecognition.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * AI 서버와 통신하는 클라이언트
 * 비동기 글자인식 작업을 AI 서버에 요청합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIServerClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${AI_SERVER_URL:http://localhost:8000}")
    private String aiServerUrl;
    
    /**
     * AI 서버에 비동기 글자인식 작업을 요청합니다.
     * 
     * @param file 업로드할 이미지 파일
     * @param callbackUrl 결과를 받을 콜백 URL  
     * @param useCache 캐시 사용 여부
     * @param useContentHash 콘텐츠 해시 사용 여부
     * @return AI 서버 응답 (job_id 포함)
     * @throws AIServerException AI 서버 통신 오류 시
     */
    public AIServerResponse submitAsync(MultipartFile file, String callbackUrl, Boolean useCache, Boolean useContentHash) {
        return submitAsyncWithRetry(file, callbackUrl, useCache, useContentHash, 1);
    }
    
    /**
     * AI 서버에 비동기 글자인식 작업을 요청합니다 (재시도 로직 포함).
     * 
     * @param file 업로드할 이미지 파일
     * @param callbackUrl 결과를 받을 콜백 URL  
     * @param useCache 캐시 사용 여부
     * @param useContentHash 콘텐츠 해시 사용 여부
     * @param attempt 현재 시도 횟수
     * @return AI 서버 응답 (job_id 포함)
     * @throws AIServerException AI 서버 통신 오류 시
     */
    private AIServerResponse submitAsyncWithRetry(MultipartFile file, String callbackUrl, Boolean useCache, Boolean useContentHash, int attempt) {
        String url = aiServerUrl + "/text-recognition/async/submit";
        
        log.info("AI 서버 비동기 요청 시작: url={}, callbackUrl={}, attempt={}/10", url, callbackUrl, attempt);
        
        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 멀티파트 요청 데이터 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 파일 데이터를 ByteArrayResource로 변환
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            // 멀티파트 폼에 파일과 개별 필드들 추가
            body.add("file", fileResource);
            body.add("callback_url", callbackUrl);
            if (useCache != null) {
                body.add("use_cache", useCache.toString());
            }
            if (useContentHash != null) {
                body.add("use_content_hash", useContentHash.toString());
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // AI 서버에 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            // 응답 처리
            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                String responseBody = response.getBody();
                log.info("AI 서버 요청 성공: response={}", responseBody);
                
                // JSON 파싱
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                return AIServerResponse.builder()
                    .jobId((String) responseMap.get("job_id"))
                    .status((String) responseMap.get("status"))
                    .callbackUrl((String) responseMap.get("callback_url"))
                    .build();
                    
            } else {
                throw new AIServerException("예상하지 못한 응답 코드: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류: {}", e.getMessage());
            return handleRetryableException("파일 처리 중 오류가 발생했습니다", e, file, callbackUrl, useCache, useContentHash, attempt);
            
        } catch (RestClientException e) {
            log.error("AI 서버 통신 오류: {}, attempt={}/10", e.getMessage(), attempt);
            return handleRetryableException("AI 서버 통신 중 오류가 발생했습니다", e, file, callbackUrl, useCache, useContentHash, attempt);
            
        } catch (Exception e) {
            log.error("AI 서버 요청 중 예상하지 못한 오류: {}, attempt={}/10", e.getMessage(), attempt);
            return handleRetryableException("AI 서버 요청 중 예상하지 못한 오류가 발생했습니다", e, file, callbackUrl, useCache, useContentHash, attempt);
        }
    }
    
    /**
     * 재시도 가능한 예외를 처리합니다 (지수 백오프 적용).
     * 
     * @param message 오류 메시지
     * @param exception 발생한 예외
     * @param file 업로드할 파일
     * @param callbackUrl 콜백 URL
     * @param useCache 캐시 사용 여부
     * @param useContentHash 콘텐츠 해시 사용 여부
     * @param attempt 현재 시도 횟수
     * @return AI 서버 응답 또는 예외 발생
     */
    private AIServerResponse handleRetryableException(String message, Exception exception, 
                                                     MultipartFile file, String callbackUrl, 
                                                     Boolean useCache, Boolean useContentHash, int attempt) {
        
        // 최대 10회 시도
        if (attempt >= 10) {
            log.error("AI 서버 요청 최종 실패: 최대 재시도 횟수 초과 (10회), 원본 오류: {}", exception.getMessage());
            throw new AIServerException(message + " - 최대 재시도 횟수 초과: " + exception.getMessage(), exception);
        }
        
        // 지수 백오프 계산: 2^attempt초 (2, 4, 8, 16, ..., 최대 1024초)
        long delaySeconds = Math.min((long) Math.pow(2, attempt), 1024);
        
        log.warn("AI 서버 요청 실패 - {}초 후 재시도: attempt={}/10, delay={}초, error={}", 
                delaySeconds, attempt, delaySeconds, exception.getMessage());
        
        try {
            Thread.sleep(delaySeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AIServerException("재시도 대기 중 인터럽트 발생: " + e.getMessage(), e);
        }
        
        // 재시도 실행
        return submitAsyncWithRetry(file, callbackUrl, useCache, useContentHash, attempt + 1);
    }
    
    /**
     * AI 서버 응답 DTO
     */
    public static class AIServerResponse {
        private final String jobId;
        private final String status;
        private final String callbackUrl;
        
        private AIServerResponse(String jobId, String status, String callbackUrl) {
            this.jobId = jobId;
            this.status = status;
            this.callbackUrl = callbackUrl;
        }
        
        public String getJobId() { return jobId; }
        public String getStatus() { return status; }
        public String getCallbackUrl() { return callbackUrl; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String jobId;
            private String status;
            private String callbackUrl;
            
            public Builder jobId(String jobId) {
                this.jobId = jobId;
                return this;
            }
            
            public Builder status(String status) {
                this.status = status;
                return this;
            }
            
            public Builder callbackUrl(String callbackUrl) {
                this.callbackUrl = callbackUrl;
                return this;
            }
            
            public AIServerResponse build() {
                return new AIServerResponse(jobId, status, callbackUrl);
            }
        }
    }
    
    /**
     * AI 서버 통신 예외
     */
    public static class AIServerException extends RuntimeException {
        public AIServerException(String message) {
            super(message);
        }
        
        public AIServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}