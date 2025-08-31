package com.iroomclass.springbackend.domain.exam.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.UUID;

/**
 * 문제별 결과 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제별 결과 응답")
public record QuestionResultResponse(
        @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID submissionId,

        @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174001") UUID questionId,

        @Schema(description = "문제 번호", example = "1") int questionNumber,

        @Schema(description = "문제 내용", example = "다음 수를 계산하시오: 2 + 3 × 4") String questionContent,

        @Schema(description = "정답 여부", example = "true") Boolean isCorrect,

        @Schema(description = "획득 점수", example = "5") Integer score,

        @Schema(description = "문제 배점", example = "5") Integer points,

        @Schema(description = "단원명", example = "정수와 유리수") String unitName,

        @Schema(description = "중분류명", example = "정수") String subcategoryName,

        @Schema(description = "대분류명", example = "수와 연산") String categoryName,

        @Schema(description = "난이도", example = "중") String difficulty,

        @Schema(description = "학생 답안", example = "14") String studentAnswer,

        @Schema(description = "정답", example = "14") String correctAnswer,

        @Schema(description = "답안 이미지 URL", example = "https://example.com/answer1.jpg") String answerImageUrl) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public QuestionResultResponse {
        Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        Objects.requireNonNull(questionContent, "questionContent은 필수입니다");
    }
}