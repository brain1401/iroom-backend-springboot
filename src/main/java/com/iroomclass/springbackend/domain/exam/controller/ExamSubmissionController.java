package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.exam.service.ExamSubmissionService;

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
 * 시험 제출 관리 컨트롤러 (학생용)
 * 
 * 학생이 시험을 제출할 수 있는 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/user/exam-submission")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 시험 제출", description = "학생 시험 제출 생성, 최종 제출 API")
public class ExamSubmissionController {

    private final ExamSubmissionService examSubmissionService;

    /**
     * 시험 제출 생성
     * 
     * @param request 시험 제출 생성 요청
     * @return 생성된 시험 제출 정보
     */
    @PostMapping
    @Operation(summary = "시험 제출 생성", description = "학생이 시험을 제출할 때 사용됩니다. 중복 제출은 방지됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "입력 검증 실패",
                            summary = "입력 데이터 검증 실패",
                            value = """
                            {
                              "result": "ERROR",
                              "message": "입력 데이터 검증에 실패했습니다",
                              "data": null
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "중복 제출",
                            summary = "이미 제출한 시험",
                            value = """
                            {
                              "result": "ERROR",
                              "message": "이미 제출된 시험입니다",
                              "data": null
                            }
                            """
                        )
                    }
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                    description = "오류",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "시험 없음",
                        summary = "시험을 찾을 수 없음",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "시험을 찾을 수 없습니다",
                          "data": null
                        }
                        """
                    )
                )
            )
    })
    public ApiResponse<ExamSubmissionCreateResponse> createExamSubmission(
            @Valid @RequestBody ExamSubmissionCreateRequest request) {
        log.info("시험 제출 생성 요청: 시험={}, 학생={}, 전화번호={}",
                request.examId(), request.studentName(), request.studentPhone());

        ExamSubmissionCreateResponse response = examSubmissionService.createExamSubmission(request);

        log.info("시험 제출 생성 성공: ID={}, 학생={}, 시험={}",
                response.submissionId(), response.studentName(), response.examName());

        return ApiResponse.success("시험 제출 생성 성공", response);
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = @ExampleObject(
                        name = "답안 미완료",
                        summary = "답안이 완료되지 않음",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "답안이 완료되지 않았습니다",
                          "data": null
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                    description = "오류",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "시험 제출 없음",
                        summary = "시험 제출을 찾을 수 없음",
                        value = """
                        {
                          "result": "ERROR",
                          "message": "시험 제출을 찾을 수 없습니다",
                          "data": null
                        }
                        """
                    )
                )
            )
    })
    public ApiResponse<ExamSubmissionCreateResponse> finalSubmitExam(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID submissionId) {
        log.info("시험 최종 제출 요청: 제출 ID={}", submissionId);

        ExamSubmissionCreateResponse response = examSubmissionService.finalSubmitExam(submissionId);

        log.info("시험 최종 제출 성공: 제출 ID={}, 학생={}",
                response.submissionId(), response.studentName());

        return ApiResponse.success("시험 제출 생성 성공", response);
    }
}
