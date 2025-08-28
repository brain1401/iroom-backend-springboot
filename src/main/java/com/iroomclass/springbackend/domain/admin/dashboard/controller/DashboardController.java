package com.iroomclass.springbackend.domain.admin.dashboard.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.admin.dashboard.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.admin.dashboard.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.admin.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
     * 학년별 시험 제출 현황 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험별 제출 현황
     */
    @Operation(
        summary = "학년별 시험 제출 현황 조회",
        description = "특정 학년의 시험별 제출 현황을 조회합니다"
    )
    @GetMapping("/grade/{grade}/submission-status")
    public ApiResponse<GradeSubmissionStatusResponse> getGradeSubmissionStatus(
            @Parameter(description = "학년", example = "1") 
            @PathVariable Integer grade) {
        log.info("학년별 시험 제출 현황 조회 요청: 학년={}", grade);
        
        GradeSubmissionStatusResponse response = dashboardService.getGradeSubmissionStatus(grade);
        
        log.info("학년별 시험 제출 현황 조회 완료: 학년={}, 시험 수={}", grade, response.getTotalExamCount());
        
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
        description = "특정 학년의 성적 분포도를 조회합니다"
    )
    @GetMapping("/grade/{grade}/score-distribution")
    public ApiResponse<GradeScoreDistributionResponse> getGradeScoreDistribution(
            @Parameter(description = "학년", example = "1") 
            @PathVariable Integer grade) {
        log.info("학년별 성적 분포도 조회 요청: 학년={}", grade);
        
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(grade);
        
        log.info("학년별 성적 분포도 조회 완료: 학년={}, 전체 학생 수={}", grade, response.getTotalStudentCount());
        
        return ApiResponse.success("학년별 성적 분포도 조회 성공", response);
    }
}
