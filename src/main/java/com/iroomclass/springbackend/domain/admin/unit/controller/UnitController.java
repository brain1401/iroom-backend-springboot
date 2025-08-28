package com.iroomclass.springbackend.domain.admin.unit.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.admin.unit.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.admin.unit.dto.UnitStatisticsResponse;
import com.iroomclass.springbackend.domain.admin.unit.service.UnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 단원 관리 컨트롤러
 * 
 * 학년별 단원 목록 조회, 통계 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/unit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "단원 관리", description = "학년별 단원 목록 조회, 통계 API")
public class UnitController {
    
    private final UnitService unitService;
    
    /**
     * 학년별 단원 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 단원 목록과 각 단원별 문제 수
     */
    @GetMapping("/grade/{grade}")
    @Operation(
        summary = "학년별 단원 목록 조회",
        description = "특정 학년의 모든 단원 목록을 조회합니다. 각 단원별 문제 수와 함께 제공됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 학년의 단원을 찾을 수 없음")
    })
    public ApiResponse<UnitListResponse> getUnitsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") 
            @PathVariable int grade) {
        log.info("학년 {} 단원 목록 조회 요청", grade);
        
        UnitListResponse response = unitService.getUnitsByGrade(grade);
        
        log.info("학년 {} 단원 목록 조회 성공: {}개 단원", grade, response.totalUnits());
        
        return ApiResponse.success("학년별 단원 목록 조회 성공", response);
    }
    
    /**
     * 학년별 단원 통계 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 단원별 문제 수와 난이도별 분포
     */
    @GetMapping("/grade/{grade}/statistics")
    @Operation(
        summary = "학년별 단원 통계 조회",
        description = "특정 학년의 단원별 문제 수와 난이도별 분포 통계를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 학년의 단원을 찾을 수 없음")
    })
    public ApiResponse<UnitStatisticsResponse> getUnitStatisticsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") 
            @PathVariable int grade) {
        log.info("학년 {} 단원 통계 조회 요청", grade);
        
        UnitStatisticsResponse response = unitService.getUnitStatisticsByGrade(grade);
        
        log.info("학년 {} 단원 통계 조회 성공: {}개 단원", grade, response.totalStat().totalUnits());
        
        return ApiResponse.success("학년별 단원 통계 조회 성공", response);
    }
}