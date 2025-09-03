package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 문제 상세 정보 응답 DTO
 * 
 * <p>특정 문제의 상세 정보를 담는 DTO입니다.
 * 문제 텍스트, 답안, 이미지, 선택지 등 모든 정보를 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 상세 정보")
public record QuestionDetailDto(
    
    @Schema(description = "문제 텍스트")
    String questionText,
    
    @Schema(description = "주관식 문제의 정답 (주관식인 경우만)")
    String correctAnswer,
    
    @Schema(description = "문제 이미지 URL (이미지가 있는 경우만)")
    String imageUrl,
    
    @Schema(description = "객관식 선택지 목록 (객관식인 경우만)")
    List<Choice> choices,
    
    @Schema(description = "학생이 선택한 선택지 (객관식인 경우만)")
    Choice selectedChoice,
    
    @Schema(description = "총 문항 수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "문제 유형", example = "OBJECTIVE")
    String questionType
    
) {
    /**
     * 선택지 정보 내부 클래스
     */
    @Schema(description = "객관식 선택지")
    public record Choice(
        @Schema(description = "선택지 번호", example = "1") Integer choiceNumber,
        @Schema(description = "선택지 내용", example = "x = 5") String choiceText
    ) {}
    
    /**
     * 정적 팩토리 메서드 - 주관식 문제용
     */
    public static QuestionDetailDto forSubjective(String questionText, String correctAnswer, 
                                                 String imageUrl, Integer totalQuestions) {
        return new QuestionDetailDto(questionText, correctAnswer, imageUrl, 
                                   null, null, totalQuestions, "SUBJECTIVE");
    }
    
    /**
     * 정적 팩토리 메서드 - 객관식 문제용
     */
    public static QuestionDetailDto forObjective(String questionText, String imageUrl,
                                                List<Choice> choices, Choice selectedChoice, 
                                                Integer totalQuestions) {
        return new QuestionDetailDto(questionText, null, imageUrl, 
                                   choices, selectedChoice, totalQuestions, "OBJECTIVE");
    }
    
    /**
     * 주관식 문제 여부 확인
     */
    public boolean isSubjective() {
        return "SUBJECTIVE".equals(questionType);
    }
    
    /**
     * 객관식 문제 여부 확인
     */
    public boolean isObjective() {
        return "OBJECTIVE".equals(questionType);
    }
}