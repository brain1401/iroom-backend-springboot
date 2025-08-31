package com.iroomclass.springbackend.domain.exam.dto;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제 상세 조회 응답 DTO
 * 
 * 특정 문제의 상세 정보 조회 시 사용됩니다.
 * 주관식과 객관식 문제를 모두 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 상세 조회 응답")
public record QuestionDetailResponse(
    @Schema(description = "문제 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID questionId,
    
    @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID unitId,
    
    @Schema(description = "단원명", example = "자연수와 0")
    String unitName,
    
    @Schema(description = "난이도", example = "하", allowableValues = {"하", "중", "상"})
    String difficulty,
    
    @Schema(description = "문제 유형", example = "SUBJECTIVE", allowableValues = {"SUBJECTIVE", "MULTIPLE_CHOICE"})
    String questionType,
    
    @Schema(description = "문제 내용 (HTML)", example = "<p>다음 중 자연수는?</p>")
    String stem,
    
    @Schema(description = "주관식 정답 (주관식인 경우)", example = "3")
    String answerText,
    
    @Schema(description = "채점 기준 (채점 루브릭)", example = "정확한 계산 과정과 최종 답안 모두 필요")
    String scoringRubric,
    
    @Schema(description = "객관식 선택지 (객관식인 경우)", example = "{\"1\": \"0\", \"2\": \"-1\", \"3\": \"3\", \"4\": \"1.5\", \"5\": \"모든 것\"}")
    Map<String, String> choices,
    
    @Schema(description = "객관식 정답 번호 (객관식인 경우)", example = "3")
    Integer correctChoice
) {
    public QuestionDetailResponse {
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        Objects.requireNonNull(unitId, "unitId은 필수입니다");
        Objects.requireNonNull(unitName, "unitName는 필수입니다");
        Objects.requireNonNull(difficulty, "difficulty은 필수입니다");
        Objects.requireNonNull(questionType, "questionType은 필수입니다");
        Objects.requireNonNull(stem, "stem은 필수입니다");
        
        // 문제 유형별 필수 필드 검증
        if ("SUBJECTIVE".equals(questionType)) {
            Objects.requireNonNull(answerText, "주관식 문제는 answerText가 필수입니다");
        } else if ("MULTIPLE_CHOICE".equals(questionType)) {
            Objects.requireNonNull(choices, "객관식 문제는 choices가 필수입니다");
            Objects.requireNonNull(correctChoice, "객관식 문제는 correctChoice가 필수입니다");
            
            if (correctChoice < 1 || correctChoice > 5) {
                throw new IllegalArgumentException("correctChoice는 1~5 사이여야 합니다: " + correctChoice);
            }
        }
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
     * 객관식 문제 여부 확인
     * 
     * @return 객관식 문제이면 true
     */
    public boolean isMultipleChoice() {
        return "MULTIPLE_CHOICE".equals(questionType);
    }
}