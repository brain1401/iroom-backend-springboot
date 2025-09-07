package com.iroomclass.springbackend.domain.textrecognition.service;

import com.iroomclass.springbackend.common.UUIDv7Generator;
import com.iroomclass.springbackend.domain.textrecognition.client.AIServerClient;
import com.iroomclass.springbackend.domain.textrecognition.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 텍스트 인식 서비스
 * AI 서버와의 통신을 통한 글자인식 작업을 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TextRecognitionService {
    
    private final AIServerClient aiServerClient;
    private final SseConnectionManager sseConnectionManager;
    
    // 작업 상태를 메모리에서 관리 (운영 환경에서는 Redis 사용 권장)
    private final Map<String, JobState> jobStates = new ConcurrentHashMap<>();
    private final Map<String, TextRecognitionAnswerResponse> jobResults = new ConcurrentHashMap<>();
    private final Map<String, BatchState> batchStates = new ConcurrentHashMap<>();
    
    // 폴링을 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    @Value("${server.port:3055}")
    private String serverPort;
    
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;
    
    // ==================== 1. 동기식 처리 ====================
    
    /**
     * 답안지 글자인식 (동기식)
     */
    public TextRecognitionAnswerResponse recognizeAnswerSheetSync(
            MultipartFile file, Boolean useCache, Boolean useContentHash) {
        
        validateFile(file);
        
        log.info("동기식 답안지 인식 시작: 파일명={}, 크기={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // AI 서버에 동기식 요청
            return aiServerClient.recognizeAnswerSheetSync(file, useCache, useContentHash);
            
        } catch (Exception e) {
            log.error("동기식 답안지 인식 실패: {}", e.getMessage(), e);
            throw new RuntimeException("답안지 인식 처리 중 오류가 발생했습니다", e);
        }
    }
    
    /**
     * 배치 글자인식 제출
     */
    public BatchTextRecognitionResponse submitBatchRecognition(BatchTextRecognitionRequest request) {
        
        // 파일 검증
        if (request.files() == null || request.files().isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        if (request.files().size() > 20) {
            throw new IllegalArgumentException("파일 개수는 최대 20개까지 가능합니다");
        }
        
        for (MultipartFile file : request.files()) {
            validateFile(file);
        }
        
        String batchId = UUIDv7Generator.generateString();
        
        log.info("배치 글자인식 시작: batchId={}, 파일 개수={}", batchId, request.files().size());
        
        try {
            // AI 서버에 배치 요청
            BatchState batchState = new BatchState(batchId, request.files().size());
            batchStates.put(batchId, batchState);
            
            // AI 서버에 배치 요청 전송
            aiServerClient.submitBatch(request.files(), request.priority(), request.useCache(), batchId);
            
            // 진행률 모니터링 시작
            startBatchProgressMonitoring(batchId);
            
            return BatchTextRecognitionResponse.builder()
                .batchId(batchId)
                .progressStreamUrl("/api/text-recognition/batch/" + batchId + "/progress")
                .totalItems(request.files().size())
                .status("processing")
                .build();
                
        } catch (Exception e) {
            log.error("배치 글자인식 제출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("배치 처리 중 오류가 발생했습니다", e);
        }
    }
    
    // ==================== 2. 비동기 처리 ====================
    
    /**
     * 비동기 작업 제출
     */
    public AsyncTextRecognitionSubmitResponse submitAsyncRecognition(
            MultipartFile file, AsyncTextRecognitionSubmitRequest request) {
        
        validateFile(file);
        
        String jobId = UUIDv7Generator.generateString();
        
        log.info("비동기 작업 제출: jobId={}, 파일명={}", jobId, file.getOriginalFilename());
        
        // 작업 상태 초기화
        JobState jobState = JobState.create(jobId, file.getOriginalFilename(), 
                                           file.getSize(), request.callbackUrl());
        jobStates.put(jobId, jobState);
        
        try {
            // AI 서버에 비동기 요청
            AIServerClient.AIServerResponse response = aiServerClient.submitAsync(
                file, request.callbackUrl(), request.useCache(), request.priority());
            
            log.info("AI 서버 작업 제출 성공: jobId={}, aiJobId={}", jobId, response.getJobId());
            
            // JobState에 AI 서버 jobId 저장
            jobState = jobState.withAiJobId(response.getJobId());
            jobStates.put(jobId, jobState);
            
            // 폴링 시작 (콜백 백업)
            startPolling(jobId, response.getJobId());
            
            return AsyncTextRecognitionSubmitResponse.builder()
                .jobId(jobId)
                .status("submitted")
                .estimatedCompletionTime(LocalDateTime.now().plusMinutes(2))
                .callbackUrl(request.callbackUrl())
                .submittedAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("비동기 작업 제출 실패: {}", e.getMessage(), e);
            jobState = jobState.fail("작업 제출 실패: " + e.getMessage());
            jobStates.put(jobId, jobState);
            throw new RuntimeException("작업 제출 중 오류가 발생했습니다", e);
        }
    }
    
    /**
     * 작업 상태 조회
     */
    public JobStatusResponse getJobStatus(String jobId) {
        JobState state = jobStates.get(jobId);
        if (state == null) {
            throw new IllegalArgumentException("존재하지 않는 작업 ID: " + jobId);
        }
        
        return JobStatusResponse.builder()
            .jobId(jobId)
            .status(state.getStatus().name().toLowerCase())
            .createdAt(state.getCreatedAt())
            .completedAt(state.getCompletedAt())
            .build();
    }
    
    /**
     * 작업 결과 조회
     */
    public TextRecognitionAnswerResponse getJobResult(String jobId) {
        JobState state = jobStates.get(jobId);
        if (state == null) {
            throw new IllegalArgumentException("존재하지 않는 작업 ID: " + jobId);
        }
        
        if (!state.getStatus().isCompleted()) {
            throw new IllegalStateException("작업이 아직 완료되지 않았습니다: " + state.getStatus());
        }
        
        TextRecognitionAnswerResponse result = jobResults.get(jobId);
        if (result == null) {
            throw new IllegalStateException("결과를 찾을 수 없습니다");
        }
        
        return result;
    }
    
    /**
     * AI 서버 직접 상태 조회
     */
    public JobStatusResponse checkAIServerStatus(String jobId) {
        try {
            JobState localState = jobStates.get(jobId);
            if (localState == null) {
                throw new IllegalArgumentException("존재하지 않는 작업 ID: " + jobId);
            }
            
            // AI 서버에 직접 상태 조회 (AI 서버의 jobId 사용)
            String aiJobId = localState.getAiJobId();
            if (aiJobId == null) {
                log.warn("AI 서버 jobId가 없습니다: localJobId={}", jobId);
                return JobStatusResponse.builder()
                    .jobId(jobId)
                    .status(localState.getStatus().name().toLowerCase())
                    .createdAt(localState.getCreatedAt())
                    .completedAt(localState.getCompletedAt())
                    .build();
            }
            
            JobStatusResponse aiStatus = aiServerClient.getJobStatus(aiJobId);
            
            // 로컬 상태 업데이트
            if (aiStatus.status() != null) {
                JobStatus newStatus = JobStatus.fromString(aiStatus.status());
                if (newStatus != localState.getStatus()) {
                    localState = localState.updateStatus(newStatus);
                    jobStates.put(jobId, localState);
                }
            }
            
            return JobStatusResponse.builder()
                .jobId(jobId)
                .status(aiStatus.status())
                .createdAt(localState.getCreatedAt())
                .completedAt(aiStatus.completedAt())
                .build();
                
        } catch (Exception e) {
            log.error("AI 서버 상태 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 서버 상태 조회 중 오류가 발생했습니다", e);
        }
    }
    
    // ==================== 3. SSE 스트리밍 ====================
    
    /**
     * 배치 진행률 스트리밍
     */
    public SseEmitter streamBatchProgress(String batchId) {
        BatchState batchState = batchStates.get(batchId);
        if (batchState == null) {
            throw new IllegalArgumentException("존재하지 않는 배치 ID: " + batchId);
        }
        
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃
        
        // SSE 연결 관리
        sseConnectionManager.addConnection(batchId, emitter);
        
        // 초기 상태 전송
        try {
            BatchProgressEvent initialEvent = BatchProgressEvent.builder()
                .batchId(batchId)
                .progressPercentage(batchState.getProgressPercentage())
                .completedItems(batchState.getCompletedItems())
                .failedItems(batchState.getFailedItems())
                .totalItems(batchState.getTotalItems())
                .status(batchState.getStatus())
                .build();
                
            emitter.send(SseEmitter.event()
                .name("progress")
                .data(initialEvent));
                
        } catch (IOException e) {
            log.error("초기 진행률 전송 실패: {}", e.getMessage());
            sseConnectionManager.removeConnection(batchId);
        }
        
        return emitter;
    }
    
    // ==================== 4. 콜백 처리 ====================
    
    /**
     * AI 서버 콜백 처리
     */
    public void handleCallback(String jobId, AsyncTextRecognitionCallbackData callbackData) {
        log.info("콜백 처리 시작: jobId={}, status={}", jobId, callbackData.status());
        
        JobState state = jobStates.get(jobId);
        if (state == null) {
            log.warn("알 수 없는 작업에 대한 콜백: jobId={}", jobId);
            return;
        }
        
        // 상태 업데이트
        if ("completed".equalsIgnoreCase(callbackData.status())) {
            state = state.complete();
            jobStates.put(jobId, state);
            
            // 결과 저장
            if (callbackData.result() != null) {
                jobResults.put(jobId, callbackData.result());
            }
            
            // SSE 알림
            notifyCompletion(jobId, callbackData.result());
            
        } else if ("failed".equalsIgnoreCase(callbackData.status())) {
            String errorMsg = callbackData.error() != null ? 
                callbackData.error().errorMessage() : "알 수 없는 오류";
            state = state.fail(errorMsg);
            jobStates.put(jobId, state);
            
            // SSE 알림
            notifyFailure(jobId, errorMsg);
        }
        
        log.info("콜백 처리 완료: jobId={}, 최종상태={}", jobId, state.getStatus());
    }
    
    // ==================== 유틸리티 메서드 ====================
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        
        // 파일 크기 검증 (20MB)
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다");
        }
        
        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. JPEG, PNG, WEBP, GIF만 가능합니다.");
        }
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType.startsWith("image/") && 
               (contentType.contains("jpeg") || contentType.contains("jpg") || 
                contentType.contains("png") || contentType.contains("webp") || 
                contentType.contains("gif"));
    }
    
    private void startPolling(String jobId, String aiJobId) {
        // 30초마다 상태 확인 (최대 10분)
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                JobState state = jobStates.get(jobId);
                if (state == null || state.getStatus().isCompleted()) {
                    return; // 이미 완료됨
                }
                
                // 타임아웃 체크 (10분)
                if (state.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
                    state = state.fail("처리 시간 초과");
                    jobStates.put(jobId, state);
                    notifyFailure(jobId, "처리 시간 초과");
                    return;
                }
                
                // AI 서버 상태 조회
                JobStatusResponse aiStatus = checkAIServerStatus(jobId);
                
                // 완료된 경우 결과 가져오기
                if ("completed".equalsIgnoreCase(aiStatus.status())) {
                    try {
                        TextRecognitionAnswerResponse result = aiServerClient.getJobResult(aiJobId);
                        if (result != null) {
                            jobResults.put(jobId, result);
                            JobState updatedState = state.complete();
                            jobStates.put(jobId, updatedState);
                            notifyCompletion(jobId, result);
                            log.info("폴링으로 작업 완료 확인: jobId={}, aiJobId={}", jobId, aiJobId);
                        }
                    } catch (Exception ex) {
                        log.error("결과 가져오기 실패: jobId={}, error={}", jobId, ex.getMessage());
                    }
                } else if ("failed".equalsIgnoreCase(aiStatus.status())) {
                    state = state.fail("AI 서버 처리 실패");
                    jobStates.put(jobId, state);
                    notifyFailure(jobId, "AI 서버 처리 실패");
                }
                
            } catch (Exception e) {
                log.error("폴링 중 오류: jobId={}, error={}", jobId, e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private void startBatchProgressMonitoring(String batchId) {
        // 5초마다 진행률 업데이트
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                BatchState state = batchStates.get(batchId);
                if (state == null || "completed".equals(state.getStatus())) {
                    return;
                }
                
                // AI 서버에서 진행률 조회
                AIServerClient.BatchProgressResponse progress = aiServerClient.getBatchProgress(batchId);
                
                // 상태 업데이트
                state.updateProgress(progress.getCompletedItems(), progress.getFailedItems());
                
                // SSE 이벤트 전송
                BatchProgressEvent event = BatchProgressEvent.builder()
                    .batchId(batchId)
                    .progressPercentage(state.getProgressPercentage())
                    .completedItems(state.getCompletedItems())
                    .failedItems(state.getFailedItems())
                    .totalItems(state.getTotalItems())
                    .status(state.getStatus())
                    .build();
                    
                sseConnectionManager.sendEventToAll(batchId, event);
                
            } catch (Exception e) {
                log.error("배치 진행률 모니터링 중 오류: {}", e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    private void notifyCompletion(String jobId, TextRecognitionAnswerResponse result) {
        if (sseConnectionManager.hasConnection(jobId)) {
            TextRecognitionSseEvent event = TextRecognitionSseEvent.complete(jobId, result);
            sseConnectionManager.sendEvent(jobId, event);
        }
    }
    
    private void notifyFailure(String jobId, String errorMessage) {
        if (sseConnectionManager.hasConnection(jobId)) {
            TextRecognitionSseEvent event = TextRecognitionSseEvent.error(jobId, errorMessage);
            sseConnectionManager.sendEvent(jobId, event);
        }
    }
    
    /**
     * 작업 상태 내부 클래스
     */
    private static class BatchState {
        private final String batchId;
        private final int totalItems;
        private int completedItems;
        private int failedItems;
        private String status;
        
        public BatchState(String batchId, int totalItems) {
            this.batchId = batchId;
            this.totalItems = totalItems;
            this.completedItems = 0;
            this.failedItems = 0;
            this.status = "processing";
        }
        
        public void updateProgress(int completed, int failed) {
            this.completedItems = completed;
            this.failedItems = failed;
            if (completedItems + failedItems >= totalItems) {
                this.status = "completed";
            }
        }
        
        public double getProgressPercentage() {
            if (totalItems == 0) return 0;
            return ((double) (completedItems + failedItems) / totalItems) * 100;
        }
        
        // Getters
        public int getTotalItems() { return totalItems; }
        public int getCompletedItems() { return completedItems; }
        public int getFailedItems() { return failedItems; }
        public String getStatus() { return status; }
    }
}