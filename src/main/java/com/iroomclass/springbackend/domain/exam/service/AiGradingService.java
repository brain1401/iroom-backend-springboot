package com.iroomclass.springbackend.domain.exam.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.iroomclass.springbackend.config.AiServerConfig;
import com.iroomclass.springbackend.domain.exam.dto.answer.AiGradingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 채점 서비스
 * 
 * AI 서버와 연동하여 시험 제출 답안의 채점 기능을 제공합니다.
 * 객관식은 자동 채점, 주관식은 AI 보조 채점을 수행합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiGradingService {

    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;
    
    private final AiServerConfig aiServerConfig;

    /**
     * 단일 제출 채점
     * 
     * @param submissionId 제출 ID
     * @return AI 채점 결과
     */
    public AiGradingResponse gradeSubmission(UUID submissionId) {
        return gradeSubmission(submissionId, new GradingRequest(submissionId, false, null));
    }

    /**
     * 단일 제출 채점 (옵션 포함)
     * 
     * @param submissionId 제출 ID
     * @param request 채점 요청 옵션
     * @return AI 채점 결과
     */
    public AiGradingResponse gradeSubmission(UUID submissionId, GradingRequest request) {
        log.info("AI 채점 시작: submissionId={}, forceRegrade={}", submissionId, request.forceRegrade());

        try {
            // AI 서버 호출
            AiGradingResponse response = aiServerWebClient
                .post()
                .uri(aiServerConfig.getGradingPath() + "/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiGradingResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .block();

            if (response == null) {
                log.warn("AI 서버 채점 응답이 null입니다: submissionId={}", submissionId);
                throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다");
            }

            log.info("AI 채점 완료: submissionId={}, 총점={}/{}, 상태={}", 
                    submissionId, response.totalScore(), response.maxTotalScore(), response.status());
            
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 채점 호출 실패: submissionId={}, HTTP Status={}, 응답 본문={}", 
                     submissionId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 채점 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 채점 중 예상치 못한 오류: submissionId={}, 오류={}", submissionId, e.getMessage());
            throw new RuntimeException("AI 채점 실패: " + e.getMessage());
        }
    }

    /**
     * 채점 결과 조회
     * 
     * @param submissionId 제출 ID
     * @return AI 채점 결과
     */
    public AiGradingResponse getGradingResult(UUID submissionId) {
        log.info("AI 채점 결과 조회 시작: submissionId={}", submissionId);

        try {
            // AI 서버 호출
            AiGradingResponse response = aiServerWebClient
                .get()
                .uri(aiServerConfig.getGradingPath() + "/" + submissionId)
                .retrieve()
                .bodyToMono(AiGradingResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .block();

            if (response == null) {
                log.warn("AI 서버 채점 결과 조회 응답이 null입니다: submissionId={}", submissionId);
                throw new RuntimeException("채점 결과를 찾을 수 없습니다");
            }

            log.info("AI 채점 결과 조회 완료: submissionId={}, 상태={}", submissionId, response.status());
            
            return response;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("채점 결과를 찾을 수 없음: submissionId={}", submissionId);
                throw new RuntimeException("채점 결과를 찾을 수 없습니다");
            }
            
            log.error("AI 서버 채점 결과 조회 실패: submissionId={}, HTTP Status={}, 응답 본문={}", 
                     submissionId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 채점 결과 조회 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 채점 결과 조회 중 예상치 못한 오류: submissionId={}, 오류={}", submissionId, e.getMessage());
            throw new RuntimeException("채점 결과 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 배치 채점 처리
     * 
     * @param request 배치 채점 요청
     * @return 배치 채점 결과
     */
    public BatchGradingResponse gradeBatchSubmissions(BatchGradingRequest request) {
        log.info("AI 배치 채점 시작: 제출 개수={}, 우선순위={}", request.submissionIds().size(), request.priority());

        try {
            // AI 서버 호출
            BatchGradingResponse response = aiServerWebClient
                .post()
                .uri(aiServerConfig.getGradingPath() + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BatchGradingResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout() * 2)) // 배치는 더 오래 걸릴 수 있음
                .block();

            if (response == null) {
                log.warn("AI 서버 배치 채점 응답이 null입니다");
                throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다");
            }

            log.info("AI 배치 채점 완료: batchId={}, 총 제출={}, 성공={}, 실패={}", 
                    response.batchId(), response.totalSubmissions(), 
                    response.successfulGradings(), response.failedGradings());
            
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 배치 채점 호출 실패: HTTP Status={}, 응답 본문={}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 배치 채점 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 배치 채점 중 예상치 못한 오류: {}", e.getMessage());
            throw new RuntimeException("AI 배치 채점 실패: " + e.getMessage());
        }
    }

    /**
     * 채점 시스템 통계 조회
     * 
     * @return 채점 통계
     */
    public Object getGradingStats() {
        log.info("AI 채점 통계 조회 시작");

        try {
            // AI 서버 호출
            Object response = aiServerWebClient
                .get()
                .uri(aiServerConfig.getGradingPath() + "/stats")
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .block();

            if (response == null) {
                log.warn("AI 서버 채점 통계 조회 응답이 null입니다");
                throw new RuntimeException("채점 통계를 가져올 수 없습니다");
            }

            log.info("AI 채점 통계 조회 완료");
            return response;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 채점 통계 조회 실패: HTTP Status={}, 응답 본문={}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 채점 통계 조회 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 채점 통계 조회 중 예상치 못한 오류: {}", e.getMessage());
            throw new RuntimeException("채점 통계 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 채점 요청 DTO
     */
    public record GradingRequest(
        UUID submissionId,
        Boolean forceRegrade,
        Object gradingOptions
    ) {
        public GradingRequest {
            if (submissionId == null) {
                throw new IllegalArgumentException("submissionId는 필수입니다");
            }
            if (forceRegrade == null) {
                forceRegrade = false;
            }
        }
    }

    /**
     * 배치 채점 요청 DTO
     */
    public record BatchGradingRequest(
        List<UUID> submissionIds,
        Boolean forceRegrade,
        Object gradingOptions,
        Integer priority
    ) {
        public BatchGradingRequest {
            if (submissionIds == null || submissionIds.isEmpty()) {
                throw new IllegalArgumentException("submissionIds는 필수이며 비어있을 수 없습니다");
            }
            if (submissionIds.size() > 50) {
                throw new IllegalArgumentException("한 번에 최대 50개까지만 처리할 수 있습니다");
            }
            if (forceRegrade == null) {
                forceRegrade = false;
            }
            if (priority == null) {
                priority = 3;
            }
        }
    }

    /**
     * 배치 채점 결과 DTO
     */
    public record BatchGradingResponse(
        UUID batchId,
        Integer totalSubmissions,
        Integer successfulGradings,
        Integer failedGradings,
        List<AiGradingResponse> results,
        Integer totalProcessingTimeMs,
        Integer averageProcessingTimeMs,
        String completedAt
    ) {}
}