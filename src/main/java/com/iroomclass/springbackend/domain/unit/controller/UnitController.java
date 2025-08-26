package com.iroomclass.springbackend.domain.unit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.unit.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.unit.dto.UnitStatisticsResponse;
import com.iroomclass.springbackend.domain.unit.service.UnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 단원 관리 컨트롤러
 * 
 * <p>학년별 단원 목록 조회, 통계 정보를 제공하는 API입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/unit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "단원 관리", description = "단원 조회 및 관리 API")
public class UnitController {
    
    private final UnitService unitService;
    
    /**
     * 특정 학년의 단원 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 단원 목록과 각 단원별 문제 수
     */
    @GetMapping("/grade/{grade}")
    @Operation(
        summary = "학년별 단원 목록 조회",
        description = "지정된 학년의 모든 단원 목록과 각 단원별 문제 수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "단원 목록 조회 성공",
            content = @Content(
                schema = @Schema(implementation = UnitListResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 학년 값 (1, 2, 3만 허용)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content
        )
    })
    public ResponseEntity<UnitListResponse> getUnitsByGrade(
        @Parameter(description = "학년 (1: 중1, 2: 중2, 3: 중3)", required = true, example = "2")
        @PathVariable int grade
    ) {
        log.info("학년 {} 단원 목록 조회 요청", grade);
        
        // 학년 값 검증 (1, 2, 3만 허용)
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 값: {}", grade);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            UnitListResponse response = unitService.getUnitsByGrade(grade);
            log.info("학년 {} 단원 목록 조회 성공: {}개 단원", grade, response.getUnits().size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("학년 {} 단원 목록 조회 중 오류 발생: {}", grade, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 학년의 단원별 문제 수 통계 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 단원별 문제 수와 난이도별 분포
     */
    @GetMapping("/statistics/{grade}")
    @Operation(
        summary = "학년별 단원 통계 조회",
        description = "지정된 학년의 단원별 문제 수와 난이도별 분포를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "단원 통계 조회 성공",
            content = @Content(
                schema = @Schema(implementation = UnitStatisticsResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 학년 값 (1, 2, 3만 허용)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content
        )
    })
    public ResponseEntity<UnitStatisticsResponse> getUnitStatisticsByGrade(
        @Parameter(description = "학년 (1: 중1, 2: 중2, 3: 중3)", required = true, example = "2")
        @PathVariable int grade
    ) {
        log.info("학년 {} 단원 통계 조회 요청", grade);
        
        // 학년 값 검증 (1, 2, 3만 허용)
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 값: {}", grade);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            UnitStatisticsResponse response = unitService.getUnitStatisticsByGrade(grade);
            log.info("학년 {} 단원 통계 조회 성공: {}개 단원", grade, response.getUnitStats().size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("학년 {} 단원 통계 조회 중 오류 발생: {}", grade, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}