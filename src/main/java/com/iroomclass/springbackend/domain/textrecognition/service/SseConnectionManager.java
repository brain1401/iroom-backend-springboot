package com.iroomclass.springbackend.domain.textrecognition.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.domain.textrecognition.dto.TextRecognitionSseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 연결 관리자
 * 텍스트 인식 작업의 실시간 상태 업데이트를 위한 SSE 연결을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseConnectionManager {
    
    private final ObjectMapper objectMapper;
    
    // jobId별 SSE 연결을 저장하는 맵
    private final ConcurrentHashMap<String, SseEmitter> connections = new ConcurrentHashMap<>();
    
    // SSE 연결 타임아웃 (30분)
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * 새로운 SSE 연결을 생성합니다.
     * 
     * @param jobId 작업 고유 식별자
     * @return SseEmitter 객체
     */
    public SseEmitter createConnection(String jobId) {
        log.info("SSE 연결 생성: jobId={}", jobId);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 연결 종료 시 정리
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: jobId={}", jobId);
            removeConnection(jobId);
        });
        
        // 연결 타임아웃 시 정리
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃: jobId={}", jobId);
            removeConnection(jobId);
        });
        
        // 연결 오류 시 정리
        emitter.onError((ex) -> {
            log.error("SSE 연결 오류: jobId={}, error={}", jobId, ex.getMessage());
            removeConnection(jobId);
        });
        
        // 연결을 맵에 저장
        connections.put(jobId, emitter);
        
        // 초기 연결 확인 메시지 전송
        sendInitialMessage(jobId, emitter);
        
        return emitter;
    }
    
    /**
     * 특정 작업에 이벤트를 전송합니다.
     * 
     * @param jobId 작업 고유 식별자
     * @param event 전송할 이벤트
     */
    public void sendEvent(String jobId, TextRecognitionSseEvent event) {
        SseEmitter emitter = connections.get(jobId);
        if (emitter == null) {
            log.warn("SSE 연결을 찾을 수 없음: jobId={}", jobId);
            return;
        }
        
        try {
            String eventData = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                .name("textRecognition")
                .data(eventData));
                
            log.debug("SSE 이벤트 전송 성공: jobId={}, eventType={}", jobId, event.eventType());
            
            // 완료 이벤트인 경우 연결 종료
            if ("COMPLETED".equals(event.eventType()) || "FAILED".equals(event.eventType())) {
                emitter.complete();
            }
            
        } catch (IOException e) {
            log.error("SSE 이벤트 전송 실패: jobId={}, error={}", jobId, e.getMessage());
            removeConnection(jobId);
        }
    }
    
    /**
     * SSE 연결을 제거합니다.
     * 
     * @param jobId 작업 고유 식별자
     */
    public void removeConnection(String jobId) {
        SseEmitter emitter = connections.remove(jobId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE 연결 종료 중 오류: jobId={}, error={}", jobId, e.getMessage());
            }
        }
        log.debug("SSE 연결 제거: jobId={}", jobId);
    }
    
    /**
     * 활성 연결 수를 반환합니다.
     * 
     * @return 활성 연결 수
     */
    public int getActiveConnectionCount() {
        return connections.size();
    }
    
    /**
     * 특정 작업에 연결이 존재하는지 확인합니다.
     * 
     * @param jobId 작업 고유 식별자
     * @return 연결 존재 여부
     */
    public boolean hasConnection(String jobId) {
        return connections.containsKey(jobId);
    }
    
    /**
     * 초기 연결 확인 메시지를 전송합니다.
     */
    private void sendInitialMessage(String jobId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                .name("connection")
                .data("SSE 연결이 성공적으로 생성되었습니다"));
                
            log.debug("SSE 초기 메시지 전송 성공: jobId={}", jobId);
            
        } catch (IOException e) {
            log.error("SSE 초기 메시지 전송 실패: jobId={}, error={}", jobId, e.getMessage());
            removeConnection(jobId);
        }
    }
}