package com.iroomclass.springbackend.domain.student.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.auth.dto.StudentUpsertRequest;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.dto.response.*;
import com.iroomclass.springbackend.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 학생 API 컨트롤러
 * 
 * <p>학생 관리 및 시험 관련 정보 조회 API를 제공합니다.
 * 모든 엔드포인트는 3요소 정보(이름, 생년월일, 전화번호)를 사용하여 학생을 조회하거나 생성합니다.</p>
 */
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(
    name = "학생 API", 
    description = """
        학생 관리 및 시험 정보 조회 API
        
        주요 기능:
        - 3요소 정보 기반 학생 등록/로그인 (이름, 생년월일, 전화번호)
        - 최근 시험 제출 내역 조회
        - 시험 결과 요약 및 상세 조회
        - 학생 정보 조회
        - 로그아웃
        
        학생 관리 방식:
        - JWT 토큰 없이 매 요청마다 3요소 정보 확인
        - 존재하지 않는 학생은 자동으로 생성 (Upsert 패턴)
        - 모든 엔드포인트에서 StudentUpsertRequest 사용
        """
)
@Slf4j
public class StudentController {

    private final StudentService studentService;

    /**
     * 학생 등록/로그인
     * 
     * @param request 학생 정보 요청 (이름, 생년월일, 전화번호)
     * @return 로그인 응답 (학생 ID, 이름)
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "학생 등록/로그인",
        description = """
            학생의 3요소 정보를 통해 학생을 조회하거나 생성합니다.
            
            동작 방식:
            - 이름, 생년월일, 전화번호 조합으로 학생 검색
            - JWT 토큰을 사용하지 않음
            - 존재하는 학생: 기존 정보 반환
            - 존재하지 않는 학생: 자동으로 생성 후 정보 반환
            
            응답 정보:
            - 학생 ID (Long)
            - 학생 이름 (String)
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "등록/로그인 성공",
                content = @Content(schema = @Schema(implementation = StudentLoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "시스템 오류 (정상적으로는 발생하지 않음)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "요청 데이터 검증 실패",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
        }
    )
    public ApiResponse<StudentLoginResponse> login(
            @Valid @RequestBody StudentUpsertRequest request) {
        
        log.info("학생 등록/로그인 요청: name={}", request.name());
        
        StudentLoginResponse response = studentService.loginWithUpsert(request);
        
        return ApiResponse.success("학생 등록/로그인 성공", response);
    }

    /**
     * 최근 시험 제출 내역 조회
     * 
     * @param request 학생 정보 요청
     * @param pageable 페이징 정보 (기본값: page=0, size=10)
     * @return 최근 제출 내역 페이지
     */
    @PostMapping("/recent-submissions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "최근 시험 제출 내역 조회",
        description = """
            학생의 최근 시험 제출 내역을 페이징으로 조회합니다.
            
            조회 정보:
            - 시험명
            - 제출 시간
            - 시험 설명
            - 문제 수
            
            정렬 기준:
            - 제출 시간 내림차순 (최신순)
            
            페이징:
            - 기본 page=0, size=10
            - 최대 size=100 제한
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = RecentSubmissionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<Page<RecentSubmissionDto>> getRecentSubmissions(
            @Valid @RequestBody StudentUpsertRequest request,
            @PageableDefault(page = 0, size = 10) 
            @Parameter(description = "페이징 정보 (page=0, size=10)", hidden = true) 
            Pageable pageable) {
        
        log.info("최근 시험 제출 내역 조회 요청: name={}, page={}", 
                request.name(), pageable.getPageNumber());
        
        Page<RecentSubmissionDto> submissions = studentService.getRecentSubmissionsWithUpsert(request, pageable);
        
        return ApiResponse.success("최근 시험 제출 내역 조회 성공", submissions);
    }

    /**
     * 시험 결과 요약 목록 조회
     * 
     * @param request 학생 정보 요청
     * @param pageable 페이징 정보 (기본값: page=0, size=10)
     * @return 시험 결과 요약 페이지
     */
    @PostMapping("/exam-results")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "시험 결과 요약 목록 조회",
        description = """
            학생의 시험 결과 요약 목록을 페이징으로 조회합니다.
            
            조회 정보:
            - 시험명
            - 총점
            - 채점 상태
            - 채점 일시
            - 학년
            
            정렬 기준:
            - 채점 일시 내림차순 (최신순)
            
            페이징:
            - 기본 page=0, size=10
            - 최대 size=100 제한
            
            참고사항:
            - 재채점된 경우 최신 버전의 결과만 반환
            - 채점 완료된 시험만 조회
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamResultSummaryDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<Page<ExamResultSummaryDto>> getExamResultsSummary(
            @Valid @RequestBody StudentUpsertRequest request,
            @PageableDefault(page = 0, size = 10) 
            @Parameter(description = "페이징 정보 (page=0, size=10)", hidden = true) 
            Pageable pageable) {
        
        log.info("시험 결과 요약 조회 요청: name={}, page={}", 
                request.name(), pageable.getPageNumber());
        
        Page<ExamResultSummaryDto> results = studentService.getExamResultsSummaryWithUpsert(request, pageable);
        
        return ApiResponse.success("시험 결과 요약 조회 성공", results);
    }

    /**
     * 특정 시험의 상세 결과 조회
     * 
     * @param request 학생 정보 요청
     * @param examId 시험 ID
     * @return 시험 상세 결과
     */
    @PostMapping("/exam-detail/{examId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "시험 상세 결과 조회",
        description = """
            특정 시험의 상세 결과를 조회합니다.
            
            조회 정보:
            - 시험 기본 정보 (시험명, 총점, 채점 상태)
            - 문제별 상세 결과
              - 문제 내용
              - 학생 답안
              - 채점 점수
              - 정답 여부
              - 피드백
            - 문제 유형별 통계
              - 주관식/객관식 문제 수
              - 유형별 정답률
            
            참고사항:
            - 재채점된 경우 최신 버전의 결과 반환
            - 채점 완료된 시험만 조회 가능
            - 해당 학생이 응시하지 않은 시험은 404 오류
            """,
        parameters = {
            @Parameter(
                name = "examId", 
                description = "시험 고유 식별자", 
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000"
            )
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExamDetailResultDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보 또는 시험 결과를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "잘못된 시험 ID 형식"
            )
        }
    )
    public ApiResponse<ExamDetailResultDto> getExamDetailResult(
            @Valid @RequestBody StudentUpsertRequest request,
            @PathVariable("examId") 
            @Parameter(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            UUID examId) {
        
        log.info("시험 상세 결과 조회 요청: name={}, examId={}", request.name(), examId);
        
        ExamDetailResultDto result = studentService.getExamDetailResultWithUpsert(request, examId);
        
        return ApiResponse.success("시험 상세 결과 조회 성공", result);
    }

    /**
     * 학생 정보 조회
     * 
     * @param request 학생 정보 요청
     * @return 학생 정보
     */
    @PostMapping("/info")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "학생 정보 조회",
        description = """
            학생의 기본 정보를 조회합니다.
            
            조회 정보:
            - 학생 ID
            - 이름
            - 전화번호
            - 생년월일
            - 최근 응시 학년 정보
            - 계정 생성/수정 일시
            
            학년 정보:
            - 가장 최근에 응시한 시험의 학년 정보
            - 응시 기록이 없으면 null 반환
            
            참고사항:
            - Student 엔터티에는 학년 필드가 없음
            - Exam.grade 필드를 통해 동적으로 판단
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StudentInfoDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보를 찾을 수 없음"
            )
        }
    )
    public ApiResponse<StudentInfoDto> getStudentInfo(
            @Valid @RequestBody StudentUpsertRequest request) {
        
        log.info("학생 정보 조회 요청: name={}", request.name());
        
        StudentInfoDto studentInfo = studentService.getStudentInfoWithUpsert(request);
        
        return ApiResponse.success("학생 정보 조회 성공", studentInfo);
    }

    /**
     * 학생 시험 이력 조회
     * 
     * @param request 학생 정보 요청 (이름, 생년월일, 전화번호)
     * @param pageable 페이징 정보 (기본값: page=0, size=10)
     * @return 학생의 시험 이력 페이지
     */
    @PostMapping("/exam-history")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "학생 시험 이력 조회",
        description = """
            학생이 응시한 모든 시험의 정보를 페이징으로 조회합니다.
            
            조회 정보:
            - 시험 ID
            - 시험명
            - 응시일시
            - 총 문제 수
            - 총점
            
            정렬 기준:
            - 응시일시 내림차순 (최신순)
            
            페이징:
            - 기본 page=0, size=10
            - 최대 size=100 제한
            
            참고사항:
            - 재채점된 경우 최신 버전의 결과만 반환
            - 채점 완료된 시험의 점수 포함
            - 채점되지 않은 시험은 점수가 0으로 표시
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StudentExamHistoryDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "요청 데이터 검증 실패"
            )
        }
    )
    public ApiResponse<Page<StudentExamHistoryDto>> getExamHistory(
            @Valid @RequestBody StudentUpsertRequest request,
            @PageableDefault(page = 0, size = 10) 
            @Parameter(description = "페이징 정보 (page=0, size=10)", hidden = true) 
            Pageable pageable) {
        
        log.info("학생 시험 이력 조회 요청: name={}, page={}", 
                request.name(), pageable.getPageNumber());
        
        Page<StudentExamHistoryDto> examHistory = studentService.getExamHistoryWithUpsert(request, pageable);
        
        return ApiResponse.success("학생 시험 이력 조회 성공", examHistory);
    }

    /**
     * 학생 로그아웃
     * 
     * @param request 학생 정보 요청
     * @return 로그아웃 완료 응답
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "학생 로그아웃",
        description = """
            학생 로그아웃을 수행합니다.
            
            현재 구현:
            - 상태리스 설계로 별도의 세션 관리 없음
            - 인증 정보 확인 후 성공 응답 반환
            - JWT 토큰 무효화 등의 처리 없음
            
            향후 확장 가능:
            - 세션 기반 인증 시 세션 무효화
            - 토큰 기반 인증 시 토큰 블랙리스트 처리
            - 로그아웃 로그 기록
            
            참고사항:
            - 학생 정보 검증 실패 시 해당 학생 자동 생성
            - 성공 시 data는 null로 반환
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "학생 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = StudentUpsertRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "학생 정보를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "요청 데이터 검증 실패"
            )
        }
    )
    public ApiResponse<Void> logout(
            @Valid @RequestBody StudentUpsertRequest request) {
        
        log.info("학생 로그아웃 요청: name={}", request.name());
        
        studentService.logoutWithUpsert(request);
        
        return ApiResponse.success("학생 로그아웃 성공");
    }
}