package com.iroomclass.springbackend.domain.user.exam.result.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.user.exam.result.dto.CompleteGradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.dto.ExamResultDto;
import com.iroomclass.springbackend.domain.user.exam.result.dto.StartGradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.dto.StartRegradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;
import com.iroomclass.springbackend.domain.user.exam.result.service.ExamResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * AI 시험 결과 컨트롤러
 * 
 * AI 기반 시험 채점 결과 관련 REST API를 제공합니다.
 * AI 자동 채점, AI 재채점, 조회 등의 기능을 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/api/exam-results")
@RequiredArgsConstructor
@Tag(name = "AI 시험 결과 API", description = "AI 기반 시험 채점 결과 관리 API")
public class ExamResultController {
    
    private final ExamResultService examResultService;
    
    /**
     * AI 자동 채점 시작
     * 
     * @param request 채점 시작 요청
     * @return 생성된 시험 결과
     */
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "AI 자동 채점 시작",
        description = "시험 제출에 대한 AI 자동 채점을 시작합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "AI 자동 채점 시작 성공",
                content = @Content(schema = @Schema(implementation = ExamResultDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "시험 제출물을 찾을 수 없음"
            )
        }
    )
    public ApiResponse<ExamResultDto> startGrading(@Valid @RequestBody StartGradingRequest request) {
        // AI 자동 채점만 지원
        ExamResult result = examResultService.startAutoGrading(request.submissionId());
        ExamResultDto dto = ExamResultDto.from(result);
        
        return ApiResponse.success("AI 자동 채점이 시작되었습니다", dto);
    }
    
    /**
     * AI 재채점 시작
     * 
     * @param request 재채점 시작 요청
     * @return 새로 생성된 시험 결과
     */
    @PostMapping("/regrade")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "AI 재채점 시작",
        description = "기존 채점 결과에 대한 AI 재채점을 시작합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "AI 재채점 시작 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "기존 채점 결과를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<ExamResultDto> startRegrading(@Valid @RequestBody StartRegradingRequest request) {
        // AI 재채점은 기존 결과 ID만 필요
        ExamResult result = examResultService.startRegrading(request.originalResultId());
        ExamResultDto dto = ExamResultDto.from(result);
        
        return ApiResponse.success("AI 재채점이 시작되었습니다", dto);
    }
    
    /**
     * 채점 완료
     * 
     * @param request 채점 완료 요청
     * @return 성공 응답
     */
    @PutMapping("/complete")
    @Operation(
        summary = "채점 완료",
        description = "진행 중인 채점을 완료합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "채점 완료 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터 또는 채점이 완료되지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "시험 결과를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<Void> completeGrading(@Valid @RequestBody CompleteGradingRequest request) {
        examResultService.completeGrading(request.resultId(), request.comment());
        return ApiResponse.success("채점이 완료되었습니다");
    }
    
    /**
     * 시험 결과 조회 (ID)
     * 
     * @param resultId 시험 결과 ID
     * @return 시험 결과
     */
    @GetMapping("/{resultId}")
    @Operation(
        summary = "시험 결과 조회",
        description = "시험 결과 ID로 특정 채점 결과를 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "시험 결과를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<ExamResultDto> getExamResult(
        @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID resultId
    ) {
        ExamResult result = examResultService.findById(resultId);
        ExamResultDto dto = ExamResultDto.from(result);
        
        return ApiResponse.success("시험 결과 조회 성공", dto);
    }
    
    /**
     * 제출 ID로 최신 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 최신 채점 결과
     */
    @GetMapping("/submission/{submissionId}/latest")
    @Operation(
        summary = "제출 ID로 최신 채점 결과 조회",
        description = "시험 제출 ID로 가장 최신의 채점 결과를 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "채점 결과를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<ExamResultDto> getLatestResultBySubmissionId(
        @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001")
        @PathVariable UUID submissionId
    ) {
        ExamResult result = examResultService.findLatestResultBySubmissionId(submissionId);
        ExamResultDto dto = ExamResultDto.from(result);
        
        return ApiResponse.success("최신 채점 결과 조회 성공", dto);
    }
    
    /**
     * 제출 ID로 모든 채점 히스토리 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 히스토리 목록
     */
    @GetMapping("/submission/{submissionId}/history")
    @Operation(
        summary = "채점 히스토리 조회",
        description = "시험 제출 ID로 모든 채점 히스토리를 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        }
    )
    public ApiResponse<List<ExamResultDto>> getResultHistoryBySubmissionId(
        @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001")
        @PathVariable UUID submissionId
    ) {
        List<ExamResult> results = examResultService.findAllResultsBySubmissionId(submissionId);
        List<ExamResultDto> dtos = results.stream()
            .map(ExamResultDto::fromWithoutQuestions)
            .toList();
        
        return ApiResponse.success("채점 히스토리 조회 성공", dtos);
    }
    
    /**
     * AI 채점 결과 목록 조회
     * 
     * @param status 채점 상태 필터
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @param direction 정렬 방향
     * @return 채점 결과 페이지
     */
    @GetMapping
    @Operation(
        summary = "AI 채점 결과 목록 조회",
        description = "상태별로 AI 채점 결과 목록을 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        }
    )
    public ApiResponse<Page<ExamResultDto>> getExamResults(
        @Parameter(description = "채점 상태 필터", example = "COMPLETED")
        @RequestParam(required = false) ResultStatus status,
        
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "정렬 기준", example = "gradedAt")
        @RequestParam(defaultValue = "gradedAt") String sort,
        
        @Parameter(description = "정렬 방향", example = "desc")
        @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<ExamResult> results;
        
        if (status != null) {
            results = examResultService.findResultsByStatus(status, pageable);
        } else {
            // 기본적으로 모든 AI 자동 채점 결과 조회
            results = examResultService.findAutoGradedResults(pageable);
        }
        
        Page<ExamResultDto> dtos = results.map(ExamResultDto::fromWithoutQuestions);
        
        return ApiResponse.success("AI 채점 결과 목록 조회 성공", dtos);
    }
    
    /**
     * 재채점 결과 조회
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 재채점 결과 페이지
     */
    @GetMapping("/regraded")
    @Operation(
        summary = "재채점 결과 조회",
        description = "재채점된 결과만 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        }
    )
    public ApiResponse<Page<ExamResultDto>> getRegradedResults(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gradedAt"));
        Page<ExamResult> results = examResultService.findRegradedResults(pageable);
        Page<ExamResultDto> dtos = results.map(ExamResultDto::fromWithoutQuestions);
        
        return ApiResponse.success("재채점 결과 조회 성공", dtos);
    }
    
    /**
     * 채점 상태별 통계 조회
     * 
     * @return 상태별 개수 통계
     */
    @GetMapping("/statistics/status")
    @Operation(
        summary = "채점 상태별 통계",
        description = "채점 상태별 개수 통계를 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        }
    )
    public ApiResponse<Object> getStatusStatistics() {
        return ApiResponse.success("채점 상태별 통계 조회 성공", 
            java.util.Map.of(
                "PENDING", examResultService.countByStatus(ResultStatus.PENDING),
                "IN_PROGRESS", examResultService.countByStatus(ResultStatus.IN_PROGRESS),
                "COMPLETED", examResultService.countByStatus(ResultStatus.COMPLETED),
                "REGRADED", examResultService.countByStatus(ResultStatus.REGRADED),
                "AUTO_GRADED", examResultService.countAutoGradedResults()
            )
        );
    }
    
    /**
     * 시험 결과 삭제
     * 
     * @param resultId 시험 결과 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{resultId}")
    @Operation(
        summary = "시험 결과 삭제",
        description = "시험 결과를 삭제합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "시험 결과를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<Void> deleteExamResult(
        @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID resultId
    ) {
        examResultService.deleteResult(resultId);
        return ApiResponse.success("시험 결과가 삭제되었습니다");
    }
}