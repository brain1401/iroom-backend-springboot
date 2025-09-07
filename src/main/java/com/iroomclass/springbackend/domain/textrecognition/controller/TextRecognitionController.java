package com.iroomclass.springbackend.domain.textrecognition.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.textrecognition.dto.*;
import com.iroomclass.springbackend.domain.textrecognition.service.TextRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 텍스트 인식 API 컨트롤러
 * 
 * <p>
 * AI 서버와 연동하여 다음 기능을 제공합니다:
 * </p>
 * <ul>
 * <li>동기식 답안지 인식</li>
 * <li>배치 글자인식 처리</li>
 * <li>비동기 작업 제출 및 상태 조회</li>
 * <li>SSE를 통한 실시간 진행률 스트리밍</li>
 * </ul>
 */
@Tag(name = "텍스트 인식 API", description = "AI 기반 텍스트 인식 처리 API")
@RestController
@RequestMapping("/text-recognition")
@RequiredArgsConstructor
@Slf4j
public class TextRecognitionController {

    private final TextRecognitionService textRecognitionService;

    // ==================== 1. 동기식 엔드포인트 ====================

    /**
     * 답안지 글자인식 (동기식 - 즉시 응답)
     */
    @Operation(summary = "답안지 글자인식 (동기식)", description = "이미지를 업로드하여 즉시 글자인식 결과를 받습니다. 최대 처리 시간 60초.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "글자인식 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "파일 크기 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/answer-sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TextRecognitionAnswerResponse>> recognizeAnswerSheet(
            @Parameter(description = "이미지 파일 (JPEG, PNG, WEBP, GIF)", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "캐시 사용 여부", example = "true") @RequestParam(value = "use_cache", defaultValue = "true") Boolean useCache,

            @Parameter(description = "컨텐츠 해시 사용 여부", example = "false") @RequestParam(value = "use_content_hash", defaultValue = "false") Boolean useContentHash) {
        log.info("답안지 글자인식 요청: 파일명={}, 크기={}bytes, 캐시={}, 해시={}",
                file.getOriginalFilename(), file.getSize(), useCache, useContentHash);

        TextRecognitionAnswerResponse response = textRecognitionService.recognizeAnswerSheetSync(
                file, useCache, useContentHash);

        return ResponseEntity.ok(
                ApiResponse.success("답안지 글자인식이 완료되었습니다", response));
    }

    /**
     * 배치 글자인식
     */
    @Operation(summary = "배치 글자인식", description = "여러 이미지를 한번에 업로드하여 배치 처리합니다. 최대 20개 파일.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 작업 시작"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파일 개수 초과 또는 잘못된 요청")
    })
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BatchTextRecognitionResponse>> submitBatchRecognition(
            @Parameter(description = "이미지 파일 배열 (최대 20개)", required = true) @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "우선순위 (1-5)", example = "1") @RequestParam(value = "priority", defaultValue = "1") Integer priority,

            @Parameter(description = "캐시 사용 여부", example = "true") @RequestParam(value = "use_cache", defaultValue = "true") Boolean useCache) {
        log.info("배치 글자인식 요청: 파일 개수={}, 우선순위={}", files.size(), priority);

        BatchTextRecognitionRequest request = BatchTextRecognitionRequest.builder()
                .files(files)
                .priority(priority)
                .useCache(useCache)
                .build();

        BatchTextRecognitionResponse response = textRecognitionService.submitBatchRecognition(request);

        return ResponseEntity.ok(
                ApiResponse.success("배치 작업이 시작되었습니다", response));
    }

    // ==================== 2. 비동기 엔드포인트 ====================

    /**
     * 비동기 작업 제출
     */
    @Operation(summary = "비동기 작업 제출", description = "이미지를 업로드하여 비동기 처리를 시작합니다. 완료 시 콜백 URL로 결과 전송.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "작업 접수됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(value = "/async/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AsyncTextRecognitionSubmitResponse>> submitAsyncRecognition(
            @Parameter(description = "이미지 파일", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "완료 시 결과를 받을 URL", required = true) @RequestParam("callback_url") String callbackUrl,

            @Parameter(description = "우선순위 (1-10)", example = "5") @RequestParam(value = "priority", defaultValue = "5") Integer priority,

            @Parameter(description = "캐시 사용 여부", example = "true") @RequestParam(value = "use_cache", defaultValue = "true") Boolean useCache) {
        log.info("비동기 작업 제출: 파일명={}, 콜백={}", file.getOriginalFilename(), callbackUrl);

        AsyncTextRecognitionSubmitRequest request = AsyncTextRecognitionSubmitRequest.builder()
                .callbackUrl(callbackUrl)
                .priority(priority)
                .useCache(useCache)
                .build();

        AsyncTextRecognitionSubmitResponse response = textRecognitionService.submitAsyncRecognition(
                file, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.success("작업이 접수되었습니다", response));
    }

    /**
     * 작업 상태 조회
     */
    @Operation(summary = "작업 상태 조회", description = "비동기 작업의 현재 상태를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "작업을 찾을 수 없음")
    })
    @GetMapping("/async/status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(
            @Parameter(description = "작업 ID", required = true) @PathVariable String jobId) {
        log.info("작업 상태 조회: jobId={}", jobId);

        JobStatusResponse status = textRecognitionService.getJobStatus(jobId);

        return ResponseEntity.ok(
                ApiResponse.success("작업 상태 조회 성공", status));
    }

    /**
     * 작업 결과 조회
     */
    @Operation(summary = "작업 결과 조회", description = "완료된 비동기 작업의 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결과 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "작업을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "425", description = "작업이 아직 완료되지 않음")
    })
    @GetMapping("/async/result/{jobId}")
    public ResponseEntity<ApiResponse<TextRecognitionAnswerResponse>> getJobResult(
            @Parameter(description = "작업 ID", required = true) @PathVariable String jobId) {
        log.info("작업 결과 조회: jobId={}", jobId);

        TextRecognitionAnswerResponse result = textRecognitionService.getJobResult(jobId);

        return ResponseEntity.ok(
                ApiResponse.success("작업 결과 조회 성공", result));
    }

    /**
     * AI 서버 직접 상태 조회 (폴링용)
     */
    @Operation(summary = "AI 서버 직접 상태 조회", description = "콜백이 안 올 때 Spring Boot에서 직접 AI 서버 상태를 확인합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공")
    })
    @GetMapping("/async/ai-server-status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse>> checkAIServerStatus(
            @Parameter(description = "작업 ID", required = true) @PathVariable String jobId) {
        log.info("AI 서버 상태 직접 조회: jobId={}", jobId);

        JobStatusResponse status = textRecognitionService.checkAIServerStatus(jobId);

        return ResponseEntity.ok(
                ApiResponse.success("AI 서버 상태 조회 성공", status));
    }

    // ==================== 3. SSE 스트리밍 ====================

    /**
     * 배치 진행률 스트리밍
     */
    @Operation(summary = "배치 진행률 스트리밍", description = "Server-Sent Events를 통해 배치 처리 진행률을 실시간으로 받습니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배치 작업을 찾을 수 없음")
    })
    @GetMapping(value = "/batch/{batchId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBatchProgress(
            @Parameter(description = "배치 ID", required = true) @PathVariable String batchId) {
        log.info("배치 진행률 스트리밍 요청: batchId={}", batchId);

        return textRecognitionService.streamBatchProgress(batchId);
    }

    // ==================== 4. 콜백 엔드포인트 ====================

    /**
     * AI 서버로부터의 콜백 처리
     */
    @Operation(summary = "AI 서버 콜백 처리", description = "AI 서버가 작업 완료 후 결과를 전송하기 위한 내부 API입니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "콜백 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 콜백 데이터")
    })
    @PostMapping("/callback/{jobId}")
    public ResponseEntity<ApiResponse<Void>> handleCallback(
            @Parameter(description = "작업 ID", required = true) @PathVariable String jobId,

            @Parameter(description = "콜백 데이터", required = true) @Valid @RequestBody AsyncTextRecognitionCallbackData callbackData) {
        log.info("AI 서버 콜백 수신: jobId={}, status={}", jobId, callbackData.status());

        textRecognitionService.handleCallback(jobId, callbackData);

        return ResponseEntity.ok(
                ApiResponse.success("콜백이 성공적으로 처리되었습니다"));
    }
}