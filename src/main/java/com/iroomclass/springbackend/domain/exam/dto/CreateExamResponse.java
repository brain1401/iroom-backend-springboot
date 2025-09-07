package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시험 생성 응답 DTO
 * 
 * <p>
 * 시험 생성 후 반환되는 시험 정보를 포함합니다.
 * 시험 기본 정보와 함께 문제지 정보, 상태 등을 제공합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 생성 응답 DTO")
public record CreateExamResponse(
        @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID examId,

        @Schema(description = "시험명", example = "2024년 2학기 중간고사") String examName,

        @Schema(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174001") UUID examSheetId,

        @Schema(description = "시험지명", example = "중2 수학 1단원 문제지") String examSheetName,

        @Schema(description = "학년", example = "2") Integer grade,

        @Schema(description = "최대 학생 수", example = "30")
    Integer maxStudent,

        @Schema(description = "총 문제 수", example = "20") Integer totalQuestions,

        @Schema(description = "총점", example = "100") Integer totalPoints,

        @Schema(description = "시험 상태", example = "CREATED", allowableValues = {
                "CREATED", "IN_PROGRESS", "COMPLETED", "CANCELLED" }) ExamStatus status,

        @Schema(description = "시험 설명", example = "2학기 중간고사입니다") String description,

        @Schema(description = "시험 시작일시", example = "2024-12-10T09:00:00") LocalDateTime startDate,

        @Schema(description = "시험 종료일시", example = "2024-12-10T11:00:00") LocalDateTime endDate,

        @Schema(description = "시험 제한시간 (분 단위)", example = "120") Integer duration,

        @Schema(description = "생성일시", example = "2024-12-09T10:30:00") LocalDateTime createdAt){
    /**
     * 시험 상태 열거형
     */
    public enum ExamStatus {
        CREATED("생성됨"),
        IN_PROGRESS("진행 중"),
        COMPLETED("완료됨"),
        CANCELLED("취소됨");

        private final String description;

        ExamStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Exam 엔티티와 ExamSheet 정보를 기반으로 응답 DTO 생성
     * 
     * @param exam            생성된 시험 엔티티
     * @param examSheet       시험지 정보
     * @param teacherUsername 생성한 선생님의 username (더 이상 사용하지 않음, null 가능)
     * @param startDate       시험 시작일시 (선택)
     * @param endDate         시험 종료일시 (선택)
     * @param duration        시험 제한시간 (선택)
     * @return 시험 생성 응답 DTO
     */
    public static CreateExamResponse from(
            com.iroomclass.springbackend.domain.exam.entity.Exam exam,
            com.iroomclass.springbackend.domain.exam.entity.ExamSheet examSheet,
            String teacherUsername, // 호환성을 위해 유지하지만 사용하지 않음
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer duration) {

        // 총 문제 수 계산
        int totalQuestions = examSheet.getQuestions() != null ? examSheet.getQuestions().size() : 0;

        // 총점 계산 (ExamSheetQuestion의 points 합계)
        int totalPoints = examSheet.getQuestions() != null ? examSheet.getQuestions().stream()
                .mapToInt(q -> q.getPoints() != null ? q.getPoints() : 0)
                .sum() : 0;

        return new CreateExamResponse(
                exam.getId(),
                exam.getExamName(),
                examSheet.getId(),
                examSheet.getExamName(),
                exam.getGrade(),
                exam.getMaxStudent(),
                totalQuestions,
                totalPoints,
                ExamStatus.CREATED, // 새로 생성된 시험은 항상 CREATED 상태
                exam.getContent(),
                startDate,
                endDate,
                duration,
                exam.getCreatedAt());
    }
}