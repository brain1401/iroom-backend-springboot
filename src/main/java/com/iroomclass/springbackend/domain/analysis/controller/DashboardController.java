package com.iroomclass.springbackend.domain.analysis.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.analysis.dto.OverallStatisticsResponse;
import com.iroomclass.springbackend.domain.analysis.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API 컨트롤러
 * 
 * <p>관리자 대시보드 관련 API를 제공합니다.</p>
 */
@Slf4j
@Tag(name = "관리자 - 대시보드", description = "관리자 대시보드 API")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 전체 학년 통합 통계 조회
     * 
     * @return 모든 학년의 통합 통계 정보
     */
    @Operation(
        summary = "전체 학년 통합 통계 조회",
        description = "모든 학년의 통합 통계 정보를 조회합니다",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                    description = "성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = @ExampleObject(
                        name = "서버 오류",
                        summary = "서버 내부 오류 발생",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "서버 내부 오류가 발생했습니다",
                          "data": null
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/overall-statistics")
    public ApiResponse<OverallStatisticsResponse> getOverallStatistics() {
        log.info("전체 학년 통합 통계 조회 요청");
        
        OverallStatisticsResponse response = dashboardService.getOverallStatistics();
        
        log.info("전체 학년 통합 통계 조회 완료: 전체 학생 수={}, 평균 성적={}", 
                response.totalStudentCount(), response.overallAverageScore());
        
        return ApiResponse.success("전체 학년 통합 통계 조회 성공", response);
    }

    /**
     * 학년별 시험 제출 현황 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험별 제출 현황
     */
    @Operation(
        summary = "학년별 시험 제출 현황 조회",
        description = "특정 학년의 시험별 제출 현황을 조회합니다",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                    description = "성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = @ExampleObject(
                        name = "잘못된 학년",
                        summary = "학년 값이 올바르지 않음",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "파라미터 'grade'의 값이 올바르지 않습니다",
                          "data": null
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "$1",
                    description = "오류",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "서버 오류",
                        summary = "서버 내부 오류 발생",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "서버 내부 오류가 발생했습니다",
                          "data": null
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/grade/{grade}/submission-status")
    public ApiResponse<GradeSubmissionStatusResponse> getGradeSubmissionStatus(
            @Parameter(description = "학년", example = "1") 
            @PathVariable Integer grade) {
        log.info("학년별 시험 제출 현황 조회 요청: 학년={}", grade);
        
        GradeSubmissionStatusResponse response = dashboardService.getGradeSubmissionStatus(grade);
        
        log.info("학년별 시험 제출 현황 조회 완료: 학년={}, 시험 수={}", grade, response.totalExamCount());
        
        return ApiResponse.success("학년별 시험 제출 현황 조회 성공", response);
    }

    /**
     * 학년별 성적 분포도 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 성적 분포도
     */
    @Operation(
        summary = "학년별 성적 분포도 조회",
        description = "특정 학년의 성적 분포도를 조회합니다",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                    description = "성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = @ExampleObject(
                        name = "잘못된 학년",
                        summary = "학년 값이 올바르지 않음",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "파라미터 'grade'의 값이 올바르지 않습니다",
                          "data": null
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "$1",
                    description = "오류",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "서버 오류",
                        summary = "서버 내부 오류 발생",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "서버 내부 오류가 발생했습니다",
                          "data": null
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/grade/{grade}/score-distribution")
    public ApiResponse<GradeScoreDistributionResponse> getGradeScoreDistribution(
            @Parameter(description = "학년", example = "1") 
            @PathVariable Integer grade) {
        log.info("학년별 성적 분포도 조회 요청: 학년={}", grade);
        
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(grade);
        
        log.info("학년별 성적 분포도 조회 완료: 학년={}, 전체 학생 수={}", grade, response.totalStudentCount());
        
        return ApiResponse.success("학년별 성적 분포도 조회 성공", response);
    }
}
