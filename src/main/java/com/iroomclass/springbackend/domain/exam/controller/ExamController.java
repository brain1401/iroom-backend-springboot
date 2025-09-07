package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.exam.dto.CreateExamRequest;
import com.iroomclass.springbackend.domain.exam.dto.CreateExamResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamAttendeeDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamFilterRequest;
import com.iroomclass.springbackend.domain.exam.dto.ExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamWithUnitsDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitSummaryDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitNameDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamQuestionsResponseDto;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 시험 관리 REST API 컨트롤러 (개선된 버전)
 * 
 * <p>
 * 통합 필터링과 쿼리 파라미터를 통한 시험 관리 API를 제공합니다.
 * 기존의 중복된 엔드포인트들을 하나로 통합하여 일관성 있는 API를 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@Tag(name = "시험 관리 API", description = """
        시험 관리 관련 통합 API입니다.

        주요 기능:
        - 시험 상세 정보 조회
        - 시험 제출 현황 통계 조회
        - 통합 필터링을 통한 시험 목록 조회
        - 학년별, 검색어, 최근 시험 등 다양한 필터링 지원
        - 시험 통계 조회
        """)
@Slf4j
public class ExamController {

    private final ExamService examService;
    private final ExamRepository examRepository;

    /**
     * 시험 생성
     */
    @Operation(summary = "시험 생성", description = """
            선택한 시험지를 기반으로 새로운 시험을 생성합니다.

            **필수 정보:**
            - examName: 시험명 (100자 이하)
            - examSheetId: 기존에 생성된 시험지 ID

            **선택 정보:**
            - description: 시험 설명 (500자 이하)
            - startDate: 시험 시작일시
            - endDate: 시험 종료일시
            - duration: 시험 제한시간 (분 단위)

            **인증:**
            - httpOnly 쿠키를 통한 선생님 인증 필요

            **자동 처리:**
            - 시험지의 학년 정보가 자동으로 복사됩니다
            - 총 문제 수와 총점이 시험지에서 자동 계산됩니다
            - 시험 상태는 CREATED로 초기화됩니다
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "시험 생성 성공", content = @Content(schema = @Schema(implementation = CreateExamResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락 또는 유효하지 않은 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "동일한 이름의 시험이 이미 존재")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<?>> createExam(
            @Parameter(description = "시험 생성 요청 정보", required = true) @Valid @RequestBody CreateExamRequest request) {

        log.info("시험 생성 요청: examName={}, examSheetId={}",
                request.examName(), request.examSheetId());

        try {
            CreateExamResponse response = examService.createExam(request);
            log.info("시험 생성 성공: examId={}, examName={}", response.examId(), response.examName());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("시험이 성공적으로 생성되었습니다", response));

        } catch (IllegalArgumentException e) {
            log.warn("시험 생성 실패 - 중복된 시험명: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (RuntimeException e) {
            log.error("시험 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 시험 상세 정보 조회
     */
    @Operation(summary = "시험 상세 정보 조회", description = """
            시험 ID를 통해 특정 시험의 상세 정보를 조회합니다.

            조회 가능한 정보:
            - 기본 시험 정보 (시험명, 학년, 설명)
            - 연관된 시험지 정보 (문제 수, 총 배점)
            - 생성/수정 시간
            - 선택적으로 단원 정보 포함 (includeUnits=true 시)

            **단원 정보 포함 옵션:**
            - `includeUnits=true`: 시험에 포함된 모든 단원 정보와 통계 포함
            - 기본값: `false` (기본 시험 정보만 반환)

            **사용 예시:**
            - 기본 정보만: `/api/exams/{examId}`
            - 단원 정보 포함: `/api/exams/{examId}?includeUnits=true`
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ExamDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<?>> getExam(
            @Parameter(description = "시험 고유 식별자", required = true) @PathVariable UUID examId,

            @Parameter(description = "단원 정보 포함 여부", example = "false") @RequestParam(required = false, defaultValue = "false") Boolean includeUnits) {

        log.info("시험 상세 조회 요청: examId={}, includeUnits={}", examId, includeUnits);

        try {
            if (includeUnits != null && includeUnits) {
                // 단원 정보 포함한 상세 조회
                ExamWithUnitsDto examWithUnits = examService.findByIdWithUnits(examId);
                return ResponseEntity.ok(ApiResponse.success("단원 정보 포함 시험 조회 성공", examWithUnits));
            } else {
                // 기본 시험 정보만 조회
                ExamDto exam = examService.findById(examId);
                return ResponseEntity.ok(ApiResponse.success("시험 정보 조회 성공", exam));
            }
        } catch (RuntimeException e) {
            log.warn("시험 조회 실패: examId={}, includeUnits={}, error={}", examId, includeUnits, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorWithType("시험을 찾을 수 없습니다"));
        }
    }

    /**
     * 시험 제출 현황 상세 조회
     */
    @Operation(summary = "시험 제출 현황 상세 조회", description = """
            특정 시험의 제출 현황을 상세히 조회합니다.

            제공되는 정보:
            - 시험 기본 정보
            - 제출 통계 (전체 학생 수, 제출 수, 제출률, 미제출 수)
            - 시간별 제출 현황 (시간대별 제출 분포)
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ExamSubmissionStatusDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{examId}/submission-status")
    public ResponseEntity<ApiResponse<ExamSubmissionStatusDto>> getExamSubmissionStatus(
            @Parameter(description = "시험 고유 식별자", required = true) @PathVariable UUID examId) {
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
     * 시험 목록 조회 (통합 필터링)
     * 
     * <p>
     * 다양한 필터링 조건을 조합하여 시험 목록을 조회합니다.
     * </p>
     * 
     * <h3>지원되는 필터링 조건:</h3>
     * <ul>
     * <li>학년별 필터: ?grade=1</li>
     * <li>시험명 검색: ?search=중간고사</li>
     * <li>최근 시험: ?recent=true</li>
     * <li>복합 필터: ?grade=1&search=중간고사</li>
     * </ul>
     * 
     * <h3>페이징 및 정렬:</h3>
     * <ul>
     * <li>페이징: ?page=0&size=20</li>
     * <li>정렬: ?sort=createdAt,desc&sort=examName,asc</li>
     * </ul>
     */
    @Operation(summary = "시험 목록 조회 (통합 필터링)", description = """
            다양한 필터링 조건을 조합하여 시험 목록을 조회합니다.

            **지원되는 필터링:**
            - 학년별: `grade=1,2,3`
            - 시험명 검색: `search=중간고사` (부분 일치, 대소문자 무관)
            - 최근 시험: `recent=true` (최근 생성된 시험만)
            - 단원 정보 포함: `includeUnits=true` (각 시험의 단원 정보 포함)
            - 복합 필터: 여러 조건 동시 적용 가능

            **기본 정렬:** 최신 생성순 (createdAt DESC)
            **지원 정렬:** id, examName, grade, createdAt

            **사용 예시:**
            - 전체 조회: `/api/exams`
            - 1학년 시험: `/api/exams?grade=1`
            - 검색: `/api/exams?search=중간고사`
            - 최근 시험: `/api/exams?recent=true`
            - 단원 정보 포함: `/api/exams?includeUnits=true`
            - 복합: `/api/exams?grade=2&search=수학&includeUnits=true&page=0&size=10`

            **기존 엔드포인트 통합:**
            - `/api/exams/all` → `/api/exams` (파라미터 없이)
            - `/api/exams/recent` → `/api/exams?recent=true`
            - `/api/exams/search` → `/api/exams?search={keyword}`
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getExams(
            @Parameter(description = "학년 필터 (1, 2, 3)", example = "1") @RequestParam(required = false) Integer grade,

            @Parameter(description = "시험명 검색어 (부분 일치)", example = "중간고사") @RequestParam(required = false) String search,

            @Parameter(description = "최근 생성된 시험만 조회", example = "true") @RequestParam(required = false) Boolean recent,

            @Parameter(description = "단원 정보 포함 여부", example = "false") @RequestParam(required = false, defaultValue = "false") Boolean includeUnits,

            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(hidden = true) Pageable pageable) {

        // 필터 객체 생성
        ExamFilterRequest filter = new ExamFilterRequest(grade, search, recent, null);

        log.info("시험 목록 조회 요청: {}, page={}, size={}",
                filter.getFilterDescription(), pageable.getPageNumber(), pageable.getPageSize());

        // 입력 검증
        if (filter.hasGradeFilter() && (filter.grade() < 1 || filter.grade() > 3)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }

        if (filter.hasSearchFilter() && filter.search().length() > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("검색어는 100자 이하여야 합니다"));
        }

        try {
            if (includeUnits != null && includeUnits) {
                // 단원 정보 포함 조회
                Page<ExamWithUnitsDto> examPage = examService.findExamsWithFilterAndUnits(filter, pageable);

                log.info("단원 정보 포함 시험 목록 조회 완료: {}, totalElements={}, totalPages={}",
                        filter.getFilterDescription(), examPage.getTotalElements(), examPage.getTotalPages());

                return ResponseEntity.ok(ApiResponse.success("단원 정보 포함 시험 목록 조회 성공", examPage));
            } else {
                // 기본 시험 정보만 조회
                Page<ExamDto> examPage = examService.findExamsWithFilter(filter, pageable);

                log.info("시험 목록 조회 완료: {}, totalElements={}, totalPages={}",
                        filter.getFilterDescription(), examPage.getTotalElements(), examPage.getTotalPages());

                return ResponseEntity.ok(ApiResponse.success("시험 목록 조회 성공", examPage));
            }

        } catch (Exception e) {
            log.error("시험 목록 조회 실패: {}, error={}", filter.getFilterDescription(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("시험 목록 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 시험 통계 조회 (통합)
     * 
     * <p>
     * 다양한 시험 통계를 조회합니다.
     * </p>
     */
    @Operation(summary = "시험 통계 조회", description = """
            다양한 시험 통계를 조회합니다.

            **지원되는 통계 타입:**
            - `by-grade`: 학년별 시험 개수 통계

            **사용 예시:**
            - 학년별 통계: `/api/exams/statistics?type=by-grade`

            **기존 엔드포인트 통합:**
            - `/api/exams/statistics/by-grade` → `/api/exams/statistics?type=by-grade`
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 통계 타입", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamStatistics(
            @Parameter(description = "통계 타입 (by-grade)", example = "by-grade", required = true) @RequestParam String type) {

        log.info("시험 통계 조회 요청: type={}", type);

        if (type == null || type.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("통계 타입을 지정해주세요"));
        }

        try {
            Map<String, Object> statistics = examService.getExamStatistics(type.trim());

            log.info("시험 통계 조회 완료: type={}, resultKeys={}", type, statistics.keySet());

            return ResponseEntity.ok(ApiResponse.success("시험 통계 조회 성공", statistics));

        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 통계 타입: type={}", type);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType(e.getMessage()));

        } catch (Exception e) {
            log.error("시험 통계 조회 실패: type={}, error={}", type, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("통계 조회 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 문제 조회 관련 엔드포인트
    // ========================================

    /**
     * 시험 문제 조회 (학생 제출용)
     */
    @Operation(summary = "시험 문제 조회 (학생 제출용)", description = """
            특정 시험에 포함된 모든 문제 정보를 조회합니다.
            학생이 시험을 제출할 때 필요한 모든 정보를 제공합니다.

            **제공되는 정보:**
            - 시험 기본 정보 (시험명, 학년)
            - 문제 통계 (총 문제 수, 객관식/주관식 개수, 총 배점)
            - 문제별 상세 정보 (순서, 유형, 내용, 배점, 선택지, 이미지)
            - 문제 유형별 비율 정보

            **정렬 순서:**
            - 문제 순서(seqNo) 오름차순 정렬

            **활용 목적:**
            - 학생 시험 제출 페이지
            - 객관식/주관식 답안 입력 인터페이스 구성
            - 이미지 스캔 및 인식을 위한 문제 정보 제공

            **사용 예시:**
            - `/api/exams/{examId}/questions`

            **성능 최적화:**
            - JOIN FETCH를 사용하여 N+1 쿼리 방지
            - 필요한 정보만 선택적 로딩
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ExamQuestionsResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<ExamQuestionsResponseDto>> getExamQuestions(
            @Parameter(description = "시험 고유 식별자", required = true) @PathVariable UUID examId) {

        log.info("시험 문제 조회 요청: examId={}", examId);

        try {
            ExamQuestionsResponseDto examQuestions = examService.findExamQuestions(examId);

            log.info("시험 문제 조회 완료: examId={}, totalQuestions={}, multipleChoice={}, subjective={}",
                    examId, examQuestions.totalQuestions(), examQuestions.multipleChoiceCount(),
                    examQuestions.subjectiveCount());

            return ResponseEntity.ok(ApiResponse.success("시험 문제 조회 성공", examQuestions));

        } catch (RuntimeException e) {
            log.warn("시험 문제 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorWithType("시험을 찾을 수 없습니다"));

        } catch (Exception e) {
            log.error("시험 문제 조회 중 예상치 못한 오류: examId={}, error={}", examId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("시험 문제 조회 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 단원 정보 관련 엔드포인트
    // ========================================

    /**
     * 특정 시험의 단원 정보 목록 조회
     */
    @Operation(summary = "시험별 단원 정보 목록 조회", description = """
            특정 시험에 포함된 모든 단원의 상세 정보를 조회합니다.

            **제공되는 정보:**
            - 단원 기본 정보 (단원명, 코드, 학년)
            - 계층 구조 정보 (대분류 > 중분류 > 단원)
            - 표시 순서 정보

            **정렬 순서:**
            - 대분류 표시 순서 → 중분류 표시 순서 → 단원 표시 순서

            **사용 예시:**
            - `/api/exams/{examId}/units`

            **성능 최적화:**
            - Projection 인터페이스를 사용하여 필요한 정보만 조회
            - N+1 쿼리 문제 방지
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    @GetMapping("/{examId}/units")
    public ResponseEntity<ApiResponse<List<UnitNameDto>>> getExamUnits(
            @Parameter(description = "시험 고유 식별자", required = true) @PathVariable UUID examId) {

        log.info("시험별 단원 이름 조회 요청: examId={}", examId);

        try {
            // 시험 존재 여부 확인을 위해 기본 정보 조회
            examService.findById(examId);

            // 단원 이름 정보 조회 (간소화된 버전)
            List<UnitNameDto> units = examService.findUnitNamesByExamId(examId);

            log.info("시험별 단원 이름 조회 완료: examId={}, unitCount={}", examId, units.size());

            return ResponseEntity.ok(ApiResponse.success("시험별 단원 이름 조회 성공", units));

        } catch (RuntimeException e) {
            log.warn("시험별 단원 정보 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorWithType("시험을 찾을 수 없습니다"));
        }
    }

    /**
     * 특정 시험의 단원별 문제 수 통계 조회
     */
    @Operation(summary = "시험별 단원 문제 수 통계 조회", description = """
            특정 시험의 각 단원별 문제 수와 배점 통계를 조회합니다.

            **제공되는 정보:**
            - 단원별 문제 개수
            - 단원별 총 배점
            - 단원명 및 식별자

            **사용 예시:**
            - `/api/exams/{examId}/unit-stats`

            **활용 목적:**
            - 시험 분석 및 리포트 생성
            - 단원별 출제 비중 파악
            - 교육과정 분석
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    @GetMapping("/{examId}/unit-stats")
    public ResponseEntity<ApiResponse<List<ExamWithUnitsDto.UnitQuestionCount>>> getExamUnitStats(
            @Parameter(description = "시험 고유 식별자", required = true) @PathVariable UUID examId) {

        log.info("시험별 단원 통계 조회 요청: examId={}", examId);

        try {
            List<ExamWithUnitsDto.UnitQuestionCount> unitStats = examService.getUnitQuestionCounts(examId);

            log.info("시험별 단원 통계 조회 완료: examId={}, statCount={}", examId, unitStats.size());

            return ResponseEntity.ok(ApiResponse.success("시험별 단원 통계 조회 성공", unitStats));

        } catch (Exception e) {
            log.error("시험별 단원 통계 조회 실패: examId={}, error={}", examId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("단원 통계 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 여러 시험의 단원 정보 배치 조회
     */
    @Operation(summary = "다중 시험 단원 정보 배치 조회", description = """
            여러 시험의 단원 정보를 한 번에 효율적으로 조회합니다.

            **사용 시나리오:**
            - 대시보드에서 여러 시험의 단원 정보 표시
            - 시험 비교 분석
            - 배치 처리 작업

            **성능 최적화:**
            - 단일 쿼리로 여러 시험 정보 조회
            - N+1 쿼리 문제 방지
            - @EntityGraph 활용

            **요청 본문 예시:**
            ```json
            {
              "examIds": [
                "550e8400-e29b-41d4-a716-446655440000",
                "550e8400-e29b-41d4-a716-446655440001"
              ]
            }
            ```

            **제한사항:**
            - 최대 50개 시험까지 동시 조회 가능
            - 대용량 데이터 조회 시 타임아웃 가능성
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/batch/units")
    public ResponseEntity<ApiResponse<List<ExamWithUnitsDto>>> getExamsBatchWithUnits(
            @Parameter(description = "조회할 시험 ID 목록", required = true) @RequestBody BatchExamRequest request) {

        log.info("다중 시험 단원 정보 배치 조회 요청: examCount={}",
                request != null ? request.examIds().size() : 0);

        // 입력 검증
        if (request == null || request.examIds() == null || request.examIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("시험 ID 목록이 필요합니다"));
        }

        if (request.examIds().size() > 50) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("한 번에 최대 50개 시험까지 조회 가능합니다"));
        }

        try {
            List<ExamWithUnitsDto> results = examService.findByIdsWithUnits(request.examIds());

            log.info("다중 시험 단원 정보 배치 조회 완료: requestCount={}, resultCount={}",
                    request.examIds().size(), results.size());

            return ResponseEntity.ok(ApiResponse.success(
                    "다중 시험 단원 정보 배치 조회 성공", results));

        } catch (Exception e) {
            log.error("다중 시험 단원 정보 배치 조회 실패: examCount={}, error={}",
                    request.examIds().size(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("배치 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 학년별 사용된 단원 목록 조회
     */
    @Operation(summary = "학년별 사용된 단원 목록 조회", description = """
            특정 학년의 모든 시험에서 사용된 단원 목록을 중복 제거하여 조회합니다.

            **활용 목적:**
            - 학년별 커리큘럼 분석
            - 단원별 출제 빈도 분석
            - 교육과정 완성도 평가

            **제공되는 정보:**
            - 해당 학년 시험에 실제로 출제된 단원만
            - 중복 제거된 유니크 단원 목록
            - 단원별 기본 정보

            **사용 예시:**
            - 1학년 사용 단원: `/api/exams/units/by-grade?grade=1`
            - 2학년 사용 단원: `/api/exams/units/by-grade?grade=2`
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년")
    })
    @GetMapping("/units/by-grade")
    public ResponseEntity<ApiResponse<List<UnitSummaryDto>>> getUnitsByGrade(
            @Parameter(description = "학년 (1, 2, 3)", required = true, example = "1") @RequestParam Integer grade) {

        log.info("학년별 사용된 단원 목록 조회 요청: grade={}", grade);

        // 입력 검증
        if (grade == null || grade < 1 || grade > 3) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }

        try {
            List<UnitSummaryDto> units = examService.getDistinctUnitsByGrade(grade);

            log.info("학년별 사용된 단원 목록 조회 완료: grade={}, unitCount={}", grade, units.size());

            return ResponseEntity.ok(ApiResponse.success(
                    grade + "학년 사용 단원 목록 조회 성공", units));

        } catch (Exception e) {
            log.error("학년별 사용된 단원 목록 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.errorWithType("단원 목록 조회 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 요청/응답 DTO
    // ========================================

    /**
     * 배치 시험 조회 요청 DTO
     */
    @Schema(description = "배치 시험 조회 요청 DTO")
    public record BatchExamRequest(
            @Parameter(description = "조회할 시험 ID 목록", required = true) @Schema(description = "시험 ID 목록 (최대 50개)", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"550e8400-e29b-41d4-a716-446655440001\"]") List<UUID> examIds) {
        /**
         * Compact Constructor - 입력 검증
         */
        public BatchExamRequest {
            if (examIds != null && examIds.size() > 50) {
                throw new IllegalArgumentException("시험 ID는 최대 50개까지 지원합니다");
            }
        }
    }

    /**
     * 특정 시험의 응시자 목록 조회 (페이징)
     * 
     * <p>
     * 시험 ID를 기반으로 해당 시험에 응시한 학생들의 정보를
     * 페이징 처리하여 조회합니다.
     * </p>
     * 
     * @param examId 시험 ID
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지당 항목 수
     * @param sort   정렬 기준 (기본값: submittedAt,desc)
     * @return 응시자 목록 페이지
     */
    @GetMapping("/{examId}/attendees")
    @Operation(summary = "시험 응시자 목록 조회", description = """
            특정 시험에 응시한 학생들의 정보를 페이징하여 조회합니다.

            **페이징 파라미터:**
            - page: 페이지 번호 (0부터 시작, 기본값: 0)
            - size: 페이지당 항목 수 (기본값: 20, 최대: 100)
            - sort: 정렬 기준 (기본값: submittedAt,desc)

            **정렬 옵션:**
            - submittedAt,desc: 제출 시간 내림차순 (최신순)
            - submittedAt,asc: 제출 시간 오름차순
            - studentName,asc: 학생 이름 오름차순
            - studentName,desc: 학생 이름 내림차순
            """, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "응시자 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = """
                    {
                        "result": "SUCCESS",
                        "message": "시험 응시자 목록 조회 성공",
                        "data": {
                            "content": [
                                {
                                    "submissionId": "550e8400-e29b-41d4-a716-446655440000",
                                    "studentId": 1,
                                    "studentName": "홍길동",
                                    "studentPhone": "010-1234-5678",
                                    "studentBirthDate": "2008-03-15",
                                    "submittedAt": "2025-01-20T14:30:00",
                                    "examId": "550e8400-e29b-41d4-a716-446655440001",
                                    "examName": "2학년 중간고사"
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "sorted": true,
                                    "ascending": false
                                },
                                "pageNumber": 0,
                                "pageSize": 20
                            },
                            "totalElements": 45,
                            "totalPages": 3,
                            "last": false,
                            "first": true,
                            "numberOfElements": 20
                        }
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = """
                    {
                        "result": "ERROR",
                        "message": "시험을 찾을 수 없습니다",
                        "data": null
                    }
                    """)))
    })
    @Parameters({
            @Parameter(name = "examId", description = "시험 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지당 항목 수", example = "20"),
            @Parameter(name = "sort", description = "정렬 기준", example = "submittedAt,desc")
    })
    public ApiResponse<Page<ExamAttendeeDto>> getExamAttendees(
            @PathVariable UUID examId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다") @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다") int size,
            @RequestParam(defaultValue = "submittedAt,desc") String sort) {

        log.info("시험 응시자 목록 조회 요청: examId={}, page={}, size={}, sort={}",
                examId, page, size, sort);

        try {
            // 정렬 정보 파싱
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            // 페이징 정보 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // 서비스 호출 (최적화 버전 사용)
            Page<ExamAttendeeDto> attendees = examService.getExamAttendeesOptimized(examId, pageable);

            log.info("시험 응시자 목록 조회 성공: examId={}, 총 {}명 중 {}명 반환",
                    examId, attendees.getTotalElements(), attendees.getContent().size());

            return ApiResponse.success("시험 응시자 목록 조회 성공", attendees);

        } catch (RuntimeException e) {
            log.error("시험 응시자 목록 조회 실패: examId={}, error={}", examId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("시험 응시자 목록 조회 중 예상치 못한 오류: examId={}", examId, e);
            throw new RuntimeException("시험 응시자 목록 조회 중 오류가 발생했습니다", e);
        }
    }
}