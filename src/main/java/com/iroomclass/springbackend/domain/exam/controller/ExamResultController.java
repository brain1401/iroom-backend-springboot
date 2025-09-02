package com.iroomclass.springbackend.domain.exam.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse;
import com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultUpdateRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.StartGradingRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.StartRegradingRequest;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;

import com.iroomclass.springbackend.domain.exam.service.ExamResultService;
import com.iroomclass.springbackend.domain.exam.service.QuestionResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 통합 시험 결과 컨트롤러
 * 
 * 시험 결과 및 문제별 결과 관련 REST API를 제공합니다.
 * 계층적 구조로 시험 결과 하위에 문제별 결과를 관리합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exam/results")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "통합 시험 결과 API", description = "시험 결과 및 문제별 결과 통합 관리 API - 계층적 REST 구조")
public class ExamResultController {

    private final ExamResultService examResultService;
    private final QuestionResultService questionResultService;

    // ============================================
    // 시험 결과 관리 (Exam-Level Results)
    // ============================================

    /**
     * AI 자동 채점 시작
     * 
     * @param request 채점 시작 요청
     * @return 생성된 시험 결과
     */
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "AI 자동 채점 시작", description = "시험 제출에 대한 AI 자동 채점을 시작합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "AI 자동 채점 시작 성공", content = @Content(schema = @Schema(implementation = ExamResultResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 제출물 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 제출 없음", value = ApiResponseConstants.SUBMISSION_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamResultResponse> startGrading(@Valid @RequestBody StartGradingRequest request) {
        log.info("AI 자동 채점 시작 요청: 제출 ID={}", request.submissionId());

        ExamResult result = examResultService.startAutoGrading(request.submissionId());
        ExamResultResponse dto = ExamResultResponse.from(result);

        log.info("AI 자동 채점 시작 성공: 결과 ID={}, 제출 ID={}", dto.id(), request.submissionId());
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "AI 재채점 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기존 채점 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "채점 결과 없음", value = ApiResponseConstants.ORIGINAL_RESULT_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamResultResponse> startRegrading(@Valid @RequestBody StartRegradingRequest request) {
        log.info("AI 재채점 시작 요청: 기존 결과 ID={}", request.originalResultId());

        ExamResult result = examResultService.startRegrading(request.originalResultId());
        ExamResultResponse dto = ExamResultResponse.from(result);

        log.info("AI 재채점 시작 성공: 새 결과 ID={}, 기존 결과 ID={}", dto.id(), request.originalResultId());
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = {
                    @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE),
                    @ExampleObject(name = "채점 미완료", value = ApiResponseConstants.GRADING_NOT_COMPLETED_EXAMPLE)
            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 결과 없음", value = ApiResponseConstants.RESULT_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<Void> completeGrading(@Valid @RequestBody CompleteGradingRequest request) {
        log.info("채점 완료 요청: 결과 ID={}, 코멘트={}", request.resultId(), request.comment());

        examResultService.completeGrading(request.resultId(), request.comment());

        log.info("채점 완료 성공: 결과 ID={}", request.resultId());
        return ApiResponse.success(ApiResponseConstants.GRADING_COMPLETE_SUCCESS);
    }

    /**
     * 시험 결과 조회 (ID)
     * 
     * @param resultId 시험 결과 ID
     * @return 시험 결과
     */
    @GetMapping("/{resultId}")
    @Operation(summary = "시험 결과 조회", description = "시험 결과 ID로 특정 채점 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 결과 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "시험 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<ExamResultResponse> getExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        log.info("시험 결과 조회 요청: 결과 ID={}", resultId);

        ExamResult result = examResultService.findById(resultId);
        ExamResultResponse dto = ExamResultResponse.from(result);

        log.info("시험 결과 조회 성공: 결과 ID={}, 점수={}", resultId, dto.totalScore());
        return ApiResponse.success(ApiResponseConstants.RESULT_GET_SUCCESS, dto);
    }

    /**
     * 제출 ID로 최신 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 최신 채점 결과
     */
    @GetMapping("/submission/{submissionId}/latest")
    @Operation(summary = "제출 ID로 최신 채점 결과 조회", description = "시험 제출 ID로 가장 최신의 채점 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채점 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "채점 결과 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "채점 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<ExamResultResponse> getLatestResultBySubmissionId(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID submissionId) {
        log.info("최신 채점 결과 조회 요청: 제출 ID={}", submissionId);

        ExamResult result = examResultService.findLatestResultBySubmissionId(submissionId);
        ExamResultResponse dto = ExamResultResponse.from(result);

        log.info("최신 채점 결과 조회 성공: 제출 ID={}, 결과 ID={}", submissionId, dto.id());
        return ApiResponse.success(ApiResponseConstants.LATEST_RESULT_SUCCESS, dto);
    }

    /**
     * 제출 ID로 모든 채점 히스토리 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 히스토리 목록
     */
    @GetMapping("/submission/{submissionId}/history")
    @Operation(summary = "채점 히스토리 조회", description = "시험 제출 ID로 모든 채점 히스토리를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채점 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "채점 결과 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "채점 결과를 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<List<ExamResultResponse>> getResultHistoryBySubmissionId(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID submissionId) {
        log.info("채점 히스토리 조회 요청: 제출 ID={}", submissionId);

        List<ExamResultResponse> history = examResultService.findAllResultsBySubmissionId(submissionId)
                .stream()
                .map(ExamResultResponse::from)
                .toList();

        log.info("채점 히스토리 조회 성공: 제출 ID={}, 결과 수={}", submissionId, history.size());
        return ApiResponse.success("채점 히스토리 조회 성공", history);
    }

    /**
     * 시험 결과 삭제
     * 
     * @param resultId 시험 결과 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{resultId}")
    @Operation(summary = "시험 결과 삭제", description = "시험 결과를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "참조 무결성 제약", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "참조 무결성 제약", value = """
                    {
                      "result": "ERROR",
                      "message": "참조 무결성 제약 조건에 위배됩니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<Void> deleteExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        log.info("시험 결과 삭제 요청: 결과 ID={}", resultId);

        examResultService.deleteResult(resultId);

        log.info("시험 결과 삭제 성공: 결과 ID={}", resultId);
        return ApiResponse.success(ApiResponseConstants.RESULT_DELETE_SUCCESS);
    }

    // ============================================
    // 문제별 결과 관리 (Question-Level Results)
    // ============================================

    /**
     * 시험 결과의 문제별 결과 목록 조회
     * 
     * @param resultId 시험 결과 ID
     * @return 문제별 결과 목록
     */
    @GetMapping("/{resultId}/questions")
    @Operation(summary = "문제별 결과 목록 조회", description = "특정 시험 결과의 모든 문제별 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<List<QuestionResultResponse>> getQuestionResultsByExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        log.info("문제별 결과 목록 조회 요청: 시험 결과 ID={}", resultId);

        List<QuestionResultResponse> questionResults = questionResultService.findQuestionResultResponsesByExamResultId(resultId);

        log.info("문제별 결과 목록 조회 성공: 시험 결과 ID={}, 문제 수={}", resultId, questionResults.size());
        return ApiResponse.success("문제별 결과 목록 조회 성공", questionResults);
    }

    /**
     * 문제별 결과 단일 조회
     * 
     * @param resultId         시험 결과 ID
     * @param questionResultId 문제별 결과 ID
     * @return 문제별 결과
     */
    @GetMapping("/{resultId}/questions/{questionResultId}")
    @Operation(summary = "문제별 결과 조회", description = "특정 문제별 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<QuestionResultResponse> getQuestionResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId,
            @Parameter(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID questionResultId) {
        log.info("문제별 결과 조회 요청: 시험 결과 ID={}, 문제별 결과 ID={}", resultId, questionResultId);

        QuestionResultResponse questionResult = questionResultService.findResponseById(questionResultId);

        log.info("문제별 결과 조회 성공: 문제별 결과 ID={}", questionResultId);
        return ApiResponse.success("문제별 결과 조회 성공", questionResult);
    }

    /**
     * 문제별 결과 생성
     * 
     * @param resultId 시험 결과 ID
     * @param request  문제별 결과 생성 요청
     * @return 생성된 문제별 결과
     */
    @PostMapping("/{resultId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "문제별 결과 생성", description = "새로운 문제별 결과를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 검증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<QuestionResultResponse> createQuestionResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId,
            @Valid @RequestBody QuestionResultCreateRequest request) {
        log.info("문제별 결과 생성 요청: 시험 결과 ID={}, 문제 ID={}", resultId, request.questionId());

        QuestionResultResponse questionResult = questionResultService.createQuestionResult(resultId, request);

        log.info("문제별 결과 생성 성공: 문제별 결과 ID={}, 문제 ID={}", questionResult.id(), request.questionId());
        return ApiResponse.success("문제별 결과 생성 성공", questionResult);
    }

    /**
     * 문제별 결과 수정
     * 
     * @param resultId         시험 결과 ID
     * @param questionResultId 문제별 결과 ID
     * @param request          문제별 결과 수정 요청
     * @return 수정된 문제별 결과
     */
    @PutMapping("/{resultId}/questions/{questionResultId}")
    @Operation(summary = "문제별 결과 수정", description = "기존 문제별 결과를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 검증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<QuestionResultResponse> updateQuestionResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId,
            @Parameter(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID questionResultId,
            @Valid @RequestBody QuestionResultUpdateRequest request) {
        log.info("문제별 결과 수정 요청: 문제별 결과 ID={}, 점수={}", questionResultId, request.score());

        QuestionResultResponse questionResult = questionResultService.updateQuestionResult(questionResultId, request);

        log.info("문제별 결과 수정 성공: 문제별 결과 ID={}", questionResultId);
        return ApiResponse.success("문제별 결과 수정 성공", questionResult);
    }

    /**
     * 문제별 결과 삭제
     * 
     * @param resultId         시험 결과 ID
     * @param questionResultId 문제별 결과 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{resultId}/questions/{questionResultId}")
    @Operation(summary = "문제별 결과 삭제", description = "문제별 결과를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<Void> deleteQuestionResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId,
            @Parameter(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID questionResultId) {
        log.info("문제별 결과 삭제 요청: 문제별 결과 ID={}", questionResultId);

        questionResultService.deleteQuestionResult(questionResultId);

        log.info("문제별 결과 삭제 성공: 문제별 결과 ID={}", questionResultId);
        return ApiResponse.success("문제별 결과 삭제 성공");
    }

    /**
     * 문제 ID로 문제별 결과 조회
     * 
     * @param resultId   시험 결과 ID
     * @param questionId 문제 ID
     * @return 문제별 결과
     */
    @GetMapping("/{resultId}/questions/by-question/{questionId}")
    @Operation(summary = "문제 ID로 문제별 결과 조회", description = "문제 ID로 특정 문제의 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<QuestionResultResponse> getQuestionResultByQuestionId(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId,
            @Parameter(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID questionId) {
        log.info("문제 ID로 문제별 결과 조회 요청: 시험 결과 ID={}, 문제 ID={}", resultId, questionId);

        QuestionResultResponse questionResult = questionResultService.findByExamResultIdAndQuestionId(resultId,
                questionId);

        log.info("문제 ID로 문제별 결과 조회 성공: 문제 ID={}", questionId);
        return ApiResponse.success("문제별 결과 조회 성공", questionResult);
    }

    /**
     * 시험 결과의 문제별 점수 통계 조회
     * 
     * @param resultId 시험 결과 ID
     * @return 점수 통계
     */
    @GetMapping("/{resultId}/questions/statistics")
    @Operation(summary = "문제별 점수 통계 조회", description = "시험 결과의 문제별 점수 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<?> getQuestionScoreStatistics(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        log.info("문제별 점수 통계 조회 요청: 시험 결과 ID={}", resultId);

        var statistics = questionResultService.getScoreStatistics(resultId);

        log.info("문제별 점수 통계 조회 성공: 시험 결과 ID={}", resultId);
        return ApiResponse.success("문제별 점수 통계 조회 성공", statistics);
    }

    /**
     * 시험 결과의 오답 문제 목록 조회
     * 
     * @param resultId 시험 결과 ID
     * @return 오답 문제 목록
     */
    @GetMapping("/{resultId}/questions/incorrect")
    @Operation(summary = "오답 문제 목록 조회", description = "시험 결과에서 틀린 문제들의 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ApiResponse<List<QuestionResultResponse>> getIncorrectQuestionResults(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID resultId) {
        log.info("오답 문제 목록 조회 요청: 시험 결과 ID={}", resultId);

        List<QuestionResultResponse> incorrectQuestions = questionResultService
                .findIncorrectQuestionsByExamResult(resultId);

        log.info("오답 문제 목록 조회 성공: 시험 결과 ID={}, 오답 수={}", resultId, incorrectQuestions.size());
        return ApiResponse.success("오답 문제 목록 조회 성공", incorrectQuestions);
    }
}