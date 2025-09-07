package com.iroomclass.springbackend.domain.textrecognition.service;

import com.iroomclass.springbackend.common.UUIDv7Generator;
import com.iroomclass.springbackend.domain.textrecognition.client.AIServerClient;
import com.iroomclass.springbackend.domain.textrecognition.dto.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 텍스트 인식 서비스
 * AI 서버와의 비동기 통신을 통한 글자인식 작업을 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TextRecognitionService {
    
    private final AIServerClient aiServerClient;
    private final SseConnectionManager sseConnectionManager;
    
    // 작업 상태를 메모리에서 관리 (운영 환경에서는 Redis 사용 권장)
    private final ConcurrentHashMap<String, JobState> jobStates = new ConcurrentHashMap<>();
    
    @Value("${server.port:3057}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:/api}")  
    private String contextPath;
    
    /**
     * 텍스트 인식 작업을 제출합니다.
     * 
     * @param request 텍스트 인식 요청
     * @return 작업 응답 (jobId 포함)
     */
    public TextRecognitionJobResponse submitTextRecognition(TextRecognitionSubmitRequest request) {
        // 파일 검증
        validateFile(request.file());
        
        // 고유한 작업 ID 생성 (JUG 라이브러리로 UUID v7 생성)
        String jobId = UUIDv7Generator.generateString();
        
        log.info("텍스트 인식 작업 시작: jobId={}, filename={}", jobId, request.file().getOriginalFilename());
        
        // 콜백 URL 생성
        String callbackUrl = buildCallbackUrl(jobId);
        
        // 작업 상태 초기화
        JobState jobState = JobState.create(
            jobId,
            request.file().getOriginalFilename(),
            request.file().getSize(),
            callbackUrl
        );
        jobStates.put(jobId, jobState);
        
        try {
            // AI 서버에 비동기 요청 전송
            AIServerClient.AIServerResponse aiResponse = aiServerClient.submitAsync(
                request.file(),
                callbackUrl,
                request.useCache(),
                request.useContentHash()
            );
            
            log.info("AI 서버 요청 성공: jobId={}, aiJobId={}", jobId, aiResponse.getJobId());
            
            // 상태를 PROCESSING으로 업데이트
            jobState = jobState.updateStatus(JobStatus.PROCESSING);
            jobStates.put(jobId, jobState);
            
            // SSE 연결이 있다면 상태 알림
            if (sseConnectionManager.hasConnection(jobId)) {
                TextRecognitionSseEvent event = TextRecognitionSseEvent.statusChange(
                    jobId, 
                    JobStatus.PROCESSING, 
                    "AI 서버에서 글자인식을 처리하고 있습니다"
                );
                sseConnectionManager.sendEvent(jobId, event);
            }
            
            return TextRecognitionJobResponse.success(jobId);
            
        } catch (AIServerClient.AIServerException e) {
            log.error("AI 서버 요청 실패: jobId={}, error={}", jobId, e.getMessage());
            
            // 작업 상태를 FAILED로 업데이트
            jobState = jobState.fail("AI 서버 요청 실패: " + e.getMessage());
            jobStates.put(jobId, jobState);
            
            // SSE로 실패 알림
            if (sseConnectionManager.hasConnection(jobId)) {
                TextRecognitionSseEvent event = TextRecognitionSseEvent.failed(jobId, e.getMessage());
                sseConnectionManager.sendEvent(jobId, event);
            }
            
            throw new TextRecognitionException("글자인식 요청 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * AI 서버로부터 콜백을 처리합니다.
     * 
     * @param jobId 작업 ID
     * @param callbackRequest 콜백 요청 데이터
     */
    /**
     * SSE 연결 구독
     * 
     * @param jobId 작업 ID
     * @return SSE 이미터
     */
    public SseEmitter subscribeToJob(String jobId) {
        return sseConnectionManager.createConnection(jobId);
    }

    /**
     * AI 서버 콜백 처리
     * 
     * @param callbackRequest AI 서버 콜백 요청
     */
    public void handleCallback(AIServerCallbackRequest callbackRequest) {
        String jobId = callbackRequest.jobId();
        log.info("AI 서버 콜백 수신: jobId={}, status={}", jobId, callbackRequest.status());
        
        JobState jobState = jobStates.get(jobId);
        if (jobState == null) {
            log.warn("알 수 없는 작업 ID의 콜백: jobId={}", jobId);
            return;
        }
        
        try {
            TextRecognitionSseEvent sseEvent;
            
            if (callbackRequest.isSuccessful()) {
                // 성공 처리 - RecognizedAnswer를 AnswerDto로, metadata를 MetadataDto로 변환
                List<AnswerDto> answerDtos = callbackRequest.convertToAnswerDtos();
                MetadataDto metadataDto = callbackRequest.convertToMetadataDto();
                jobState = jobState.complete(answerDtos, metadataDto);
                jobStates.put(jobId, jobState);
                
                sseEvent = TextRecognitionSseEvent.completed(jobId, answerDtos, metadataDto);
                log.info("텍스트 인식 완료: jobId={}, 답안 수={}", jobId, 
                    callbackRequest.answers() != null ? callbackRequest.answers().size() : 0);
                
            } else {
                // 실패 처리
                String errorMsg = callbackRequest.hasError() ? callbackRequest.errorMessage() : "알 수 없는 오류";
                jobState = jobState.fail(errorMsg);
                jobStates.put(jobId, jobState);
                
                sseEvent = TextRecognitionSseEvent.failed(jobId, errorMsg);
                log.error("텍스트 인식 실패: jobId={}, error={}", jobId, errorMsg);
            }
            
            // SSE로 결과 전송
            sseConnectionManager.sendEvent(jobId, sseEvent);
            
            // 작업 완료 후 일정 시간 후 상태 정리 (메모리 관리)
            scheduleJobCleanup(jobId);
            
        } catch (Exception e) {
            log.error("콜백 처리 중 오류: jobId={}, error={}", jobId, e.getMessage(), e);
        }
    }
    
    /**
     * 작업 상태를 조회합니다.
     * 
     * @param jobId 작업 ID
     * @return 작업 상태 (선택적)
     */
    public Optional<JobState> getJobState(String jobId) {
        return Optional.ofNullable(jobStates.get(jobId));
    }
    
    /**
     * 활성 작업 수를 반환합니다.
     * 
     * @return 활성 작업 수
     */
    public int getActiveJobCount() {
        return jobStates.size();
    }
    
    /**
     * 모든 작업 상태를 반환합니다 (CallbackTimeoutHandler용).
     * 
     * @return 작업 상태 맵
     */
    public ConcurrentHashMap<String, JobState> getJobStates() {
        return jobStates;
    }
    
    /**
     * 작업 상태를 업데이트합니다 (CallbackTimeoutHandler용).
     * 
     * @param jobId 작업 ID
     * @param newState 새로운 상태
     */
    public void updateJobState(String jobId, JobState newState) {
        jobStates.put(jobId, newState);
        log.debug("작업 상태 업데이트: jobId={}, status={}", jobId, newState.status());
    }
    
    /**
     * 작업 정리를 예약합니다 (CallbackTimeoutHandler용).
     * 
     * @param jobId 작업 ID
     */
    public void scheduleCleanup(String jobId) {
        scheduleJobCleanup(jobId);
    }
    
    /**
     * 파일을 검증합니다.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TextRecognitionException("파일이 비어있습니다");
        }
        
        // 파일 크기 검증 (20MB)
        long maxSize = 20 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new TextRecognitionException("파일 크기가 너무 큽니다 (최대 20MB)");
        }
        
        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new TextRecognitionException("지원하지 않는 파일 형식입니다 (JPEG, PNG, WEBP, GIF만 지원)");
        }
        
        log.debug("파일 검증 완료: filename={}, size={}, type={}", 
            file.getOriginalFilename(), file.getSize(), contentType);
    }
    
    /**
     * 유효한 이미지 타입인지 확인합니다.
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/gif");
    }
    
    /**
     * 콜백 URL을 생성합니다.
     */
    private String buildCallbackUrl(String jobId) {
        return String.format("http://localhost:%s%s/text-recognition/callback/%s", 
            serverPort, contextPath, jobId);
    }
    
    /**
     * 작업 완료 후 정리를 예약합니다.
     * 실제 운영에서는 스케줄러를 사용하거나 Redis TTL을 활용
     */
    private void scheduleJobCleanup(String jobId) {
        // 30분 후 작업 상태 정리 (메모리 절약)
        // 실제로는 @Scheduled 어노테이션이나 별도 스케줄러 사용 권장
        new Thread(() -> {
            try {
                Thread.sleep(30 * 60 * 1000L); // 30분 대기
                jobStates.remove(jobId);
                log.debug("작업 상태 정리 완료: jobId={}", jobId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 텍스트 인식 서비스 예외
     */
    public static class TextRecognitionException extends RuntimeException {
        public TextRecognitionException(String message) {
            super(message);
        }
        
        public TextRecognitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}