package com.iroomclass.springbackend.domain.unit.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.unit.dto.UnitTreeNode;
import com.iroomclass.springbackend.domain.unit.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 단원 관리 컨트롤러
 * 
 * 교육과정의 단원 구조 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "단원 관리 API",
    description = """
        교육과정의 단원 구조 조회 관련 API입니다.
        
        주요 기능:
        - 계층적 단원 트리 구조 조회 (대분류 → 중분류 → 세부단원)
        - 학년별 단원 필터링 조회
        - 시험지 등록을 위한 단원 목록 조회
        """
)
public class UnitController {

    private final UnitService unitService;

    /**
     * 단원 트리 구조 조회 API
     * 
     * @param grade 학년 필터 (선택적, 1/2/3)
     * @return 단원 트리 구조
     */
    @GetMapping("/tree")
    @Operation(
        summary = "단원 트리 구조 조회",
        description = """
            교육과정의 계층적 단원 구조를 트리 형태로 조회합니다.
            
            구조: 대분류 → 중분류 → 세부단원
            - 대분류: 수와 연산, 문자와 식, 함수, 기하, 통계와 확률 등
            - 중분류: 정수와 유리수, 일차방정식, 이차방정식 등
            - 세부단원: 정수의 덧셈, 일차방정식의 해 등
            
            **사용 방법:**
            - 전체 조회: GET /api/units/tree
            - 학년별 조회: GET /api/units/tree?grade=1
            
            **응답 특징:**
            - displayOrder 순서로 정렬된 계층 구조
            - 각 노드는 하위 children 배열 포함
            - 세부단원(Unit)은 학년 정보 포함
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                    implementation = ApiResponse.class
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 학년 값 (1, 2, 3 이외의 값)",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                    implementation = ApiResponse.ErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<UnitTreeNode>>> getUnitsTree(
        @Parameter(
            description = "학년 필터 (선택적)", 
            example = "1",
            schema = @Schema(allowableValues = {"1", "2", "3"})
        )
        @RequestParam(required = false) Integer grade
    ) {
        log.info("단원 트리 구조 조회 API 호출: grade={}", grade);
        
        List<UnitTreeNode> treeNodes;
        
        if (grade != null) {
            // 학년별 조회
            treeNodes = unitService.getUnitsByGradeAsTree(grade);
            log.info("학년별 단원 트리 조회 완료: grade={}, 대분류 {} 개", grade, treeNodes.size());
            return ResponseEntity.ok(
                ApiResponse.success(grade + "학년 단원 트리 구조 조회 성공", treeNodes)
            );
        } else {
            // 전체 조회
            treeNodes = unitService.getAllUnitsAsTree();
            log.info("전체 단원 트리 조회 완료: 대분류 {} 개", treeNodes.size());
            return ResponseEntity.ok(
                ApiResponse.success("전체 단원 트리 구조 조회 성공", treeNodes)
            );
        }
    }
    
    /**
     * 학년별 단원 목록 조회 API (플랫 구조)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 세부단원 목록
     */
    @GetMapping("/list")
    @Operation(
        summary = "학년별 단원 목록 조회",
        description = """
            특정 학년의 세부단원 목록을 평면적 구조로 조회합니다.
            
            **사용 목적:**
            - 시험지 등록 시 단원 선택 목록으로 사용
            - 문제 등록 시 단원 매핑용으로 사용
            
            **응답 특징:**
            - 계층 구조 없이 해당 학년의 모든 세부단원만 반환
            - displayOrder 순서로 정렬
            - 각 단원의 상위 분류 정보는 포함되지 않음
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 학년 값 또는 필수 파라미터 누락"
        )
    })
    public ResponseEntity<ApiResponse<List<UnitTreeNode>>> getUnitsList(
        @Parameter(
            description = "학년 (필수)", 
            example = "1",
            required = true,
            schema = @Schema(allowableValues = {"1", "2", "3"})
        )
        @RequestParam Integer grade
    ) {
        log.info("학년별 단원 목록 조회 API 호출: grade={}", grade);
        
        List<UnitTreeNode> units = unitService.getUnitsByGrade(grade);
        
        return ResponseEntity.ok(
            ApiResponse.success(grade + "학년 단원 목록 조회 성공", units)
        );
    }
}