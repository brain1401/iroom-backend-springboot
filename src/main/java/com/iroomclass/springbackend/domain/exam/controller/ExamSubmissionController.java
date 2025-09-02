package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ApiResponseConstants;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionListResponse;
import com.iroomclass.springbackend.domain.exam.service.ExamSubmissionService;
import com.iroomclass.springbackend.domain.exam.service.AdminExamSubmissionService;
import com.iroomclass.springbackend.domain.exam.service.StudentAnswerSheetService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 제출 통합 관리 컨트롤러
 * 
 * 학생과 관리자 모두가 시험 제출을 관리할 수 있는 통합 API를 제공합니다.
 * 역할 기반 접근 제어를 통해 적절한 권한 분리를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exam/submissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "시험 제출 통합 관리", description = "학생 및 관리자용 시험 제출 통합 API - 역할 기반 접근 제어")
public class ExamSubmissionController {

    private final ExamSubmissionService examSubmissionService;
    private final AdminExamSubmissionService adminExamSubmissionService;
    private final StudentAnswerSheetService studentAnswerSheetService;

    /**
     * 시험 제출 생성
     * 
     * @param request 시험 제출 생성 요청
     * @return 생성된 시험 제출 정보
     */
    @PostMapping
    @Operation(summary = "시험 제출 생성", description = "학생이 시험을 제출할 때 사용됩니다. 중복 제출은 방지됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = {
                    @ExampleObject(name = "입력 검증 실패", summary = "입력 데이터 검증 실패", value = """
                            {
                              "result": "ERROR",
                              "message": "입력 데이터 검증에 실패했습니다",
                              "data": null
                            }
                            """),
                    @ExampleObject(name = "중복 제출", summary = "이미 제출한 시험", value = """
                            {
                              "result": "ERROR",
                              "message": "이미 제출된 시험입니다",
                              "data": null
                            }
                            """)
            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 없음", summary = "시험을 찾을 수 없음", value = """
                    {
                      "result": "ERROR",
                      "message": "시험을 찾을 수 없습니다",
                      "data": null
                    }
                    """)))
    })
    public ApiResponse<ExamSubmissionCreateResponse> createExamSubmission(
            @Valid @RequestBody ExamSubmissionCreateRequest request) {
        log.info("시험 제출 생성 요청: 시험={}, 학생={}, 전화번호={}",
                request.examId(), request.studentName(), request.studentPhone());

        ExamSubmissionCreateResponse response = examSubmissionService.createExamSubmission(request);

        log.info("시험 제출 생성 성공: ID={}, 학생={}, 시험={}",
                response.submissionId(), response.studentName(), response.examName());

        return ApiResponse.success(ApiResponseConstants.SUBMISSION_CREATE_SUCCESS, response);
    }

    /**
     * 시험 최종 제출
     * 
     * @param submissionId 시험 제출 ID
     * @return 최종 제출 완료 정보
     */
    @PostMapping("/{submissionId}/final-submit")
    @Operation(summary = "시험 최종 제출", description = "모든 답안이 완료된 후 시험을 최종 제출합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "답안 미완료", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "답안 미완료", value = ApiResponseConstants.SUBMISSION_NOT_COMPLETED_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 제출 없음", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 제출 없음", value = ApiResponseConstants.SUBMISSION_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamSubmissionCreateResponse> finalSubmitExam(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID submissionId) {
        log.info("시험 최종 제출 요청: 제출 ID={}", submissionId);

        ExamSubmissionCreateResponse response = examSubmissionService.finalSubmitExam(submissionId);

        log.info("시험 최종 제출 성공: 제출 ID={}, 학생={}",
                response.submissionId(), response.studentName());

        return ApiResponse.success(ApiResponseConstants.SUBMISSION_FINAL_SUCCESS, response);
    }

    /**
     * 답안 상태 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 답안 상태 요약 정보
     */
    @GetMapping("/{examSubmissionId}/answer-status")
    @Operation(summary = "답안 상태 확인", description = "답안의 현재 상태를 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 제출 ID")
    })
    public ApiResponse<StudentAnswerSheetService.AnswerStatusSummary> getAnswerStatusSummary(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSubmissionId) {
        log.info("답안 상태 확인 요청: 시험 제출 ID={}", examSubmissionId);

        StudentAnswerSheetService.AnswerStatusSummary response = studentAnswerSheetService
                .getAnswerStatusSummary(examSubmissionId);

        log.info("답안 상태 확인 완료: 총 {}개, 정답 {}개",
                response.getTotalCount(), response.getCorrectCount());

        return ApiResponse.success("시험 답안 조회 성공", response);
    }

    // ============================================
    // 관리자 전용 엔드포인트
    // ============================================

    /**
     * 시험 제출 목록 조회 (관리자 전용)
     * 
     * @param examId       시험 ID (선택사항)
     * @param studentName  학생 이름 (선택사항)
     * @param studentPhone 학생 전화번호 (선택사항)
     * @return 필터링된 시험 제출 목록
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험 제출 목록 조회 (관리자)", description = "관리자가 특정 시험의 모든 학생 제출 현황을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    public ApiResponse<ExamSubmissionListResponse> getSubmissionsByExam(
            @Parameter(description = "시험 ID (필수)", example = "123e4567-e89b-12d3-a456-426614174000") @RequestParam UUID examId) {
        log.info("관리자 - 시험별 제출 목록 조회 요청: 시험 ID={}", examId);

        ExamSubmissionListResponse response = adminExamSubmissionService.getExamSubmissions(examId);

        log.info("관리자 - 시험별 제출 목록 조회 성공: 시험={}, 제출={}개", response.examName(), response.totalCount());

        return ApiResponse.success("시험 제출 목록 조회 성공", response);
    }

    /**
     * 시험 제출 상세 조회 (관리자 전용)
     * 
     * @param submissionId 시험 제출 ID
     * @return 시험 제출 상세 정보
     */
    @GetMapping("/{submissionId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험 제출 상세 조회 (관리자)", description = "관리자가 특정 시험 제출의 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 제출을 찾을 수 없음")
    })
    public ApiResponse<ExamSubmissionDetailResponse> getSubmissionDetail(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID submissionId) {
        log.info("관리자 - 시험 제출 상세 조회 요청: 제출 ID={}", submissionId);

        ExamSubmissionDetailResponse response = adminExamSubmissionService.getExamSubmissionDetail(submissionId);

        log.info("관리자 - 시험 제출 상세 조회 성공: 학생={}, 시험={}", response.studentName(), response.examName());

        return ApiResponse.success("시험 제출 상세 조회 성공", response);
    }

    /**
     * 학생별 제출 목록 조회 (관리자 전용)
     * 
     * @param studentName  학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "학생별 제출 목록 조회 (관리자)", description = "관리자가 특정 학생의 모든 시험 제출 이력을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패")
    })
    public ApiResponse<List<ExamSubmissionDetailResponse>> getStudentSubmissions(
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String studentPhone) {
        log.info("관리자 - 학생별 제출 목록 조회 요청: 학생={}, 전화번호={}", studentName, studentPhone);

        List<ExamSubmissionDetailResponse> response = adminExamSubmissionService.getStudentSubmissions(studentName,
                studentPhone);

        log.info("관리자 - 학생별 제출 목록 조회 성공: 학생={}, 제출={}개", studentName, response.size());

        return ApiResponse.success("학생 제출 목록 조회 성공", response);
    }

    /**
     * 시험별 제출 학생 수 조회 (관리자 전용)
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 학생 수
     */
    @GetMapping("/exam/{examId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험별 제출 학생 수 조회 (관리자)", description = "관리자가 특정 시험에 제출한 학생 수를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    public ApiResponse<Long> getExamSubmissionCount(
            @Parameter(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examId) {
        log.info("관리자 - 시험별 제출 학생 수 조회 요청: 시험 ID={}", examId);

        long count = adminExamSubmissionService.getExamSubmissionCount(examId);

        log.info("관리자 - 시험별 제출 학생 수 조회 성공: 시험 ID={}, 제출 학생 수={}", examId, count);

        return ApiResponse.success("시험 제출 수 조회 성공", count);
    }
}
