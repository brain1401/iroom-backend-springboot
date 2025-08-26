package com.iroomclass.springbackend.domain.user.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제별 결과 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문제별 결과 응답")
public class QuestionResultResponse {

    @Schema(description = "시험 제출 ID", example = "1")
    private Long submissionId;

    @Schema(description = "문제 ID", example = "1")
    private Long questionId;

    @Schema(description = "문제 번호", example = "1")
    private int questionNumber;

    @Schema(description = "문제 내용", example = "다음 수를 계산하시오: 2 + 3 × 4")
    private String questionContent;

    @Schema(description = "정답 여부", example = "true")
    private Boolean isCorrect;

    @Schema(description = "획득 점수", example = "5")
    private Integer score;

    @Schema(description = "문제 배점", example = "5")
    private Integer points;

    @Schema(description = "단원명", example = "정수와 유리수")
    private String unitName;

    @Schema(description = "중분류명", example = "정수")
    private String subcategoryName;

    @Schema(description = "대분류명", example = "수와 연산")
    private String categoryName;

    @Schema(description = "난이도", example = "중")
    private String difficulty;

    @Schema(description = "학생 답안", example = "14")
    private String studentAnswer;

    @Schema(description = "정답", example = "14")
    private String correctAnswer;

    @Schema(description = "답안 이미지 URL", example = "https://example.com/answer1.jpg")
    private String answerImageUrl;
}
