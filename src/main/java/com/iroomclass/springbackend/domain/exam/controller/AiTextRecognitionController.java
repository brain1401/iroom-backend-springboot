package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.config.AiServerConfig;
import com.iroomclass.springbackend.domain.exam.dto.answer.AiServerResponse;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * AI 텍스트 인식 프록시 컨트롤러
 * 
 * <p>AI 서버의 텍스트 인식 API를 프록시하는 컨트롤러입니다.
 * 실제 처리는 AI 서버에서 수행하고, Spring Boot는 단순히 요청을 전달합니다.</p>
 */
@RestController
@RequestMapping("/api/ai/text-recognition")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 텍스트 인식 API", description = "AI 서버 텍스트 인식 기능 프록시 API")
public class AiTextRecognitionController {
    
    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;
    private final AiServerConfig aiServerConfig;
    
    /**
     * 답안지 텍스트 인식
     * 
     * @param file 인식할 답안지 이미지 파일
     * @return AI 서버 인식 결과
     */
    @PostMapping(value = "/answer-sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "답안지 텍스트 인식",
        description = "업로드된 답안지 이미지에서 텍스트를 인식하여 구조화된 데이터로 반환합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "인식 성공",
                content = @Content(schema = @Schema(implementation = AiServerResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<AiServerResponse>> recognizeAnswerSheet(
            @Parameter(description = "답안지 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file) {
        
        log.info("답안지 텍스트 인식 요청: 파일명={}, 크기={}bytes", file.getOriginalFilename(), file.getSize());
        
        return aiServerWebClient.post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/answer-sheet")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(AiServerResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 답안지 인식 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("답안지 인식 처리 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 배치 텍스트 인식
     * 
     * @param files 인식할 이미지 파일들
     * @return 배치 인식 결과
     */
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "배치 텍스트 인식",
        description = "여러 이미지 파일을 한번에 처리하여 텍스트를 인식합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "배치 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Object>> recognizeBatch(
            @Parameter(description = "인식할 이미지 파일들", required = true)
            @RequestPart("files") MultipartFile[] files) {
        
        log.info("배치 텍스트 인식 요청: 파일 개수={}", files.length);
        
        return aiServerWebClient.post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/batch")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("files", files))
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout() * 2)) // 배치 처리는 더 긴 타임아웃
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 배치 인식 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("배치 인식 처리 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 인식 메트릭 조회
     * 
     * @return AI 서버 메트릭 정보
     */
    @GetMapping("/metrics")
    @Operation(
        summary = "인식 메트릭 조회",
        description = "AI 서버의 텍스트 인식 성능 메트릭을 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "메트릭 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> getMetrics() {
        
        log.info("인식 메트릭 조회 요청");
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/metrics")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 메트릭 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("메트릭 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 대시보드 정보 조회
     * 
     * @return AI 서버 대시보드 데이터
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "대시보드 정보 조회",
        description = "AI 서버의 텍스트 인식 대시보드 데이터를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "대시보드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> getDashboard() {
        
        log.info("대시보드 정보 조회 요청");
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/dashboard")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 대시보드 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("대시보드 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 캐시 무효화
     * 
     * @return 캐시 무효화 결과
     */
    @DeleteMapping("/cache/invalidate")
    @Operation(
        summary = "캐시 무효화",
        description = "AI 서버의 텍스트 인식 관련 캐시를 무효화합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "캐시 무효화 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
        }
    )
    public Mono<ResponseEntity<Map>> invalidateCache() {
        
        log.info("캐시 무효화 요청");
        
        return aiServerWebClient.delete()
                .uri(aiServerConfig.getTextRecognitionPath() + "/cache/invalidate")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 캐시 무효화 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("캐시 무효화 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * AI 서버 상태 확인
     * 
     * @return AI 서버 헬스 체크 결과
     */
    @GetMapping("/health")
    @Operation(
        summary = "AI 서버 상태 확인",
        description = "AI 서버의 텍스트 인식 서비스 상태를 확인합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "상태 확인 성공"),
            @ApiResponse(responseCode = "503", description = "AI 서버 서비스 불가")
        }
    )
    public Mono<ResponseEntity<Map>> checkHealth() {
        
        log.info("AI 서버 상태 확인 요청");
        
        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10)) // 헬스 체크는 짧은 타임아웃
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.warn("AI 서버 상태 확인 실패: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("상태 확인 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.status(503).build());
                });
    }
}