package com.iroomclass.springbackend.domain.exam.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.exam.dto.result.ManualGradingRequest;
import com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultDto;
import com.iroomclass.springbackend.domain.exam.entity.QuestionResult;
import com.iroomclass.springbackend.domain.exam.entity.QuestionResult.GradingMethod;
import com.iroomclass.springbackend.domain.exam.service.QuestionResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 문제별 채점 결과 컨트롤러
 * 
 * 문제별 채점 결과 관련 REST API를 제공합니다.
 * 수동 채점, 통계 조회, 문제별 분석 등의 기능을 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/api/question-results")
@RequiredArgsConstructor
@Tag(name = "문제별 채점 결과 API", description = "문제별 채점 결과 관리 API")
public class QuestionResultController {

    private final QuestionResultService questionResultService;

    /**
     * 수동 채점 처리
     * 
     * @param request 수동 채점 요청
     * @return 성공 응답
     */
    @PutMapping("/manual-grade")
    @Operation(summary = "수동 채점 처리", description = "문제별 결과에 대한 수동 채점을 처리합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수동 채점 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 수동 채점 대상이 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과를 찾을 수 없음")
    })
    public ApiResponse<Void> processManualGrading(@Valid @RequestBody ManualGradingRequest request) {
        questionResultService.processManualGrading(
                request.resultId(),
                request.score(),
                request.isCorrect(),
                request.feedback());

        return ApiResponse.success("수동 채점이 처리되었습니다");
    }

    /**
     * 문제별 결과 조회 (ID)
     * 
     * @param resultId 문제별 결과 ID
     * @return 문제별 결과
     */
    @GetMapping("/{resultId}")
    @Operation(summary = "문제별 결과 조회", description = "문제별 결과 ID로 특정 채점 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = QuestionResultDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제별 결과를 찾을 수 없음")
    })
    public ApiResponse<QuestionResultDto> getQuestionResult(
            @Parameter(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174003") @PathVariable UUID resultId) {
        QuestionResult result = questionResultService.findById(resultId);
        QuestionResultDto dto = QuestionResultDto.from(result);

        return ApiResponse.success("문제별 결과 조회 성공", dto);
    }

    /**
     * 시험 결과의 문제별 결과 목록 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 문제별 결과 목록
     */
    @GetMapping("/exam-result/{examResultId}")
    @Operation(summary = "시험 결과의 문제별 결과 목록 조회", description = "특정 시험 결과에 속한 모든 문제별 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<List<QuestionResultDto>> getQuestionResultsByExamResult(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examResultId) {
        List<QuestionResult> results = questionResultService.findByExamResultId(examResultId);
        List<QuestionResultDto> dtos = results.stream()
                .map(QuestionResultDto::from)
                .toList();

        return ApiResponse.success("문제별 결과 목록 조회 성공", dtos);
    }

    /**
     * 특정 문제의 채점 결과 조회
     * 
     * @param questionId 문제 ID
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @return 해당 문제의 채점 결과 페이지
     */
    @GetMapping("/question/{questionId}")
    @Operation(summary = "특정 문제의 채점 결과 조회", description = "특정 문제에 대한 모든 채점 결과를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Page<QuestionResultDto>> getResultsByQuestionId(
            @Parameter(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174005") @PathVariable UUID questionId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<QuestionResult> results = questionResultService.findByQuestionId(questionId, pageable);
        Page<QuestionResultDto> dtos = results.map(QuestionResultDto::from);

        return ApiResponse.success("문제별 채점 결과 조회 성공", dtos);
    }

    /**
     * 채점 방법별 결과 조회
     * 
     * @param gradingMethod 채점 방법
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 해당 방법으로 채점된 결과 페이지
     */
    @GetMapping("/grading-method/{gradingMethod}")
    @Operation(summary = "채점 방법별 결과 조회", description = "특정 채점 방법으로 처리된 결과들을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Page<QuestionResultDto>> getResultsByGradingMethod(
            @Parameter(description = "채점 방법", example = "AUTO") @PathVariable GradingMethod gradingMethod,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResult> results = questionResultService.findByGradingMethod(gradingMethod, pageable);
        Page<QuestionResultDto> dtos = results.map(QuestionResultDto::from);

        return ApiResponse.success("채점 방법별 결과 조회 성공", dtos);
    }

    /**
     * 수동 채점 대기 목록 조회
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 수동 채점 대기 결과 페이지
     */
    @GetMapping("/pending-manual")
    @Operation(summary = "수동 채점 대기 목록 조회", description = "수동 채점이 필요한 문제별 결과들을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Page<QuestionResultDto>> getPendingManualGrading(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResult> results = questionResultService.findPendingManualGrading(pageable);
        Page<QuestionResultDto> dtos = results.map(QuestionResultDto::from);

        return ApiResponse.success("수동 채점 대기 목록 조회 성공", dtos);
    }

    /**
     * 낮은 신뢰도 AI 채점 결과 조회
     * 
     * @param confidenceThreshold 신뢰도 임계값
     * @param page                페이지 번호
     * @param size                페이지 크기
     * @return 낮은 신뢰도 AI 채점 결과 페이지
     */
    @GetMapping("/low-confidence")
    @Operation(summary = "낮은 신뢰도 AI 채점 결과 조회", description = "지정된 임계값보다 낮은 신뢰도를 가진 AI 채점 결과들을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Page<QuestionResultDto>> getLowConfidenceAIResults(
            @Parameter(description = "신뢰도 임계값", example = "0.7") @RequestParam(defaultValue = "0.7") BigDecimal confidenceThreshold,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResult> results = questionResultService.findLowConfidenceAIResults(confidenceThreshold, pageable);
        Page<QuestionResultDto> dtos = results.map(QuestionResultDto::from);

        return ApiResponse.success("낮은 신뢰도 AI 채점 결과 조회 성공", dtos);
    }

    /**
     * 문제별 정답률 조회
     * 
     * @param questionId 문제 ID
     * @return 정답률
     */
    @GetMapping("/question/{questionId}/correct-rate")
    @Operation(summary = "문제별 정답률 조회", description = "특정 문제의 정답률을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Object> getCorrectRateByQuestionId(
            @Parameter(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174005") @PathVariable UUID questionId) {
        Double correctRate = questionResultService.calculateCorrectRate(questionId);
        Double averageScore = questionResultService.calculateAverageScore(questionId);

        return ApiResponse.success("문제별 정답률 조회 성공",
                java.util.Map.of(
                        "questionId", questionId,
                        "correctRate", correctRate != null ? correctRate : 0.0,
                        "averageScore", averageScore != null ? averageScore : 0.0));
    }

    /**
     * 채점 방법별 통계 조회
     * 
     * @return 채점 방법별 통계
     */
    @GetMapping("/statistics/grading-methods")
    @Operation(summary = "채점 방법별 통계 조회", description = "각 채점 방법별 통계 정보를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Object> getGradingMethodStatistics() {
        Object[] autoStats = questionResultService.getStatisticsByGradingMethod(GradingMethod.AUTO);
        Object[] manualStats = questionResultService.getStatisticsByGradingMethod(GradingMethod.MANUAL);
        Object[] aiAssistedStats = questionResultService.getStatisticsByGradingMethod(GradingMethod.AI_ASSISTED);

        return ApiResponse.success("채점 방법별 통계 조회 성공",
                java.util.Map.of(
                        "AUTO", createStatisticsMap(autoStats),
                        "MANUAL", createStatisticsMap(manualStats),
                        "AI_ASSISTED", createStatisticsMap(aiAssistedStats)));
    }

    /**
     * 시험 결과의 채점 진행률 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 채점 진행률
     */
    @GetMapping("/exam-result/{examResultId}/progress")
    @Operation(summary = "채점 진행률 조회", description = "특정 시험 결과의 채점 진행률을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<Object> getGradingProgress(
            @Parameter(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examResultId) {
        Double progress = questionResultService.calculateGradingProgress(examResultId);
        Boolean allGraded = questionResultService.isAllQuestionsGraded(examResultId);
        Integer totalScore = questionResultService.calculateTotalScore(examResultId);

        return ApiResponse.success("채점 진행률 조회 성공",
                java.util.Map.of(
                        "examResultId", examResultId,
                        "progress", progress != null ? progress : 0.0,
                        "allGraded", allGraded,
                        "totalScore", totalScore != null ? totalScore : 0));
    }

    /**
     * 통계 맵 생성 헬퍼 메서드
     * 
     * @param stats 통계 배열
     * @return 통계 맵
     */
    private java.util.Map<String, Object> createStatisticsMap(Object[] stats) {
        if (stats == null || stats.length < 4) {
            return java.util.Map.of(
                    "totalCount", 0,
                    "correctCount", 0,
                    "averageScore", 0.0,
                    "averageConfidence", 0.0);
        }

        return java.util.Map.of(
                "totalCount", stats[0] != null ? stats[0] : 0,
                "correctCount", stats[1] != null ? stats[1] : 0,
                "averageScore", stats[2] != null ? stats[2] : 0.0,
                "averageConfidence", stats[3] != null ? stats[3] : 0.0);
    }
}