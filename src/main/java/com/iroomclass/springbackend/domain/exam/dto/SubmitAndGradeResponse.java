package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시험 답안 제출 및 채점 응답 DTO
 * 
 * <p>시험 답안 제출 및 채점 결과를 담는 응답 DTO입니다.</p>
 */
@Schema(description = "시험 답안 제출 및 채점 응답")
public record SubmitAndGradeResponse(
    @Schema(description = "제출 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("submission_id")
    UUID submissionId,
    
    @Schema(description = "시험지 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("exam_sheet_id")
    UUID examSheetId,
    
    @Schema(description = "학생 답안지 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("student_answer_sheet_id")
    UUID studentAnswerSheetId,
    
    @Schema(description = "채점 결과")
    @JsonProperty("grading_result")
    GradingResultDto gradingResult,
    
    @Schema(description = "처리 상태", example = "SUCCESS")
    String status,
    
    @Schema(description = "처리 메시지", example = "제출 및 채점이 완료되었습니다")
    String message,
    
    @Schema(description = "제출 시간")
    @JsonProperty("submitted_at")
    LocalDateTime submittedAt
) {
    /**
     * 성공 응답 생성 팩토리 메서드
     */
    public static SubmitAndGradeResponse success(
            UUID submissionId,
            UUID examSheetId,
            UUID studentAnswerSheetId,
            GradingResultDto gradingResult,
            LocalDateTime submittedAt) {
        return new SubmitAndGradeResponse(
            submissionId,
            examSheetId,
            studentAnswerSheetId,
            gradingResult,
            "SUCCESS",
            "제출 및 채점이 완료되었습니다",
            submittedAt
        );
    }
    
    /**
     * 제출만 성공한 경우 (채점 대기)
     */
    public static SubmitAndGradeResponse submittedOnly(
            UUID submissionId,
            UUID examSheetId,
            UUID studentAnswerSheetId,
            LocalDateTime submittedAt) {
        return new SubmitAndGradeResponse(
            submissionId,
            examSheetId,
            studentAnswerSheetId,
            null,
            "SUBMITTED",
            "제출이 완료되었습니다. 채점은 대기 중입니다",
            submittedAt
        );
    }
}