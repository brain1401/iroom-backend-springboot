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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI 서비스 통합 컨트롤러
 * 
 * <p>
 * AI 서버의 채점 및 텍스트 인식 API를 통합 관리하는 컨트롤러입니다.
 * 실제 AI 처리는 외부 AI 서버에서 수행하고, Spring Boot는 프록시 역할을 합니다.
 * </p>
 * 
 * <ul>
 * <li><strong>채점 서비스</strong>: /api/ai-services/grading/*</li>
 * <li><strong>텍스트 인식 서비스</strong>: /api/ai-services/text-recognition/*</li>
 * </ul>
 */
@RestController
@RequestMapping("/ai-services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 서비스 통합 API", description = "AI 서버 채점 및 텍스트 인식 기능 통합 API")
public class AiServiceController {

    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;
    private final AiServerConfig aiServerConfig;

    // ============================================
    // 채점 서비스 (Grading Service)
    // ============================================

    /**
     * 단일 제출물 채점 요청
     * 
     * @param submissionId   제출물 ID
     * @param gradingRequest 채점 요청 데이터
     * @return 채점 처리 결과
     */
    @PostMapping("/grading/{submissionId}")
    @Operation(summary = "단일 제출물 채점", description = "특정 제출물에 대한 AI 채점을 요청합니다", responses = {
            @ApiResponse(responseCode = "200", description = "채점 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "제출물을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> gradeSubmission(
            @Parameter(description = "제출물 ID", example = "12345", required = true) @PathVariable String submissionId,
            @Parameter(description = "채점 요청 데이터", required = true) @RequestBody Map<String, Object> gradingRequest) {

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
    @GetMapping("/grading/{submissionId}")
    @Operation(summary = "단일 제출물 채점 결과 조회", description = "특정 제출물의 AI 채점 결과를 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "채점 결과 조회 성공", content = @Content(schema = @Schema(implementation = AiGradingResponse.class))),
            @ApiResponse(responseCode = "404", description = "제출물 또는 채점 결과를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<AiGradingResponse>> getGradingResult(
            @Parameter(description = "제출물 ID", example = "12345", required = true) @PathVariable String submissionId) {

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
    @PostMapping("/grading/batch")
    @Operation(summary = "배치 채점 요청", description = "여러 제출물에 대한 AI 채점을 일괄 요청합니다", responses = {
            @ApiResponse(responseCode = "200", description = "배치 채점 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> gradeBatch(
            @Parameter(description = "배치 채점 요청 데이터", required = true) @RequestBody Map<String, Object> batchRequest) {

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
    @GetMapping("/grading/stats")
    @Operation(summary = "채점 통계 조회", description = "AI 서버의 채점 통계 정보를 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
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
     * @param limit  조회할 제출물 개수 (선택사항)
     * @return 상태별 제출물 목록
     */
    @GetMapping("/grading/submissions")
    @Operation(summary = "채점 상태별 제출물 조회", description = "특정 채점 상태의 제출물 목록을 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "제출물 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<List>> getSubmissionsByStatus(
            @Parameter(description = "채점 상태", example = "completed", required = false) @RequestParam(required = false) String status,
            @Parameter(description = "조회할 제출물 개수", example = "50", required = false) @RequestParam(required = false) Integer limit) {

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
    @GetMapping("/grading/queue/status")
    @Operation(summary = "채점 큐 상태 조회", description = "AI 서버의 채점 큐 상태를 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "큐 상태 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
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

    // ============================================
    // 텍스트 인식 서비스 (Text Recognition Service)
    // ============================================

    /**
     * 답안지 이미지 텍스트 인식
     * 
     * @param file           업로드된 답안지 이미지 파일
     * @param additionalData 추가 처리 옵션 (JSON 형태)
     * @return 텍스트 인식 결과
     */
    @PostMapping(value = "/text-recognition/answer-sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "답안지 텍스트 인식", description = "업로드된 답안지 이미지에서 텍스트를 인식합니다", responses = {
            @ApiResponse(responseCode = "200", description = "텍스트 인식 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청"),
            @ApiResponse(responseCode = "413", description = "파일 크기가 너무 큼"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> recognizeAnswerSheet(
            @Parameter(description = "답안지 이미지 파일 (JPG, PNG 형식)", required = true) @RequestPart("file") MultipartFile file,
            @Parameter(description = "추가 처리 옵션 (JSON)", required = false) @RequestPart(value = "additionalData", required = false) String additionalData) {

        log.info("답안지 텍스트 인식 요청: fileName={}, fileSize={}", file.getOriginalFilename(), file.getSize());

        return aiServerWebClient.post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/answer-sheet")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource())
                        .with("additionalData", additionalData != null ? additionalData : "{}"))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout() * 2)) // 이미지 처리는 더 긴 타임아웃
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 오류: fileName={}, status={}, message={}",
                            file.getOriginalFilename(), ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 처리 중 예외 발생: fileName={}", file.getOriginalFilename(), ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 배치 텍스트 인식
     * 
     * @param files 여러 답안지 이미지 파일 목록
     * @return 배치 텍스트 인식 결과
     */
    @PostMapping(value = "/text-recognition/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배치 텍스트 인식", description = "여러 답안지 이미지에 대한 텍스트 인식을 일괄 처리합니다", responses = {
            @ApiResponse(responseCode = "200", description = "배치 텍스트 인식 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청"),
            @ApiResponse(responseCode = "413", description = "파일 크기가 너무 큼"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> recognizeBatchAnswerSheets(
            @Parameter(description = "답안지 이미지 파일 목록", required = true) @RequestPart("files") List<MultipartFile> files) {

        log.info("배치 텍스트 인식 요청: fileCount={}", files.size());

        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            multipartData.add("files", file.getResource());
        }

        return aiServerWebClient.post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/batch")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout() * 5)) // 배치 처리는 더 긴 타임아웃
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 배치 텍스트 인식 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("배치 텍스트 인식 처리 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 텍스트 인식 결과 조회
     * 
     * @param taskId 텍스트 인식 작업 ID
     * @return 텍스트 인식 결과
     */
    @GetMapping("/text-recognition/{taskId}")
    @Operation(summary = "텍스트 인식 결과 조회", description = "특정 작업 ID의 텍스트 인식 결과를 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "텍스트 인식 결과 조회 성공"),
            @ApiResponse(responseCode = "404", description = "작업 ID를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> getTextRecognitionResult(
            @Parameter(description = "텍스트 인식 작업 ID", example = "task-12345", required = true) @PathVariable String taskId) {

        log.info("텍스트 인식 결과 조회 요청: taskId={}", taskId);

        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/" + taskId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 결과 조회 오류: taskId={}, status={}, message={}",
                            taskId, ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 결과 조회 중 예외 발생: taskId={}", taskId, ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 텍스트 인식 성능 메트릭 조회
     * 
     * @return 텍스트 인식 성능 메트릭
     */
    @GetMapping("/text-recognition/metrics")
    @Operation(summary = "텍스트 인식 메트릭 조회", description = "AI 서버의 텍스트 인식 성능 메트릭을 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "메트릭 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> getTextRecognitionMetrics() {

        log.info("텍스트 인식 메트릭 조회 요청");

        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/metrics")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 메트릭 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 메트릭 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 텍스트 인식 대시보드 정보 조회
     * 
     * @return 텍스트 인식 대시보드 데이터
     */
    @GetMapping("/text-recognition/dashboard")
    @Operation(summary = "텍스트 인식 대시보드 조회", description = "AI 서버의 텍스트 인식 대시보드 정보를 조회합니다", responses = {
            @ApiResponse(responseCode = "200", description = "대시보드 정보 조회 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> getTextRecognitionDashboard() {

        log.info("텍스트 인식 대시보드 조회 요청");

        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/dashboard")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 대시보드 조회 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 대시보드 조회 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 텍스트 인식 캐시 관리
     * 
     * @param action 캐시 액션 (clear, refresh, status)
     * @return 캐시 관리 결과
     */
    @PostMapping("/text-recognition/cache/{action}")
    @Operation(summary = "텍스트 인식 캐시 관리", description = "AI 서버의 텍스트 인식 캐시를 관리합니다", responses = {
            @ApiResponse(responseCode = "200", description = "캐시 관리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 액션"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> manageTextRecognitionCache(
            @Parameter(description = "캐시 액션 (clear, refresh, status)", example = "clear", required = true) @PathVariable String action) {

        log.info("텍스트 인식 캐시 관리 요청: action={}", action);

        return aiServerWebClient.post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/cache/" + action)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 캐시 관리 오류: action={}, status={}, message={}",
                            action, ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 캐시 관리 중 예외 발생: action={}", action, ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 텍스트 인식 서비스 헬스체크
     * 
     * @return 텍스트 인식 서비스 상태
     */
    @GetMapping("/text-recognition/health")
    @Operation(summary = "텍스트 인식 헬스체크", description = "AI 서버의 텍스트 인식 서비스 상태를 확인합니다", responses = {
            @ApiResponse(responseCode = "200", description = "서비스 정상"),
            @ApiResponse(responseCode = "503", description = "서비스 이용 불가"),
            @ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    public Mono<ResponseEntity<Map>> checkTextRecognitionHealth() {

        log.info("텍스트 인식 헬스체크 요청");

        return aiServerWebClient.get()
                .uri(aiServerConfig.getTextRecognitionPath() + "/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI 서버 텍스트 인식 헬스체크 오류: status={}, message={}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("텍스트 인식 헬스체크 중 예외 발생", ex);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}