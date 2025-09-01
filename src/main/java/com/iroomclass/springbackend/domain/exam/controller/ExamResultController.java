package com.iroomclass.springbackend.domain.exam.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ApiResponseConstants;
import com.iroomclass.springbackend.domain.exam.dto.result.CompleteGradingRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.ExamResultResponse;
import com.iroomclass.springbackend.domain.exam.dto.result.StartGradingRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.StartRegradingRequest;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;

import com.iroomclass.springbackend.domain.exam.service.ExamResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/student/exam-results")
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
    @Operation(summary = "AI 자동 채점 시작", description = "시험 제출에 대한 AI 자동 채점을 시작합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", 
                    description = "AI 자동 채점 시작 성공", 
                    content = @Content(schema = @Schema(implementation = ExamResultResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "시험 제출물 없음", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = @ExampleObject(name = "시험 제출 없음", value = ApiResponseConstants.SUBMISSION_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamResultResponse> startGrading(@Valid @RequestBody StartGradingRequest request) {
        // AI 자동 채점만 지원
        ExamResult result = examResultService.startAutoGrading(request.submissionId());
        ExamResultResponse dto = ExamResultResponse.from(result);

        return ApiResponse.success(ApiResponseConstants.GRADING_START_SUCCESS, dto);
    }

    /**
     * AI 재채점 시작
     * 
     * @param request 재채점 시작 요청
     * @return 새로 생성된 시험 결과
     */
    @PostMapping("/regrade")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "AI 재채점 시작", description = "기존 채점 결과에 대한 AI 재채점을 시작합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", 
                    description = "AI 재채점 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "기존 채점 결과 없음", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = @ExampleObject(name = "채점 결과 없음", value = ApiResponseConstants.ORIGINAL_RESULT_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamResultResponse> startRegrading(@Valid @RequestBody StartRegradingRequest request) {
        // AI 재채점은 기존 결과 ID만 필요
        ExamResult result = examResultService.startRegrading(request.originalResultId());
        ExamResultResponse dto = ExamResultResponse.from(result);

        return ApiResponse.success(ApiResponseConstants.REGRADING_START_SUCCESS, dto);
    }

    /**
     * 채점 완료
     * 
     * @param request 채점 완료 요청
     * @return 성공 응답
     */
    @PutMapping("/complete")
    @Operation(summary = "채점 완료", description = "진행 중인 채점을 완료합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "성공", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = {
                                    @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE),
                                    @ExampleObject(name = "채점 미완료", value = ApiResponseConstants.GRADING_NOT_COMPLETED_EXAMPLE)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "시험 결과 없음", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                            examples = @ExampleObject(name = "시험 결과 없음", value = ApiResponseConstants.RESULT_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<Void> completeGrading(@Valid @RequestBody CompleteGradingRequest request) {
        examResultService.completeGrading(request.resultId(), request.comment());
        return ApiResponse.success(ApiResponseConstants.GRADING_COMPLETE_SUCCESS);
    }

    /**
     * 시험 결과 조회 (ID)
     * 
     * @param resultId 시험 결과 ID
     * @return 시험 결과
     */
    @GetMapping("/{resultId}")
    @Operation(summary = "시험 결과 조회", description = "시험 결과 ID로 특정 채점 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 결과 없음", summary = "시험 결과를 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "시험 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<ExamResultResponse> getExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        ExamResult result = examResultService.findById(resultId);
        ExamResultResponse dto = ExamResultResponse.from(result);

        return ApiResponse.success(ApiResponseConstants.RESULT_GET_SUCCESS, dto);
    }

    /**
     * 제출 ID로 최신 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 최신 채점 결과
     */
    @GetMapping("/submission/{submissionId}/latest")
    @Operation(summary = "제출 ID로 최신 채점 결과 조회", description = "시험 제출 ID로 가장 최신의 채점 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "채점 결과 없음", summary = "채점 결과를 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "채점 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<ExamResultResponse> getLatestResultBySubmissionId(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID submissionId) {
        ExamResult result = examResultService.findLatestResultBySubmissionId(submissionId);
        ExamResultResponse dto = ExamResultResponse.from(result);

        return ApiResponse.success(ApiResponseConstants.LATEST_RESULT_SUCCESS, dto);
    }

    /**
     * 제출 ID로 모든 채점 히스토리 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 히스토리 목록
     */
    @GetMapping("/submission/{submissionId}/history")
    @Operation(summary = "채점 히스토리 조회", description = "시험 제출 ID로 모든 채점 히스토리를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 결과 없음", summary = "시험 결과를 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "시험 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "충돌", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "참조 무결성 제약", summary = "참조 무결성 제약 조건 위배", value = """
                    {
                      "result": "ERROR",
                      "message": "참조 무결성 제약 조건에 위배됩니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<Void> deleteExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        examResultService.deleteResult(resultId);
        return ApiResponse.success(ApiResponseConstants.RESULT_DELETE_SUCCESS);
    }
}