package com.iroomclass.springbackend.domain.admin.statistics.controller;

import com.iroomclass.springbackend.domain.admin.statistics.dto.GradeStatisticsResponse;
import com.iroomclass.springbackend.domain.admin.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ResultStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "관리자 - 통계", description = "관리자 통계 관련 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/grade/{grade}")
    @Operation(summary = "학년별 통계 조회", description = "해당 학년의 최근 시험 평균 점수와 오답률 높은 세부 단원 통계를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "통계 조회 성공", content = @Content(schema = @Schema(implementation = GradeStatisticsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년 값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 학년의 데이터가 없음")
    })
    public ApiResponse<GradeStatisticsResponse> getGradeStatistics(
            @Parameter(description = "학년 (1, 2, 3)", example = "1") @PathVariable Integer grade) {

        log.info("학년별 통계 조회 요청: 학년={}", grade);

        // 학년 값 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 값: {}", grade);
            return new ApiResponse<>(ResultStatus.ERROR, "잘못된 학년 값입니다. 1-3 사이의 값을 입력해주세요.", null);
        }

        GradeStatisticsResponse response = statisticsService.getGradeStatistics(grade);
        log.info("학년별 통계 조회 성공: 학년={}, 최근 시험 수={}, 오답률 높은 단원 수={}",
                grade, response.getRecentExamAverages().size(), response.getHighErrorRateUnits().size());

        return ApiResponse.success("학년별 통계 조회 성공", response);
    }
}
