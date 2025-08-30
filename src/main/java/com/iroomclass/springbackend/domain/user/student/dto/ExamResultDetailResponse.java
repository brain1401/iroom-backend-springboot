package com.iroomclass.springbackend.domain.user.student.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 상세 결과 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 상세 결과 응답")
public record ExamResultDetailResponse(
    @Schema(description = "시험 제출 ID", example = "1")
    Long submissionId,

    @Schema(description = "시험 ID", example = "1")
    Long examId,

    @Schema(description = "시험명", example = "1학년 중간고사")
    String examName,

    @Schema(description = "학년", example = "1학년")
    String grade,

    @Schema(description = "학생 이름", example = "김철수")
    String studentName,

    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String studentPhone,

    @Schema(description = "제출일시")
    LocalDateTime submittedAt,

    @Schema(description = "총점", example = "85")
    Integer totalScore,

    @Schema(description = "총 문제 수", example = "20")
    int totalQuestions,

    @Schema(description = "객관식 문제 수", example = "15")
    int multipleChoiceCount,

    @Schema(description = "주관식 문제 수", example = "5")
    int subjectiveCount,

    @Schema(description = "단원명 목록", example = "정수와 유리수, 문자와 식, 일차방정식")
    String unitNames,

    @Schema(description = "정답 수", example = "17")
    int correctCount,

    @Schema(description = "오답 수", example = "3")
    int incorrectCount,

    @Schema(description = "문제별 결과 목록")
    List<QuestionResult> questionResults
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public ExamResultDetailResponse {
        Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
        Objects.requireNonNull(questionResults, "questionResults은 필수입니다");
    }

    /**
     * 문제별 결과
     */
    @Schema(description = "문제별 결과")
    public record QuestionResult(
        @Schema(description = "문제 ID", example = "1")
        Long questionId,

        @Schema(description = "문제 번호", example = "1")
        int questionNumber,

        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,

        @Schema(description = "획득 점수", example = "5")
        Integer score,

        @Schema(description = "문제 배점", example = "5")
        Integer points,

        @Schema(description = "단원명", example = "정수와 유리수")
        String unitName,

        @Schema(description = "난이도", example = "중")
        String difficulty,

        @Schema(description = "학생 답안", example = "42")
        String studentAnswer,

        @Schema(description = "정답", example = "42")
        String correctAnswer
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public QuestionResult {
            Objects.requireNonNull(questionId, "questionId은 필수입니다");
        }
    }
}