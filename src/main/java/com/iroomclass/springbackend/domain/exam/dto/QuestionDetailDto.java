package com.iroomclass.springbackend.domain.exam.dto;

import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 시험 문제 상세 정보 DTO
 * 
 * <p>학생이 시험을 제출할 때 필요한 모든 문제 정보를 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 문제 상세 정보")
public record QuestionDetailDto(
    @Schema(description = "문제 고유 식별자")
    UUID questionId,
    
    @Schema(description = "문제 순서", example = "1")
    Integer seqNo,
    
    @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE", 
            allowableValues = {"SUBJECTIVE", "MULTIPLE_CHOICE"})
    String questionType,
    
    @Schema(description = "문제 내용 (HTML 형태)", example = "<p>다음 중 옳은 것은?</p>")
    String questionText,
    
    @Schema(description = "문제 배점", example = "10")
    Integer points,
    
    @Schema(description = "문제 난이도", example = "중", 
            allowableValues = {"하", "중", "상"})
    String difficulty,
    
    @Schema(description = "객관식 선택지 (객관식 문제만)", 
            example = "{\"1\": \"선택지1\", \"2\": \"선택지2\", \"3\": \"선택지3\", \"4\": \"선택지4\", \"5\": \"선택지5\"}")
    Map<String, String> choices,
    
    @Schema(description = "이미지 URL 목록")
    List<String> imageUrls,
    
    @Schema(description = "이미지 보유 여부", example = "false")
    boolean hasImage,
    
    @Schema(description = "문제 선택 방식", example = "MANUAL",
            allowableValues = {"MANUAL", "RANDOM"})
    String selectionMethod
) {
    /**
     * ExamSheetQuestion 엔티티로부터 DTO 생성
     * 
     * @param examSheetQuestion 시험지-문제 연결 엔티티 (Question 포함)
     * @return 문제 상세 DTO
     */
    public static QuestionDetailDto from(ExamSheetQuestion examSheetQuestion) {
        Question question = examSheetQuestion.getQuestion();
        List<String> imageUrls = question.getImageUrls();
        
        return new QuestionDetailDto(
                question.getId(),
                examSheetQuestion.getSeqNo(),
                question.getQuestionType().name(),
                question.getQuestionTextAsHtml(),
                examSheetQuestion.getPoints(),
                question.getDifficulty().name(),
                question.isMultipleChoice() ? question.getChoicesAsMap() : Map.of(),
                imageUrls,
                !imageUrls.isEmpty(),
                examSheetQuestion.getSelectionMethod().name()
        );
    }
    
    /**
     * 객관식 문제 여부 확인
     * 
     * @return 객관식 문제이면 true
     */
    public boolean isMultipleChoice() {
        return "MULTIPLE_CHOICE".equals(questionType);
    }
    
    /**
     * 주관식 문제 여부 확인
     * 
     * @return 주관식 문제이면 true
     */
    public boolean isSubjective() {
        return "SUBJECTIVE".equals(questionType);
    }
    
    /**
     * 랜덤으로 선택된 문제 여부 확인
     * 
     * @return 랜덤 선택된 문제이면 true
     */
    public boolean isRandomlySelected() {
        return "RANDOM".equals(selectionMethod);
    }
    
    /**
     * 선택지 보유 여부 확인 (객관식 전용)
     * 
     * @return 선택지가 있으면 true
     */
    public boolean hasChoices() {
        return choices != null && !choices.isEmpty();
    }
}