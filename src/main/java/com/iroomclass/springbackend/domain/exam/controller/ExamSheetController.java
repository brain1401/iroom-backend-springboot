package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetDto;
import com.iroomclass.springbackend.domain.exam.service.ExamSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 문제지 관리 컨트롤러
 * 
 * <p>문제지 조회, 검색, 통계 관련 API를 제공합니다.
 * 선생님이 생성한 문제지들을 효율적으로 관리할 수 있도록 지원합니다.</p>
 */
@RestController
@RequestMapping("/api/exam-sheets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "문제지 관리 API", description = "문제지 조회, 검색, 통계 관련 API")
public class ExamSheetController {

    private final ExamSheetService examSheetService;

    /**
     * 문제지 상세 조회
     */
    @Operation(
        summary = "문제지 상세 조회",
        description = """
            특정 문제지의 상세 정보를 조회합니다.
            
            포함되는 정보:
            - 문제지 기본 정보 (이름, 학년, 생성일시 등)
            - 포함된 문제 목록과 순서
            - 각 문제의 단원 정보
            - 문제별 배점 정보
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamSheetDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "문제지를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/{examSheetId}")
    public ResponseEntity<ApiResponse<ExamSheetDto>> getExamSheet(
        @Parameter(description = "문제지 고유 식별자", required = true)
        @PathVariable UUID examSheetId
    ) {
        log.info("문제지 상세 조회 요청: examSheetId={}", examSheetId);
        
        try {
            ExamSheetDto examSheet = examSheetService.findById(examSheetId);
            log.info("문제지 상세 조회 완료: examSheetId={}, name={}", examSheetId, examSheet.examName());
            
            return ResponseEntity.ok(
                ApiResponse.success("문제지 조회 성공", examSheet)
            );
        } catch (Exception e) {
            log.error("문제지 상세 조회 실패: examSheetId={}, error={}", examSheetId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 문제지 목록 조회 (페이징, 정렬, 검색 지원)
     */
    @Operation(
        summary = "문제지 목록 조회",
        description = """
            문제지 목록을 조회합니다. 페이징, 정렬, 검색이 지원됩니다.
            
            검색 기능:
            - 문제지 이름으로 검색 가능
            - 부분 검색 지원 (LIKE 검색)
            
            정렬 기능:
            - 생성일시 기준 정렬 (기본값)
            - 문제지 이름 기준 정렬
            - 오름차순/내림차순 지원
            
            필터링 기능:
            - 특정 학년 필터링 가능
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExamSheetDto>>> getExamSheets(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "정렬 기준 필드", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sort,
        
        @Parameter(description = "정렬 방향", example = "desc")
        @RequestParam(defaultValue = "desc") String direction,
        
        @Parameter(description = "학년 필터 (1, 2, 3)", example = "1")
        @RequestParam(required = false) Integer grade,
        
        @Parameter(description = "검색 키워드 (문제지 이름)", example = "수학")
        @RequestParam(required = false) String search
    ) {
        log.info("문제지 목록 조회 요청: page={}, size={}, sort={}, direction={}, grade={}, search={}", 
                page, size, sort, direction, grade, search);
        
        try {
            // 정렬 방향 설정
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<ExamSheetDto> examSheets = examSheetService.findAll(pageable, grade, search);
            
            log.info("문제지 목록 조회 완료: totalElements={}, totalPages={}, currentPage={}", 
                    examSheets.getTotalElements(), examSheets.getTotalPages(), examSheets.getNumber());
            
            return ResponseEntity.ok(
                ApiResponse.success("문제지 목록 조회 성공", examSheets)
            );
        } catch (Exception e) {
            log.error("문제지 목록 조회 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 학년별 문제지 통계
     */
    @Operation(
        summary = "학년별 문제지 통계",
        description = """
            학년별 문제지 생성 통계를 조회합니다.
            
            제공되는 정보:
            - 각 학년별 문제지 개수
            - 전체 문제지 개수
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            )
        }
    )
    @GetMapping("/statistics/by-grade")
    public ResponseEntity<ApiResponse<ExamSheetService.GradeStatistics>> getExamSheetStatisticsByGrade() {
        log.info("학년별 문제지 통계 조회 요청");
        
        try {
            ExamSheetService.GradeStatistics statistics = examSheetService.getStatisticsByGrade();
            log.info("학년별 문제지 통계 조회 완료: {}", statistics);
            
            return ResponseEntity.ok(
                ApiResponse.success("학년별 문제지 통계 조회 성공", statistics)
            );
        } catch (Exception e) {
            log.error("학년별 문제지 통계 조회 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 최근 생성된 문제지 목록
     */
    @Operation(
        summary = "최근 생성된 문제지 목록",
        description = """
            최근에 생성된 문제지 목록을 조회합니다.
            생성일시 기준 내림차순으로 정렬됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            )
        }
    )
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Page<ExamSheetDto>>> getRecentExamSheets(
        @Parameter(description = "조회할 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("최근 문제지 목록 조회 요청: limit={}", limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ExamSheetDto> recentExamSheets = examSheetService.findAll(pageable, null, null);
            
            log.info("최근 문제지 목록 조회 완료: count={}", recentExamSheets.getNumberOfElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("최근 문제지 목록 조회 성공", recentExamSheets)
            );
        } catch (Exception e) {
            log.error("최근 문제지 목록 조회 실패: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 문제지별 사용 현황 (시험 발행 횟수)
     */
    @Operation(
        summary = "문제지별 사용 현황",
        description = """
            특정 문제지가 실제 시험으로 발행된 횟수를 조회합니다.
            문제지의 활용도를 파악할 수 있습니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "문제지를 찾을 수 없음"
            )
        }
    )
    @GetMapping("/{examSheetId}/usage")
    public ResponseEntity<ApiResponse<ExamSheetService.UsageStatistics>> getExamSheetUsage(
        @Parameter(description = "문제지 고유 식별자", required = true)
        @PathVariable UUID examSheetId
    ) {
        log.info("문제지 사용 현황 조회 요청: examSheetId={}", examSheetId);
        
        try {
            ExamSheetService.UsageStatistics usage = examSheetService.getUsageStatistics(examSheetId);
            log.info("문제지 사용 현황 조회 완료: examSheetId={}, usageCount={}", 
                    examSheetId, usage.usageCount());
            
            return ResponseEntity.ok(
                ApiResponse.success("문제지 사용 현황 조회 성공", usage)
            );
        } catch (Exception e) {
            log.error("문제지 사용 현황 조회 실패: examSheetId={}, error={}", examSheetId, e.getMessage(), e);
            throw e;
        }
    }
}