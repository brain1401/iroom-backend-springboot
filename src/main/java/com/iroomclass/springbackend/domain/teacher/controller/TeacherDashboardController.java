package com.iroomclass.springbackend.domain.teacher.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.teacher.dto.ExamSubmissionDetailDto;
import com.iroomclass.springbackend.domain.teacher.dto.RecentExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.teacher.dto.ExamAverageScoreDto;
import com.iroomclass.springbackend.domain.teacher.dto.ScoreDistributionDto;
import com.iroomclass.springbackend.domain.teacher.dto.StudentAnswerDetailDto;
import com.iroomclass.springbackend.domain.teacher.dto.UnitWrongAnswerRateDto;
import com.iroomclass.springbackend.domain.teacher.service.TeacherDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 선생님 대시보드 REST API 컨트롤러
 * 
 * <p>선생님이 필요로 하는 시험 통계, 제출 현황, 성적 분석 등의
 * 대시보드 관련 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/teacher/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "선생님 대시보드 API", description = """
    선생님 대시보드 관련 API입니다.
    
    주요 기능:
    - 최근 시험 제출 현황 조회
    - 학년별 시험 통계 조회  
    - 시험 제출자 현황 분석
    - 학생별 성적 데이터 조회
    """)
public class TeacherDashboardController {
    
    private final TeacherDashboardService teacherDashboardService;
    
    /**
     * 학년별 최근 시험 제출 현황 조회
     */
    @Operation(
        summary = "학년별 최근 시험 제출 현황 조회",
        description = """
            특정 학년의 최근 시험들의 제출 현황을 조회합니다.
            
            제공되는 정보:
            - 최근 시험 목록 (기본 10개)
            - 각 시험별 제출률
            - 전체 평균 제출률
            - 시험별 문제 개수
            
            시험은 생성일 기준 내림차순으로 정렬됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = RecentExamSubmissionStatusDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 학년 파라미터",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/recent-exams-status")
    public ResponseEntity<ApiResponse<RecentExamSubmissionStatusDto>> getRecentExamsSubmissionStatus(
        @Parameter(description = "학년 (1, 2, 3)", required = true, example = "1")
        @RequestParam Integer grade,
        
        @Parameter(description = "조회할 최근 시험 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("학년별 최근 시험 제출 현황 조회 요청: grade={}, limit={}", grade, limit);
        
        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 파라미터: grade={}", grade);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<RecentExamSubmissionStatusDto>errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }
        
        // 조회 개수 유효성 검증
        if (limit <= 0 || limit > 50) {
            log.warn("잘못된 limit 파라미터: limit={}", limit);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<RecentExamSubmissionStatusDto>errorWithType("조회 개수는 1~50 사이여야 합니다"));
        }
        
        try {
            RecentExamSubmissionStatusDto result = 
                teacherDashboardService.getRecentExamsSubmissionStatus(grade, limit);
            
            log.info("학년별 최근 시험 제출 현황 조회 완료: grade={}, examCount={}, avgRate={}", 
                    grade, result.examCount(), result.averageSubmissionRate());
            
            return ResponseEntity.ok(
                ApiResponse.success("학년별 최근 시험 제출 현황 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("학년별 최근 시험 제출 현황 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<RecentExamSubmissionStatusDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 전체 학년별 최근 시험 제출 현황 조회
     */
    @Operation(
        summary = "전체 학년별 최근 시험 제출 현황 조회",
        description = """
            모든 학년(1, 2, 3학년)의 최근 시험 제출 현황을 한 번에 조회합니다.
            
            대시보드 메인 화면에서 전체 현황을 파악하기 위해 사용됩니다.
            
            반환 형식:
            {
              "1": { grade: 1, examCount: 5, averageSubmissionRate: 85.2, ... },
              "2": { grade: 2, examCount: 7, averageSubmissionRate: 78.9, ... },
              "3": { grade: 3, examCount: 4, averageSubmissionRate: 92.1, ... }
            }
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            )
        }
    )
    @GetMapping("/all-grades-recent-exams-status")
    public ResponseEntity<ApiResponse<Map<Integer, RecentExamSubmissionStatusDto>>> getAllGradesRecentExamsStatus(
        @Parameter(description = "각 학년별 조회할 시험 개수", example = "5")
        @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("전체 학년 최근 시험 제출 현황 조회 요청: limit={}", limit);
        
        // 조회 개수 유효성 검증
        if (limit <= 0 || limit > 20) {
            log.warn("잘못된 limit 파라미터: limit={}", limit);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<Integer, RecentExamSubmissionStatusDto>>errorWithType("조회 개수는 1~20 사이여야 합니다"));
        }
        
        try {
            Map<Integer, RecentExamSubmissionStatusDto> result = 
                teacherDashboardService.getAllGradesRecentExamsStatus(limit);
            
            log.info("전체 학년 최근 시험 제출 현황 조회 완료: grades=[1,2,3], limit={}", limit);
            
            return ResponseEntity.ok(
                ApiResponse.success("전체 학년 최근 시험 제출 현황 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("전체 학년 최근 시험 제출 현황 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Map<Integer, RecentExamSubmissionStatusDto>>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 학년별 시험 통계 요약 정보 조회
     */
    @Operation(
        summary = "학년별 시험 통계 요약 정보 조회",
        description = """
            특정 학년의 전체 시험 통계 요약 정보를 조회합니다.
            
            제공되는 정보:
            - 전체 시험 개수
            - 전체 제출 수
            - 전체 학생 수
            - 평균 제출률
            
            대시보드에서 간단한 통계 표시용으로 사용됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 학년 파라미터"
            )
        }
    )
    @GetMapping("/exam-stats-summary")
    public ResponseEntity<ApiResponse<TeacherDashboardService.ExamStatsSummary>> getExamStatsSummary(
        @Parameter(description = "학년 (1, 2, 3)", required = true, example = "1")
        @RequestParam Integer grade
    ) {
        log.info("학년별 시험 통계 요약 조회 요청: grade={}", grade);
        
        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 파라미터: grade={}", grade);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<TeacherDashboardService.ExamStatsSummary>errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }
        
        try {
            TeacherDashboardService.ExamStatsSummary result = 
                teacherDashboardService.getExamStatsSummary(grade);
            
            log.info("학년별 시험 통계 요약 조회 완료: grade={}, examCount={}, studentCount={}, avgRate={}", 
                    grade, result.totalExamCount(), result.totalStudentCount(), result.getAverageSubmissionRate());
            
            return ResponseEntity.ok(
                ApiResponse.success("학년별 시험 통계 요약 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("학년별 시험 통계 요약 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<TeacherDashboardService.ExamStatsSummary>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 특정 시험의 상세 제출자 현황 조회
     */
    @Operation(
        summary = "특정 시험의 상세 제출자 현황 조회",
        description = """
            특정 시험의 상세 제출자 현황을 조회합니다.
            
            제공되는 정보:
            - 시험 기본 정보 (시험명, 학년, 문제 개수 등)
            - 제출 통계 (전체 학생 수, 제출/미제출 수, 제출률)
            - 제출한 학생 목록 (제출 시간, 소요 시간, 제출 순서 포함)
            - 미제출 학생 목록
            - 평균 제출 소요 시간
            
            선생님이 특정 시험의 제출 현황을 상세히 파악하기 위해 사용됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamSubmissionDetailDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시험을 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/exam-submission-detail")
    public ResponseEntity<ApiResponse<ExamSubmissionDetailDto>> getExamSubmissionDetail(
        @Parameter(description = "시험 ID", required = true)
        @RequestParam UUID examId
    ) {
        log.info("시험 상세 제출자 현황 조회 요청: examId={}", examId);
        
        try {
            ExamSubmissionDetailDto result = teacherDashboardService.getExamSubmissionDetail(examId);
            
            log.info("시험 상세 제출자 현황 조회 완료: examId={}, totalStudents={}, submitted={}, submissionRate={}", 
                    examId, result.statistics().totalStudentCount(), 
                    result.statistics().submittedCount(), result.statistics().submissionRate());
            
            return ResponseEntity.ok(
                ApiResponse.success("시험 상세 제출자 현황 조회 성공", result)
            );
        } catch (RuntimeException e) {
            log.warn("시험 상세 제출자 현황 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<ExamSubmissionDetailDto>errorWithType("시험을 찾을 수 없습니다"));
        } catch (Exception e) {
            log.error("시험 상세 제출자 현황 조회 실패: examId={}, error={}", examId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ExamSubmissionDetailDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 특정 학생의 특정 시험 상세 답안 조회
     */
    @Operation(
        summary = "학생별 특정 시험 상세 답안 조회",
        description = """
            특정 학생의 특정 시험에 대한 상세 답안을 조회합니다.
            
            제공되는 정보:
            - 학생 기본 정보 (이름, 전화번호, 생년월일)
            - 시험 기본 정보 (시험명, 학년, 생성일)
            - 제출 정보 (제출 시간, 소요 시간)
            - 채점 결과 (총점, 채점 상태, 버전, 코멘트)
            - 문제별 상세 답안
              * 문제 내용 및 정답
              * 학생 답안
              * 획득 점수 및 만점
              * 정답 여부
              * 문제별 피드백
              * 관련 단원 정보
            
            선생님이 개별 학생의 시험 성과를 상세히 분석하기 위해 사용됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StudentAnswerDetailDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시험 또는 학생을 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 파라미터",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/student-answer-detail")
    public ResponseEntity<ApiResponse<StudentAnswerDetailDto>> getStudentAnswerDetail(
        @Parameter(description = "시험 ID", required = true)
        @RequestParam UUID examId,
        
        @Parameter(description = "학생 ID", required = true, example = "1")
        @RequestParam Long studentId
    ) {
        log.info("학생별 상세 답안 조회 요청: examId={}, studentId={}", examId, studentId);
        
        // 파라미터 유효성 검증
        if (examId == null) {
            log.warn("시험 ID가 누락됨");
            return ResponseEntity.badRequest()
                .body(ApiResponse.<StudentAnswerDetailDto>errorWithType("시험 ID는 필수입니다"));
        }
        
        if (studentId == null || studentId <= 0) {
            log.warn("잘못된 학생 ID: studentId={}", studentId);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<StudentAnswerDetailDto>errorWithType("올바른 학생 ID를 입력해주세요"));
        }
        
        try {
            StudentAnswerDetailDto result = teacherDashboardService.getStudentAnswerDetail(examId, studentId);
            
            log.info("학생별 상세 답안 조회 완료: examId={}, studentId={}, totalScore={}, questionCount={}", 
                    examId, studentId, result.gradingResult().totalScore(), result.questionAnswers().size());
            
            return ResponseEntity.ok(
                ApiResponse.success("학생별 상세 답안 조회 성공", result)
            );
        } catch (RuntimeException e) {
            log.warn("학생별 상세 답안 조회 실패: examId={}, studentId={}, error={}", examId, studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<StudentAnswerDetailDto>errorWithType(e.getMessage()));
        } catch (Exception e) {
            log.error("학생별 상세 답안 조회 실패: examId={}, studentId={}, error={}", examId, studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<StudentAnswerDetailDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 특정 시험의 모든 학생 상세 답안 조회 (페이징)
     */
    @Operation(
        summary = "특정 시험의 모든 학생 상세 답안 조회",
        description = """
            특정 시험에 제출한 모든 학생의 상세 답안을 페이징으로 조회합니다.
            
            페이징 파라미터:
            - page: 페이지 번호 (0부터 시작)
            - size: 페이지 크기 (기본값: 20, 최대 100)
            
            정렬 기준:
            - 학생 이름 오름차순
            
            대량의 학생 답안 데이터를 효율적으로 처리하기 위해 페이징을 사용합니다.
            선생님이 전체 학생의 답안을 일괄 분석할 때 사용됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시험을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 페이징 파라미터"
            )
        }
    )
    @GetMapping("/all-students-answer-details")
    public ResponseEntity<ApiResponse<List<StudentAnswerDetailDto>>> getAllStudentsAnswerDetails(
        @Parameter(description = "시험 ID", required = true)
        @RequestParam UUID examId,
        
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "페이지 크기 (최대 100)", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("시험의 모든 학생 상세 답안 조회 요청: examId={}, page={}, size={}", examId, page, size);
        
        // 파라미터 유효성 검증
        if (examId == null) {
            log.warn("시험 ID가 누락됨");
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<StudentAnswerDetailDto>>errorWithType("시험 ID는 필수입니다"));
        }
        
        if (page < 0) {
            log.warn("잘못된 페이지 번호: page={}", page);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<StudentAnswerDetailDto>>errorWithType("페이지 번호는 0 이상이어야 합니다"));
        }
        
        if (size <= 0 || size > 100) {
            log.warn("잘못된 페이지 크기: size={}", size);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<StudentAnswerDetailDto>>errorWithType("페이지 크기는 1~100 사이여야 합니다"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<StudentAnswerDetailDto> result = 
                teacherDashboardService.getAllStudentsAnswerDetails(examId, pageable);
            
            log.info("시험의 모든 학생 상세 답안 조회 완료: examId={}, resultCount={}, page={}, size={}", 
                    examId, result.size(), page, size);
            
            return ResponseEntity.ok(
                ApiResponse.success("시험의 모든 학생 상세 답안 조회 성공", result)
            );
        } catch (RuntimeException e) {
            log.warn("시험의 모든 학생 상세 답안 조회 실패: examId={}, error={}", examId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<List<StudentAnswerDetailDto>>errorWithType("시험을 찾을 수 없습니다"));
        } catch (Exception e) {
            log.error("시험의 모든 학생 상세 답안 조회 실패: examId={}, error={}", examId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<List<StudentAnswerDetailDto>>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 학년별 성적 분포도 조회
     */
    @Operation(
        summary = "학년별 성적 분포도 조회",
        description = """
            특정 학년 전체 학생들의 평균 성적을 구간별로 나누어 분포도를 조회합니다.
            
            제공되는 정보:
            - 전체 학생 수와 평균 점수
            - 중앙값과 표준편차  
            - 점수 구간별 분포 (0-39, 40-59, 60-69, 70-79, 80-89, 90-100점)
            - 각 구간별 학생 수와 비율
            - 통계 요약 (최고/최저 점수, 합격률, 우수율)
            
            성적 구간 정의:
            - 0-39점: 낙제
            - 40-59점: 미달  
            - 60-69점: 보통
            - 70-79점: 양호
            - 80-89점: 우수
            - 90-100점: 최우수
            
            합격률: 60점 이상 학생 비율
            우수율: 80점 이상 학생 비율
            
            선생님이 해당 학년 학생들의 전반적인 성적 수준과 분포를 파악하기 위해 사용됩니다.
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ScoreDistributionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 학년 파라미터",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/score-distribution")
    public ResponseEntity<ApiResponse<ScoreDistributionDto>> getScoreDistribution(
        @Parameter(description = "학년 (1, 2, 3)", required = true, example = "1")
        @RequestParam Integer grade
    ) {
        log.info("학년별 성적 분포도 조회 요청: grade={}", grade);
        
        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 파라미터: grade={}", grade);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<ScoreDistributionDto>errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }
        
        try {
            ScoreDistributionDto result = teacherDashboardService.getScoreDistribution(grade);
            
            log.info("학년별 성적 분포도 조회 완료: grade={}, studentCount={}, avgScore={}, distributionCount={}", 
                    grade, result.totalStudentCount(), result.averageScore(), result.distributions().size());
            
            return ResponseEntity.ok(
                ApiResponse.success("학년별 성적 분포도 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("학년별 성적 분포도 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ScoreDistributionDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 시험별 평균 점수 조회 (API 11)
     */
    @Operation(
        summary = "시험별 평균 점수 조회",
        description = """
            지정된 학년의 시험별 평균 점수와 통계 정보를 조회합니다.
            
            제공되는 정보:
            - 최근 시험순으로 정렬된 시험별 평균 점수
            - 각 시험별 제출 수와 제출률
            - 시험별 최고/최저 점수
            - 시험별 표준편차
            - 전체 통계 요약 (총 시험 수, 전체 평균 등)
            
            통계 계산:
            - 평균 점수: 해당 시험의 모든 제출 점수의 평균
            - 제출률: (실제 제출 수 / 전체 학생 수) × 100
            - 표준편차: 점수의 분산도를 나타내는 통계값
            - 최고/최저 점수: 해당 시험의 최고점과 최저점
            
            활용 목적:
            - 시험별 난이도 비교 분석
            - 학생들의 시험 성과 추이 파악
            - 교수 방법 개선을 위한 데이터 근거
            - 시험 출제 난이도 조절 참고자료
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamAverageScoreDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 요청 파라미터",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/exam-average-scores")
    public ResponseEntity<ApiResponse<ExamAverageScoreDto>> getExamAverageScores(
        @Parameter(description = "학년 (1, 2, 3)", required = true, example = "2")
        @RequestParam Integer grade,
        
        @Parameter(description = "최대 조회할 시험 수 (기본값: 10)", example = "10")
        @RequestParam(defaultValue = "10") 
        @Min(value = 1, message = "limit은 1 이상이어야 합니다")
        @Max(value = 50, message = "limit은 50 이하여야 합니다")
        Integer limit
    ) {
        log.info("시험별 평균 점수 조회 요청: grade={}, limit={}", grade, limit);
        
        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 파라미터: grade={}", grade);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<ExamAverageScoreDto>errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }
        
        try {
            ExamAverageScoreDto result = teacherDashboardService.getExamAverageScores(grade, limit);
            
            log.info("시험별 평균 점수 조회 완료: grade={}, limit={}, examCount={}, overallAverage={}", 
                    grade, limit, result.totalExamCount(), result.overallAverageScore());
            
            return ResponseEntity.ok(
                ApiResponse.success("시험별 평균 점수 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("시험별 평균 점수 조회 실패: grade={}, limit={}, error={}", grade, limit, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ExamAverageScoreDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 단원별 오답률 조회 (API 12)
     */
    @Operation(
        summary = "단원별 오답률 조회",
        description = """
            지정된 학년의 단원별 오답률 통계를 조회합니다.
            
            제공되는 정보:
            - 전체 문제 수와 제출 수
            - 전체 오답률
            - 각 단원별 세부 정보:
              * 단원명과 소속 대분류/중분류
              * 해당 단원 문제 수 및 제출 수
              * 오답 수와 오답률
              * 오답률 기준 순위 (높은 순)
            
            통계 계산:
            - 오답률: (오답 수 / 해당 단원 전체 제출 수) × 100
            - 순위: 오답률이 높은 단원부터 1위로 책정
            - 제출이 없는 단원은 제외
            
            교육적 활용:
            - 학습 취약 단원 파악
            - 보충 학습 우선순위 결정
            - 교수 방법 개선 필요 영역 식별
            - 문제 출제 난이도 조정 참고
            
            정렬 기준: 오답률 높은 순 → 단원명 순
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = UnitWrongAnswerRateDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 학년 파라미터",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    @GetMapping("/unit-wrong-answer-rates")
    public ResponseEntity<ApiResponse<UnitWrongAnswerRateDto>> getUnitWrongAnswerRates(
        @Parameter(description = "학년 (1, 2, 3)", required = true, example = "2")
        @RequestParam Integer grade
    ) {
        log.info("단원별 오답률 조회 요청: grade={}", grade);
        
        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            log.warn("잘못된 학년 파라미터: grade={}", grade);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<UnitWrongAnswerRateDto>errorWithType("학년은 1, 2, 3 중 하나여야 합니다"));
        }
        
        try {
            UnitWrongAnswerRateDto result = teacherDashboardService.getUnitWrongAnswerRates(grade);
            
            log.info("단원별 오답률 조회 완료: grade={}, unitCount={}, overallWrongRate={}%", 
                    grade, result.unitStatistics().size(), result.overallWrongAnswerRate());
            
            return ResponseEntity.ok(
                ApiResponse.success("단원별 오답률 조회 성공", result)
            );
        } catch (Exception e) {
            log.error("단원별 오답률 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<UnitWrongAnswerRateDto>errorWithType("조회 중 오류가 발생했습니다"));
        }
    }
}