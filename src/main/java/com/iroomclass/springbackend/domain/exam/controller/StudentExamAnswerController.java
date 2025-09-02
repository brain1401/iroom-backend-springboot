package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.exam.service.StudentAnswerSheetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 답안 관리 컨트롤러
 * 
 * 학생이 시험 답안을 생성, 수정, 조회할 수 있는 API를 제공합니다.
 * AI 이미지 인식과 연동하여 답안을 처리합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/student/exam-answers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 시험 답안 관리", description = "시험 답안 생성, 수정, 조회 API")
public class StudentExamAnswerController {

    private final StudentAnswerSheetService studentAnswerSheetService;

    /**
     * 답안 상태 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 답안 상태 요약 정보
     */
    @GetMapping("/submission/{examSubmissionId}/status")
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

}
