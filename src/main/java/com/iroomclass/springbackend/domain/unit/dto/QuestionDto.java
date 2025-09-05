package com.iroomclass.springbackend.domain.unit.dto;

import com.iroomclass.springbackend.domain.exam.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * 시험지 등록용 문제 정보 DTO
 * 
 * 단원 트리에서 문제 목록을 표시할 때 사용됩니다.
 * 문제의 핵심 정보만 포함하여 성능을 최적화합니다.
 */
@Schema(description = "시험지 등록용 문제 정보")
public record QuestionDto(
    @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID id,
    
    @Schema(description = "문제 유형", example = "SUBJECTIVE", allowableValues = {"SUBJECTIVE", "MULTIPLE_CHOICE"})
    Question.QuestionType questionType,
    
    @Schema(description = "문제 난이도", example = "중", allowableValues = {"하", "중", "상"})
    Question.Difficulty difficulty,
    
    @Schema(description = "문제 배점", example = "10")
    Integer points,
    
    @Schema(description = "문제 미리보기", example = "다음 방정식을 풀어보시오: x + 5 = 12")
    String questionPreview
) {
    
    /**
     * Question 엔티티로부터 DTO 생성
     *
     * @param question 문제 엔티티
     * @return QuestionDto
     */
    public static QuestionDto from(Question question) {
        if (question == null) {
            return null;
        }
        
        return new QuestionDto(
            question.getId(),
            question.getQuestionType(),
            question.getDifficulty(),
            question.getPoints(),
            generateQuestionPreview(question)
        );
    }
    
    /**
     * 문제 미리보기 생성
     * HTML 변환된 문제 내용을 축약하여 미리보기를 생성합니다.
     *
     * @param question 문제 엔티티
     * @return 문제 미리보기 텍스트
     */
    private static String generateQuestionPreview(Question question) {
        try {
            String htmlContent = question.getQuestionTextAsHtml();
            
            // HTML 태그 제거하고 텍스트만 추출
            String plainText = htmlContent
                .replaceAll("<[^>]+>", "") // HTML 태그 제거
                .replaceAll("\\s+", " ")   // 연속된 공백을 하나로
                .trim();
            
            // 100자로 제한하고 ... 추가
            if (plainText.length() > 100) {
                return plainText.substring(0, 97) + "...";
            }
            
            return plainText;
            
        } catch (Exception e) {
            // 예외 발생 시 기본 텍스트 반환
            return "문제 미리보기를 생성할 수 없습니다.";
        }
    }
    
    /**
     * 문제 유형별 표시명 반환
     *
     * @return 문제 유형 한글명
     */
    public String getQuestionTypeDisplayName() {
        return switch (questionType) {
            case SUBJECTIVE -> "주관식";
            case MULTIPLE_CHOICE -> "객관식";
        };
    }
    
    /**
     * 난이도 표시명 반환
     *
     * @return 난이도 한글명 (이미 한글이므로 그대로 반환)
     */
    public String getDifficultyDisplayName() {
        return difficulty.name();
    }
}