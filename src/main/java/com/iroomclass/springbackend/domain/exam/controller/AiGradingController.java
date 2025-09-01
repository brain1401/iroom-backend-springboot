package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.config.AiServerConfig;
import com.iroomclass.springbackend.domain.exam.dto.answer.AiGradingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI 채점 프록시 컨트롤러
 * 
 * <p>AI 서버의 채점 API를 프록시하는 컨트롤러입니다.
 * 실제 채점 처리는 AI 서버에서 수행하고, Spring Boot는 단순히 요청을 전달합니다.</p>
 */
@RestController
@RequestMapping("/api/ai/grading")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 채점 API", description = "AI 서버 채점 기능 프록시 API")
public class AiGradingController {
    
    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;
    private final AiServerConfig aiServerConfig;
    
    /**
     * 단일 제출물 채점 요청
     * 
     * @param submissionId 제출물 ID
     * @param gradingRequest 채점 요청 데이터
     * @return 채점 처리 결과
     */
    @PostMapping("/{submissionId}")
    @Operation(
        summary = "단일 제출물 채점",
        description = "특정 제출물에 대한 AI 채점을 요청합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "채점 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "제출물을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> gradeSubmission(
            @Parameter(description = "제출물 ID", example = "12345", required = true)
            @PathVariable String submissionId,
            @Parameter(description = "채점 요청 데이터", required = true)
            @RequestBody Map<String, Object> gradingRequest) {
        
        log.info("단일 제출물 채점 요청: submissionId={}", submissionId);
        
        return aiServerWebClient.post()
                .uri(aiServerConfig.getGradingPath() + "/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(gradingRequest))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 채점 오류: submissionId={}, status={}, message={}", 
                             submissionId, ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("채점 처리 중 예외 발생: submissionId={}", submissionId, ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 단일 제출물 채점 결과 조회
     * 
     * @param submissionId 제출물 ID
     * @return 채점 결과
     */
    @GetMapping("/{submissionId}")
    @Operation(
        summary = "단일 제출물 채점 결과 조회",
        description = "특정 제출물의 AI 채점 결과를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "채점 결과 조회 성공",
                content = @Content(schema = @Schema(implementation = AiGradingResponse.class))),
            @ApiResponse(responseCode = "404", description = "제출물 또는 채점 결과를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<AiGradingResponse>> getGradingResult(
            @Parameter(description = "제출물 ID", example = "12345", required = true)
            @PathVariable String submissionId) {
        
        log.info("채점 결과 조회 요청: submissionId={}", submissionId);
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getGradingPath() + "/" + submissionId)
                .retrieve()
                .bodyToMono(AiGradingResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 채점 결과 조회 오류: submissionId={}, status={}, message={}", 
                             submissionId, ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("채점 결과 조회 중 예외 발생: submissionId={}", submissionId, ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 배치 채점 요청
     * 
     * @param batchRequest 배치 채점 요청 데이터
     * @return 배치 채점 처리 결과
     */
    @PostMapping("/batch")
    @Operation(
        summary = "배치 채점 요청",
        description = "여러 제출물에 대한 AI 채점을 일괄 요청합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "배치 채점 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> gradeBatch(
            @Parameter(description = "배치 채점 요청 데이터", required = true)
            @RequestBody Map<String, Object> batchRequest) {
        
        log.info("배치 채점 요청: 요청 데이터={}", batchRequest.keySet());
        
        return aiServerWebClient.post()
                .uri(aiServerConfig.getGradingPath() + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(batchRequest))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout() * 3)) // 배치 처리는 더 긴 타임아웃
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 배치 채점 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("배치 채점 처리 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 채점 통계 조회
     * 
     * @return 채점 통계 정보
     */
    @GetMapping("/stats")
    @Operation(
        summary = "채점 통계 조회",
        description = "AI 서버의 채점 통계 정보를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> getGradingStats() {
        
        log.info("채점 통계 조회 요청");
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getGradingPath() + "/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 채점 통계 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("채점 통계 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 채점 상태별 제출물 조회
     * 
     * @param status 채점 상태 (pending, processing, completed, failed)
     * @param limit 조회할 제출물 개수 (선택사항)
     * @return 상태별 제출물 목록
     */
    @GetMapping("/submissions")
    @Operation(
        summary = "채점 상태별 제출물 조회",
        description = "특정 채점 상태의 제출물 목록을 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "제출물 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<List>> getSubmissionsByStatus(
            @Parameter(description = "채점 상태", example = "completed", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "조회할 제출물 개수", example = "50", required = false)
            @RequestParam(required = false) Integer limit) {
        
        log.info("채점 상태별 제출물 조회: status={}, limit={}", status, limit);
        
        // 쿼리 파라미터 구성
        String uri = aiServerConfig.getGradingPath() + "/submissions";
        if (status != null || limit != null) {
            uri += "?";
            if (status != null) {
                uri += "status=" + status;
                if (limit != null) {
                    uri += "&limit=" + limit;
                }
            } else if (limit != null) {
                uri += "limit=" + limit;
            }
        }
        
        return aiServerWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 제출물 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("제출물 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 채점 큐 상태 조회
     * 
     * @return 채점 큐 상태 정보
     */
    @GetMapping("/queue/status")
    @Operation(
        summary = "채점 큐 상태 조회",
        description = "AI 서버의 채점 큐 상태를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "큐 상태 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> getQueueStatus() {
        
        log.info("채점 큐 상태 조회 요청");
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getGradingPath() + "/queue/status")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 큐 상태 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("큐 상태 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}