package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.service.ExamService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

/**
 * 시험 관리 REST API 컨트롤러
 * 
 * <p>시험 조회, 시험 제출 현황 통계, 시험 목록 관리 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "시험 관리 API", description = """
    시험 관리 관련 API입니다.
    
    주요 기능:
    - 시험 상세 정보 조회
    - 시험 제출 현황 통계 조회  
    - 학년별 시험 목록 조회
    - 시험명 검색 및 필터링
    """)
@Slf4j
public class ExamController {
    
    private final ExamService examService;
    private final ExamRepository examRepository;
    
    /**
     * 시험 상세 정보 조회
     */
    @Operation(
        summary = "시험 상세 정보 조회",
        description = """
            시험 ID를 통해 특정 시험의 상세 정보를 조회합니다.
            
            조회 가능한 정보:
            - 기본 시험 정보 (시험명, 학년, 설명)
            - 연관된 시험지 정보 (문제 수, 총 배점)
            - 생성/수정 시간
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시험을 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<ExamDto>> getExam(
        @Parameter(description = "시험 고유 식별자", required = true)
        @PathVariable UUID examId
    ) {
        log.info("시험 상세 조회 요청: examId={}", examId);
        
        try {
            ExamDto exam = examService.findById(examId);
            return ResponseEntity.ok(ApiResponse.success("시험 정보 조회 성공", exam));
        } catch (RuntimeException e) {
            log.warn("시험 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.errorWithType("시험을 찾을 수 없습니다"));
        }
    }
    
    /**
     * 시험 제출 현황 상세 조회
     */
    @Operation(
        summary = "시험 제출 현황 상세 조회",
        description = """
            특정 시험의 제출 현황을 상세히 조회합니다.
            
            제공되는 정보:
            - 시험 기본 정보
            - 제출 통계 (전체 학생 수, 제출 수, 제출률, 미제출 수)
            - 시간별 제출 현황 (시간대별 제출 분포)
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamSubmissionStatusDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시험을 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/{examId}/submission-status")
    public ResponseEntity<ApiResponse<ExamSubmissionStatusDto>> getExamSubmissionStatus(
        @Parameter(description = "시험 고유 식별자", required = true)
        @PathVariable UUID examId
    ) {
        log.info("시험 제출 현황 조회 요청: examId={}", examId);
        
        try {
            ExamSubmissionStatusDto submissionStatus = examService.getExamSubmissionStatus(examId);
            return ResponseEntity.ok(ApiResponse.success("시험 제출 현황 조회 성공", submissionStatus));
        } catch (RuntimeException e) {
            log.warn("시험 제출 현황 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.errorWithType("시험을 찾을 수 없습니다"));
        }
    }
    
    /**
     * 학년별 시험 목록 조회
     */
    @Operation(
        summary = "학년별 시험 목록 조회",
        description = """
            특정 학년의 시험 목록을 페이징으로 조회합니다.
            
            기본 정렬: 최신 생성순 (createdAt DESC)
            지원하는 정렬 기준: id, examName, grade, createdAt
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
    public ResponseEntity<ApiResponse<Page<ExamDto>>> getExamsByGrade(
        @Parameter(description = "학년 (1, 2, 3)", example = "1")
        @RequestParam(required = false) Integer grade,
        
        @Parameter(description = "검색할 시험명 (부분 일치)", example = "중간고사")
        @RequestParam(required = false) String examName,
        
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        @Parameter(hidden = true) Pageable pageable
    ) {
        log.info("시험 목록 조회 요청: grade={}, examName={}, page={}, size={}", 
                grade, examName, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamDto> examPage;
        
        if (grade != null && examName != null && !examName.trim().isEmpty()) {
            // 학년 + 시험명 복합 검색
            examPage = examService.searchByGradeAndExamName(grade, examName.trim(), pageable);
        } else if (grade != null) {
            // 학년별 조회
            examPage = examService.findByGrade(grade, pageable);
        } else if (examName != null && !examName.trim().isEmpty()) {
            // 시험명 검색
            examPage = examService.searchByExamName(examName.trim(), pageable);
        } else {
            // 전체 조회
            examPage = examService.findAll(pageable);
        }
        
        log.info("시험 목록 조회 완료: totalElements={}, totalPages={}", 
                examPage.getTotalElements(), examPage.getTotalPages());
        
        return ResponseEntity.ok(ApiResponse.success("시험 목록 조회 성공", examPage));
    }
    
    /**
     * 전체 시험 목록 조회 (관리자용)
     */
    @Operation(
        summary = "전체 시험 목록 조회",
        description = """
            모든 시험 목록을 페이징으로 조회합니다. (관리자용)
            
            기본 정렬: 최신 생성순 (createdAt DESC)
            지원하는 정렬 기준: id, examName, grade, createdAt
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ExamDto>>> getAllExams(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        @Parameter(hidden = true) Pageable pageable
    ) {
        log.info("전체 시험 목록 조회 요청: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamDto> examPage = examService.findAll(pageable);
        
        log.info("전체 시험 목록 조회 완료: totalElements={}, totalPages={}", 
                examPage.getTotalElements(), examPage.getTotalPages());
        
        return ResponseEntity.ok(ApiResponse.success("전체 시험 목록 조회 성공", examPage));
    }
    
    /**
     * 시험명 검색
     */
    @Operation(
        summary = "시험명으로 검색",
        description = """
            시험명으로 부분 일치 검색을 수행합니다.
            
            검색 특징:
            - 대소문자 무시 (case insensitive)
            - 부분 일치 (LIKE %검색어%)
            - 기본 정렬: 최신 생성순
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "검색 성공",
                content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "검색어가 비어있음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ExamDto>>> searchExams(
        @Parameter(description = "검색할 시험명", required = true, example = "중간고사")
        @RequestParam String examName,
        
        @Parameter(description = "학년 필터 (선택사항)", example = "1")
        @RequestParam(required = false) Integer grade,
        
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        @Parameter(hidden = true) Pageable pageable
    ) {
        if (examName == null || examName.trim().isEmpty()) {
            log.warn("시험명 검색 요청에서 검색어가 비어있음");
            return ResponseEntity.badRequest()
                .body(ApiResponse.errorWithType("검색어를 입력해주세요"));
        }
        
        log.info("시험명 검색 요청: examName={}, grade={}, page={}, size={}", 
                examName, grade, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamDto> examPage;
        
        if (grade != null) {
            examPage = examService.searchByGradeAndExamName(grade, examName.trim(), pageable);
        } else {
            examPage = examService.searchByExamName(examName.trim(), pageable);
        }
        
        log.info("시험명 검색 완료: examName={}, grade={}, totalElements={}", 
                examName, grade, examPage.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success("시험 검색 성공", examPage));
    }
    
    /**
     * 학년별 시험 통계 조회
     */
    @Operation(
        summary = "학년별 시험 통계 조회",
        description = """
            각 학년별 시험 개수 통계를 조회합니다.
            
            제공되는 정보:
            - 학년별 시험 개수
            - 전체 시험 개수
            - 각 학년의 비율
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            )
        }
    )
    @GetMapping("/statistics/by-grade")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamStatisticsByGrade() {
        log.info("학년별 시험 통계 조회 요청");
        
        try {
            // 학년별 시험 개수 조회
            long grade1Count = examRepository.countByGrade(1);
            long grade2Count = examRepository.countByGrade(2);
            long grade3Count = examRepository.countByGrade(3);
            long totalCount = grade1Count + grade2Count + grade3Count;
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("grade1", grade1Count);
            statistics.put("grade2", grade2Count);
            statistics.put("grade3", grade3Count);
            statistics.put("total", totalCount);
            
            // 비율 계산 (전체가 0이 아닐 때만)
            if (totalCount > 0) {
                Map<String, Double> percentages = new HashMap<>();
                percentages.put("grade1Percentage", Math.round((double) grade1Count / totalCount * 100 * 100.0) / 100.0);
                percentages.put("grade2Percentage", Math.round((double) grade2Count / totalCount * 100 * 100.0) / 100.0);
                percentages.put("grade3Percentage", Math.round((double) grade3Count / totalCount * 100 * 100.0) / 100.0);
                statistics.put("percentages", percentages);
            }
            
            log.info("학년별 시험 통계 조회 완료: 1학년={}, 2학년={}, 3학년={}, 전체={}", 
                    grade1Count, grade2Count, grade3Count, totalCount);
            
            return ResponseEntity.ok(ApiResponse.success("학년별 시험 통계 조회 성공", statistics));
        } catch (Exception e) {
            log.error("학년별 시험 통계 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.errorWithType("통계 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 최근 생성된 시험 목록 (대시보드용)
     */
    @Operation(
        summary = "최근 생성된 시험 목록",
        description = """
            최근에 생성된 시험 목록을 조회합니다. (대시보드용)
            
            기본적으로 최근 10개의 시험을 반환하며,
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
    public ResponseEntity<ApiResponse<List<ExamDto>>> getRecentExams(
        @Parameter(description = "조회할 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("최근 시험 목록 조회 요청: limit={}", limit);
        
        try {
            // limit을 페이지 사이즈로 사용하여 최근 시험 조회
            Pageable pageable = PageRequest.of(0, Math.min(limit, 50), 
                Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<ExamDto> recentExamsPage = examService.findAll(pageable);
            List<ExamDto> recentExams = recentExamsPage.getContent();
            
            log.info("최근 시험 목록 조회 완료: count={}", recentExams.size());
            
            return ResponseEntity.ok(ApiResponse.success("최근 시험 목록 조회 성공", recentExams));
        } catch (Exception e) {
            log.error("최근 시험 목록 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.errorWithType("최근 시험 목록 조회 중 오류가 발생했습니다"));
        }
    }
}