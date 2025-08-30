package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 상세 조회 응답 DTO
 * 
 * 시험지 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 상세 조회 응답")
public record ExamSheetDetailResponse(
    @Schema(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID examSheetId,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "총 문제 개수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "선택된 단원 목록")
    List<UnitInfo> units,
    
    @Schema(description = "선택된 문제 목록")
    List<QuestionInfo> questions
) {
    public ExamSheetDetailResponse {
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(totalQuestions, "totalQuestions는 필수입니다");
        Objects.requireNonNull(units, "units는 필수입니다");
        Objects.requireNonNull(questions, "questions는 필수입니다");
    }
    
    /**
     * 단원 정보
     */
    @Schema(description = "단원 정보")
    public record UnitInfo(
        @Schema(description = "단원 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID unitId,
        
        @Schema(description = "단원명", example = "자연수와 0")
        String unitName
    ) {
        public UnitInfo {
            Objects.requireNonNull(unitId, "unitId는 필수입니다");
            Objects.requireNonNull(unitName, "unitName은 필수입니다");
        }
    }
    
    /**
     * 문제 정보
     */
    @Schema(description = "문제 정보")
    public record QuestionInfo(
        @Schema(description = "문제 번호", example = "1")
        Integer seqNo,
        
        @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID questionId,
        
        @Schema(description = "단원 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID unitId,
        
        @Schema(description = "단원명", example = "자연수와 0")
        String unitName,
        
        @Schema(description = "난이도", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"})
        String difficulty,
        
        @Schema(description = "문제 내용 (HTML)", example = "<p>다음 중 자연수는?</p>")
        String stem,
        
        @Schema(description = "배점", example = "10")
        Integer points
    ) {
        public QuestionInfo {
            Objects.requireNonNull(seqNo, "seqNo는 필수입니다");
            Objects.requireNonNull(questionId, "questionId는 필수입니다");
            Objects.requireNonNull(unitId, "unitId는 필수입니다");
            Objects.requireNonNull(unitName, "unitName은 필수입니다");
            Objects.requireNonNull(difficulty, "difficulty는 필수입니다");
            Objects.requireNonNull(stem, "stem은 필수입니다");
            Objects.requireNonNull(points, "points는 필수입니다");
        }
    }
}