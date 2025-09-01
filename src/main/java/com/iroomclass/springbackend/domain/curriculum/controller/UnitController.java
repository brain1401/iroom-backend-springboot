package com.iroomclass.springbackend.domain.curriculum.controller;

import java.util.List;
import java.util.UUID;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.curriculum.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.curriculum.dto.UnitStatisticsResponse;
import com.iroomclass.springbackend.domain.curriculum.dto.UnitTreeResponse;
import com.iroomclass.springbackend.domain.curriculum.service.UnitService;
import com.iroomclass.springbackend.domain.curriculum.service.UnitService.UnitQuestionInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/admin/units")
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
    @Operation(summary = "학년별 단원 목록 조회", description = "특정 학년의 모든 단원 목록을 조회합니다. 각 단원별 문제 수와 함께 제공됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "잘못된 학년", summary = "학년 값이 올바르지 않음", value = """
                    {
                      "result": "ERROR",
                      "message": "파라미터 'grade'의 값이 올바르지 않습니다",
                      "data": null
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "단원 없음", summary = "해당 학년의 단원을 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "해당 학년의 단원을 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<UnitListResponse> getUnitsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") @PathVariable int grade) {
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
    @Operation(summary = "학년별 단원 통계 조회", description = "특정 학년의 단원별 문제 수와 난이도별 분포 통계를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "잘못된 학년", summary = "학년 값이 올바르지 않음", value = """
                    {
                      "result": "ERROR",
                      "message": "파라미터 'grade'의 값이 올바르지 않습니다",
                      "data": null
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "단원 없음", summary = "해당 학년의 단원을 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "해당 학년의 단원을 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<UnitStatisticsResponse> getUnitStatisticsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") @PathVariable int grade) {
        log.info("학년 {} 단원 통계 조회 요청", grade);

        UnitStatisticsResponse response = unitService.getUnitStatisticsByGrade(grade);

        log.info("학년 {} 단원 통계 조회 성공: {}개 단원", grade, response.totalStat().totalUnits());

        return ApiResponse.success("학년별 단원 통계 조회 성공", response);
    }

    /**
     * 전체 단원 트리 구조 조회
     * 
     * 대분류 → 중분류 → 세부단원의 계층 구조를 조회합니다.
     * 문제 직접 선택 시스템에서 사용됩니다.
     * 
     * @return 전체 단원 트리 구조
     */
    @GetMapping("/tree")
    @Operation(summary = "단원 트리 구조 조회", description = "대분류 → 중분류 → 세부단원의 계층 구조를 조회합니다. 문제 직접 선택 시스템에서 사용됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "서버 오류", summary = "서버 내부 오류 발생", value = """
                    {
                      "result": "ERROR",
                      "message": "서버 내부 오류가 발생했습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<List<UnitTreeResponse>> getUnitTree() {
        log.info("단원 트리 구조 조회 요청");

        List<UnitTreeResponse> response = unitService.getUnitTree();

        log.info("단원 트리 구조 조회 성공: {}개 대분류", response.size());

        return ApiResponse.success("단원 트리 구조 조회 성공", response);
    }

    /**
     * 단원별 문제 목록 조회
     * 
     * 특정 단원에 속한 문제들을 페이징으로 조회합니다.
     * 문제 직접 선택 시스템에서 사용됩니다.
     * 
     * @param unitId   단원 ID
     * @param pageable 페이징 정보 (기본: 0페이지, 20개씩)
     * @return 페이징된 문제 목록
     */
    @GetMapping("/{unitId}/questions")
    @Operation(summary = "단원별 문제 목록 조회", description = """
            특정 단원에 속한 문제들을 페이징으로 조회합니다.

            조회 가능한 정보:
            - 문제 ID, 문제 유형, 난이도
            - 단원 정보
            - 페이징 정보 (전체 개수, 페이지 정보)
            """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "잘못된 단원 ID", summary = "단원 ID 형식이 잘못됨", value = """
                    {
                      "result": "ERROR",
                      "message": "파라미터 'unitId'의 값이 올바르지 않습니다",
                      "data": null
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "단원 없음", summary = "단원을 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "단원을 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<Page<UnitQuestionInfo>> getUnitQuestions(
            @Parameter(description = "단원 ID", example = "1", required = true) @PathVariable UUID unitId,
            @Parameter(description = "페이징 정보 (page=0, size=20, sort=id)") @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        log.info("단원 {} 문제 목록 조회 요청 (페이지: {}, 크기: {})",
                unitId, pageable.getPageNumber(), pageable.getPageSize());

        Page<UnitQuestionInfo> response = unitService.getUnitQuestions(unitId, pageable);

        log.info("단원 {} 문제 목록 조회 성공: {}개 문제 (전체 {}개 중 {}페이지)",
                unitId, response.getNumberOfElements(), response.getTotalElements(), response.getNumber() + 1);

        return ApiResponse.success("단원별 문제 목록 조회 성공", response);
    }
}