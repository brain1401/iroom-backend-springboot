package com.iroomclass.springbackend.domain.textrecognition.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iroomclass.springbackend.domain.textrecognition.dto.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 서버 클라이언트
 * AI 서버와의 통신을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIServerClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;
    
    // ==================== 1. 동기식 엔드포인트 ====================
    
    /**
     * 답안지 글자인식 (동기식)
     */
    public TextRecognitionAnswerResponse recognizeAnswerSheetSync(
            MultipartFile file, Boolean useCache, Boolean useContentHash) {
        
        String url = aiServerUrl + "/text-recognition/answer-sheet";
        
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("use_cache", useCache.toString());
            body.add("use_content_hash", useContentHash.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<TextRecognitionAnswerResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, TextRecognitionAnswerResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("동기식 답안지 인식 성공");
                return response.getBody();
            } else {
                throw new AIServerException("예상치 못한 응답: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류: {}", e.getMessage());
            throw new AIServerException("파일 처리 중 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("AI 서버 요청 실패: {}", e.getMessage(), e);
            throw new AIServerException("AI 서버 요청이 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 배치 글자인식 제출
     */
    public BatchSubmitResponse submitBatch(List<MultipartFile> files, Integer priority, 
                                          Boolean useCache, String batchId) {
        
        String url = aiServerUrl + "/text-recognition/batch";
        
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 파일들 추가
            for (MultipartFile file : files) {
                body.add("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }
            
            body.add("priority", priority.toString());
            body.add("use_cache", useCache.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<BatchSubmitResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, BatchSubmitResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("배치 제출 성공: batchId={}", response.getBody().batchId);
                return response.getBody();
            } else {
                throw new AIServerException("배치 제출 실패: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류: {}", e.getMessage());
            throw new AIServerException("파일 처리 중 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("배치 제출 실패: {}", e.getMessage(), e);
            throw new AIServerException("배치 제출이 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    // ==================== 2. 비동기 엔드포인트 ====================
    
    /**
     * 비동기 작업 제출
     */
    public AIServerResponse submitAsync(MultipartFile file, String callbackUrl, 
                                       Boolean useCache, Integer priority) {
        
        String url = aiServerUrl + "/text-recognition/async/submit";
        
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("callback_url", callbackUrl);
            body.add("priority", priority != null ? priority.toString() : "5");
            body.add("use_cache", useCache.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<AIServerResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, AIServerResponse.class);
            
            if (response.getStatusCode() == HttpStatus.ACCEPTED && response.getBody() != null) {
                log.info("비동기 작업 제출 성공: jobId={}", response.getBody().jobId);
                return response.getBody();
            } else {
                throw new AIServerException("비동기 작업 제출 실패: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류: {}", e.getMessage());
            throw new AIServerException("파일 처리 중 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("비동기 작업 제출 실패: {}", e.getMessage(), e);
            throw new AIServerException("비동기 작업 제출이 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 작업 상태 조회
     */
    public JobStatusResponse getJobStatus(String jobId) {
        String url = aiServerUrl + "/text-recognition/async/status/" + jobId;
        
        try {
            ResponseEntity<JobStatusResponse> response = restTemplate.getForEntity(
                url, JobStatusResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new AIServerException("작업 상태 조회 실패: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("작업 상태 조회 실패: jobId={}, error={}", jobId, e.getMessage());
            throw new AIServerException("작업 상태 조회가 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 작업 결과 조회
     */
    public TextRecognitionAnswerResponse getJobResult(String jobId) {
        String url = aiServerUrl + "/text-recognition/async/result/" + jobId;
        
        try {
            ResponseEntity<AIJobResultWrapper> response = restTemplate.getForEntity(
                url, AIJobResultWrapper.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().result;
            } else {
                throw new AIServerException("작업 결과 조회 실패: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("작업 결과 조회 실패: jobId={}, error={}", jobId, e.getMessage());
            throw new AIServerException("작업 결과 조회가 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * AI 서버 결과 래퍼 클래스
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AIJobResultWrapper {
        private String jobId;
        private String status;
        private TextRecognitionAnswerResponse result;
        private LocalDateTime completedAt;
    }
    
    /**
     * AI 서버 직접 상태 조회
     */
    public JobStatusResponse checkAIServerStatus(String jobId) {
        String url = aiServerUrl + "/text-recognition/async/ai-server-status/" + jobId;
        
        try {
            ResponseEntity<JobStatusResponse> response = restTemplate.getForEntity(
                url, JobStatusResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new AIServerException("AI 서버 상태 조회 실패: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("AI 서버 상태 조회 실패: jobId={}, error={}", jobId, e.getMessage());
            throw new AIServerException("AI 서버 상태 조회가 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    // ==================== 3. 배치 진행률 ====================
    
    /**
     * 배치 진행률 조회
     */
    public BatchProgressResponse getBatchProgress(String batchId) {
        // SSE 대신 폴링용 엔드포인트가 필요한 경우 구현
        // 현재는 SSE로 직접 스트리밍하므로 이 메서드는 placeholder
        return BatchProgressResponse.builder()
            .batchId(batchId)
            .completedItems(0)
            .failedItems(0)
            .totalItems(0)
            .progressPercentage(0.0)
            .status("processing")
            .build();
    }
    
    // ==================== Response DTOs ====================
    
    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AIServerResponse {
        private String jobId;
        private String status;
        private String callbackUrl;
        private LocalDateTime submittedAt;
        
        public String getJobId() { return jobId; }
    }
    
    @Data
    @Builder
    public static class BatchSubmitResponse {
        private String batchId;
        private String progressStreamUrl;
        private Integer totalItems;
        private String status;
    }
    
    @Data
    @Builder
    public static class BatchProgressResponse {
        private String batchId;
        private Double progressPercentage;
        private Integer completedItems;
        private Integer failedItems;
        private Integer totalItems;
        private String status;
        
        public Integer getCompletedItems() { return completedItems; }
        public Integer getFailedItems() { return failedItems; }
    }
    
    /**
     * AI 서버 예외 클래스
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