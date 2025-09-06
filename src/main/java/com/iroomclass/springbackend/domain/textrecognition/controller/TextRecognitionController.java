package com.iroomclass.springbackend.domain.textrecognition.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.textrecognition.dto.TextRecognitionSubmitRequest;
import com.iroomclass.springbackend.domain.textrecognition.dto.TextRecognitionJobResponse;
import com.iroomclass.springbackend.domain.textrecognition.dto.AIServerCallbackRequest;
import com.iroomclass.springbackend.domain.textrecognition.service.TextRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;

/**
 * 텍스트 인식 비동기 처리 API 컨트롤러
 * 
 * <p>이 컨트롤러는 다음 기능을 제공합니다:</p>
 * <ul>
 *   <li>이미지 파일 업로드 및 텍스트 인식 작업 제출</li>
 *   <li>Server-Sent Events(SSE)를 통한 실시간 작업 상태 업데이트</li>
 *   <li>AI 서버로부터의 콜백 처리</li>
 * </ul>
 */
@Tag(name = "텍스트 인식 API", description = "비동기 텍스트 인식 처리 API")
@RestController
@RequestMapping("/text-recognition")
@RequiredArgsConstructor
@Slf4j
public class TextRecognitionController {
    
    private final TextRecognitionService textRecognitionService;
    
    /**
     * 텍스트 인식 작업 제출
     * 
     * @param file 텍스트 인식을 수행할 이미지 파일
     * @param pageNumber 페이지 번호 (기본값: 1)
     * @param questionType 질문 유형 (기본값: "단답형")
     * @param gradeLevel 학년 수준 (기본값: "중학교")
     * @return 작업 ID와 SSE URL이 포함된 응답
     */
    @Operation(
        summary = "텍스트 인식 작업 제출",
        description = """
            이미지 파일을 업로드하여 텍스트 인식 작업을 시작합니다.
            
            작업은 즉시 AI 서버로 전송되며, 클라이언트는 반환된 jobId를 사용하여
            SSE 연결을 통해 실시간으로 작업 진행 상황을 받을 수 있습니다.
            
            지원되는 파일 형식:
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WEBP (.webp)
            - GIF (.gif)
            
            최대 파일 크기: 20MB
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "작업 제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 형식, 크기 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TextRecognitionJobResponse>> submitTextRecognition(
        @Parameter(description = "텍스트 인식할 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file,
        
        @Parameter(description = "페이지 번호", example = "1")
        @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
        
        @Parameter(description = "질문 유형", example = "단답형")
        @RequestParam(value = "questionType", defaultValue = "단답형") String questionType,
        
        @Parameter(description = "학년 수준", example = "중학교")
        @RequestParam(value = "gradeLevel", defaultValue = "중학교") String gradeLevel
    ) {
        
        log.info("텍스트 인식 작업 제출 요청: 파일명={}, 크기={}bytes, 페이지={}, 질문유형={}, 학년={}", 
                file.getOriginalFilename(), file.getSize(), pageNumber, questionType, gradeLevel);
        
        // DTO 객체 생성
        TextRecognitionSubmitRequest request = TextRecognitionSubmitRequest.builder()
            .file(file)
            .pageNumber(pageNumber)
            .questionType(questionType)
            .gradeLevel(gradeLevel)
            .build();
        
        // 작업 제출
        TextRecognitionJobResponse response = textRecognitionService.submitTextRecognition(request);
        
        log.info("텍스트 인식 작업 제출 완료: jobId={}", response.jobId());
        
        return ResponseEntity.ok(
            ApiResponse.success("텍스트 인식 작업이 성공적으로 제출되었습니다", response)
        );
    }
    
    /**
     * SSE 연결을 통한 실시간 작업 상태 업데이트
     * 
     * @param jobId 작업 ID
     * @return SSE 스트림
     */
    @Operation(
        summary = "실시간 작업 상태 업데이트 스트림",
        description = """
            Server-Sent Events(SSE)를 통해 텍스트 인식 작업의 실시간 상태 업데이트를 받습니다.
            
            연결 후 다음과 같은 이벤트를 수신할 수 있습니다:
            - JOB_STATUS: 작업 상태 변경 (SUBMITTED, PROCESSING, COMPLETED, FAILED)
            - JOB_RESULT: 최종 텍스트 인식 결과 (완료 시)
            - JOB_ERROR: 오류 정보 (실패 시)
            
            연결 타임아웃: 30분
            재연결 간격: 3초
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 작업 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "이미 완료된 작업")
        }
    )
    @GetMapping(value = "/jobs/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToJobUpdates(
        @Parameter(description = "작업 ID", required = true, example = "job_20240817_143052_abc123")
        @PathVariable String jobId
    ) {
        
        log.info("SSE 연결 요청: jobId={}", jobId);
        
        SseEmitter emitter = textRecognitionService.subscribeToJob(jobId);
        
        log.info("SSE 연결 생성 완료: jobId={}", jobId);
        
        return emitter;
    }
    
    /**
     * AI 서버로부터의 콜백 처리
     * 
     * <p>이 엔드포인트는 AI 서버가 텍스트 인식 작업 완료 후 결과를 전송하기 위해 사용합니다.</p>
     * 
     * @param callbackRequest AI 서버로부터의 콜백 요청 데이터
     * @return 콜백 처리 결과
     */
    @Operation(
        summary = "AI 서버 콜백 처리",
        description = """
            AI 서버가 텍스트 인식 작업 완료 후 결과를 전송하기 위한 내부 API입니다.
            
            이 엔드포인트는 AI 서버만 호출해야 하며, 일반 클라이언트는 사용하지 않습니다.
            
            콜백 처리 후 다음 작업이 수행됩니다:
            1. 작업 상태를 COMPLETED 또는 FAILED로 업데이트
            2. 연결된 모든 SSE 클라이언트에게 결과 전송
            3. 메모리에서 작업 정보 정리
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "콜백 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 콜백 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 작업 ID")
        }
    )
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> handleCallback(
        @Parameter(description = "AI 서버로부터의 콜백 요청 데이터", required = true)
        @Valid @RequestBody AIServerCallbackRequest callbackRequest
    ) {
        
        log.info("AI 서버 콜백 수신: jobId={}, status={}", 
                callbackRequest.jobId(), callbackRequest.status());
        
        // 콜백 처리
        textRecognitionService.handleCallback(callbackRequest);
        
        log.info("AI 서버 콜백 처리 완료: jobId={}", callbackRequest.jobId());
        
        return ResponseEntity.ok(
            ApiResponse.success("콜백이 성공적으로 처리되었습니다")
        );
    }
}