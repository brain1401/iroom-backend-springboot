package com.iroomclass.springbackend.domain.textrecognition.service;

import com.iroomclass.springbackend.domain.textrecognition.dto.JobState;
import com.iroomclass.springbackend.domain.textrecognition.dto.JobStatus;
import com.iroomclass.springbackend.domain.textrecognition.dto.TextRecognitionSseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 콜백 타임아웃 처리 및 폴링 백업 메커니즘
 * 
 * <p>AI 서버에서 콜백을 보내지 않는 경우를 대비한 능동적 상태 확인 시스템</p>
 * 
 * <ul>
 *   <li>30초마다 콜백 미수신 job 탐지</li>
 *   <li>AI 서버 직접 상태 조회</li>  
 *   <li>5분 초과 시 강제 타임아웃 처리</li>
 *   <li>자동 복구 및 SSE 알림</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackTimeoutHandler {
    
    private final TextRecognitionService textRecognitionService;
    private final SseConnectionManager sseConnectionManager;
    private final RestTemplate restTemplate;
    
    @Value("${AI_SERVER_URL:http://localhost:8000}")
    private String aiServerUrl;
    
    // 안전 마진: 예상 완료시간 + 30초 후 폴링 시작
    private static final long SAFETY_MARGIN_SECONDS = 30;
    
    // 강제 타임아웃: 예상 완료시간 + 5분 후 타임아웃 처리
    private static final long FORCE_TIMEOUT_SECONDS = 5 * 60;
    
    /**
     * 30초마다 실행되는 콜백 타임아웃 감지 및 처리
     * 
     * <p>처리 단계:</p>
     * <ol>
     *   <li>콜백 미수신 job 식별</li>
     *   <li>AI 서버 직접 상태 조회</li>
     *   <li>상태에 따른 적절한 처리</li>
     *   <li>SSE 이벤트 전송</li>
     * </ol>
     */
    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    public void handleTimeoutJobs() {
        LocalDateTime now = LocalDateTime.now();
        
        // 텍스트 인식 서비스에서 관리하는 작업 상태들 가져오기
        ConcurrentHashMap<String, JobState> jobStates = textRecognitionService.getJobStates();
        
        if (jobStates.isEmpty()) {
            return;
        }
        
        log.debug("콜백 타임아웃 감지 시작: 확인할 작업 수={}", jobStates.size());
        
        jobStates.values().parallelStream()
            .filter(jobState -> shouldCheckForTimeout(jobState, now))
            .forEach(jobState -> processTimeoutJob(jobState, now));
    }
    
    /**
     * 타임아웃 확인이 필요한 작업인지 판단
     */
    private boolean shouldCheckForTimeout(JobState jobState, LocalDateTime now) {
        // 완료되거나 실패한 작업은 체크 불필요
        if (jobState.status() == JobStatus.COMPLETED || 
            jobState.status() == JobStatus.FAILED) {
            return false;
        }
        
        // 생성된지 30초 이상된 PROCESSING 상태 작업들
        Duration elapsed = Duration.between(jobState.createdAt(), now);
        return elapsed.getSeconds() > SAFETY_MARGIN_SECONDS;
    }
    
    /**
     * 타임아웃 의심 작업에 대한 처리
     */
    private void processTimeoutJob(JobState jobState, LocalDateTime now) {
        String jobId = jobState.jobId();
        Duration elapsed = Duration.between(jobState.createdAt(), now);
        
        log.info("타임아웃 의심 작업 처리 시작: jobId={}, 경과시간={}초", 
            jobId, elapsed.getSeconds());
        
        try {
            // 강제 타임아웃 확인 (5분 초과)
            if (elapsed.getSeconds() > FORCE_TIMEOUT_SECONDS) {
                handleForceTimeout(jobState, elapsed);
                return;
            }
            
            // AI 서버 직접 상태 조회
            String aiServerStatus = checkAiServerStatus(jobId);
            log.info("AI 서버 상태 조회 결과: jobId={}, status={}", jobId, aiServerStatus);
            
            // 상태에 따른 처리
            if ("completed".equals(aiServerStatus)) {
                handleMissedCallback(jobState);
            } else if ("timeout_suspected".equals(aiServerStatus)) {
                handleTimeoutSuspected(jobState, elapsed);
            } else if ("processing".equals(aiServerStatus)) {
                handleStillProcessing(jobState, elapsed);
            } else {
                handleUnknownStatus(jobState, aiServerStatus, elapsed);
            }
            
        } catch (Exception e) {
            log.error("타임아웃 작업 처리 중 오류: jobId={}, error={}", jobId, e.getMessage(), e);
            handlePollingError(jobState, e, elapsed);
        }
    }
    
    /**
     * AI 서버에 직접 상태 조회
     */
    private String checkAiServerStatus(String jobId) {
        try {
            String url = aiServerUrl + "/text-recognition/async/ai-server-status/" + jobId;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("overall_status")) {
                return (String) response.get("overall_status");
            }
            
            return "unknown";
            
        } catch (Exception e) {
            log.warn("AI 서버 상태 조회 실패: jobId={}, error={}", jobId, e.getMessage());
            return "query_failed";
        }
    }
    
    /**
     * 5분 초과 강제 타임아웃 처리
     */
    private void handleForceTimeout(JobState jobState, Duration elapsed) {
        String jobId = jobState.jobId();
        String message = String.format("작업 타임아웃 (경과시간: %d분)", elapsed.toMinutes());
        
        log.warn("강제 타임아웃 처리: jobId={}, 경과시간={}분", jobId, elapsed.toMinutes());
        
        // 작업 상태를 FAILED로 업데이트
        JobState failedState = jobState.fail(message);
        textRecognitionService.updateJobState(jobId, failedState);
        
        // SSE로 타임아웃 알림
        TextRecognitionSseEvent event = TextRecognitionSseEvent.failed(jobId, message);
        sseConnectionManager.sendEvent(jobId, event);
        
        // 정리 작업 예약
        textRecognitionService.scheduleCleanup(jobId);
    }
    
    /**
     * 콜백 누락 처리 (AI 서버에서는 완료되었으나 콜백 미수신)
     */
    private void handleMissedCallback(JobState jobState) {
        String jobId = jobState.jobId();
        
        log.warn("콜백 누락 감지: jobId={}", jobId);
        
        // AI 서버에 강제 콜백 재전송 요청 (가능한 경우)
        try {
            requestForcedCallback(jobId);
            log.info("강제 콜백 재전송 요청: jobId={}", jobId);
            
            // SSE로 복구 중 알림
            TextRecognitionSseEvent event = TextRecognitionSseEvent.statusChange(
                jobId, 
                JobStatus.PROCESSING, 
                "콜백 누락을 감지하여 복구 중입니다"
            );
            sseConnectionManager.sendEvent(jobId, event);
            
        } catch (Exception e) {
            log.error("강제 콜백 재전송 실패: jobId={}, error={}", jobId, e.getMessage());
            
            // 복구 실패 시 타임아웃 처리
            handleForceTimeout(jobState, Duration.between(jobState.createdAt(), LocalDateTime.now()));
        }
    }
    
    /**
     * 타임아웃 의심 상황 처리
     */
    private void handleTimeoutSuspected(JobState jobState, Duration elapsed) {
        String jobId = jobState.jobId();
        
        log.info("AI 서버에서 타임아웃 의심 보고: jobId={}, 경과시간={}초", jobId, elapsed.getSeconds());
        
        // SSE로 지연 알림
        TextRecognitionSseEvent event = TextRecognitionSseEvent.statusChange(
            jobId,
            JobStatus.PROCESSING,
            String.format("처리가 예상보다 오래 걸리고 있습니다 (경과: %d분)", elapsed.toMinutes())
        );
        sseConnectionManager.sendEvent(jobId, event);
    }
    
    /**
     * 여전히 처리 중인 상황
     */
    private void handleStillProcessing(JobState jobState, Duration elapsed) {
        String jobId = jobState.jobId();
        
        log.debug("AI 서버에서 여전히 처리 중: jobId={}, 경과시간={}초", jobId, elapsed.getSeconds());
        
        // 1분 이상 경과 시에만 SSE 알림 (너무 빈번한 알림 방지)
        if (elapsed.toMinutes() > 1) {
            TextRecognitionSseEvent event = TextRecognitionSseEvent.statusChange(
                jobId,
                JobStatus.PROCESSING,
                String.format("AI 서버에서 처리 중입니다 (경과: %d분)", elapsed.toMinutes())
            );
            sseConnectionManager.sendEvent(jobId, event);
        }
    }
    
    /**
     * 알 수 없는 상태 처리
     */
    private void handleUnknownStatus(JobState jobState, String status, Duration elapsed) {
        String jobId = jobState.jobId();
        
        log.warn("알 수 없는 AI 서버 상태: jobId={}, status={}, 경과시간={}초", 
            jobId, status, elapsed.getSeconds());
        
        // 3분 이상 알 수 없는 상태면 실패 처리
        if (elapsed.toMinutes() >= 3) {
            String message = String.format("알 수 없는 상태로 인한 타임아웃 (상태: %s)", status);
            handleForceTimeout(jobState, elapsed);
        }
    }
    
    /**
     * 폴링 중 오류 처리
     */
    private void handlePollingError(JobState jobState, Exception error, Duration elapsed) {
        String jobId = jobState.jobId();
        
        log.error("폴링 처리 중 오류: jobId={}, 경과시간={}초, error={}", 
            jobId, elapsed.getSeconds(), error.getMessage());
        
        // 2분 이상 지속적 오류 시 실패 처리
        if (elapsed.toMinutes() >= 2) {
            String message = String.format("폴링 오류로 인한 타임아웃: %s", error.getMessage());
            handleForceTimeout(jobState, elapsed);
        }
    }
    
    /**
     * AI 서버에 강제 콜백 재전송 요청
     */
    private void requestForcedCallback(String jobId) {
        // 실제 구현 시에는 AI 서버의 강제 콜백 API 호출
        // 현재는 로깅만 수행
        log.info("강제 콜백 재전송 요청: jobId={}", jobId);
        
        // 향후 구현 예시:
        // String url = aiServerUrl + "/text-recognition/async/force-callback/" + jobId;
        // restTemplate.postForEntity(url, null, String.class);
    }
    
    /**
     * 활성 작업 수 조회 (모니터링용)
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void logActiveJobCount() {
        int activeJobs = textRecognitionService.getActiveJobCount();
        int activeConnections = sseConnectionManager.getActiveConnectionCount();
        
        if (activeJobs > 0 || activeConnections > 0) {
            log.info("활성 작업 현황: jobs={}, connections={}", activeJobs, activeConnections);
        }
    }
}