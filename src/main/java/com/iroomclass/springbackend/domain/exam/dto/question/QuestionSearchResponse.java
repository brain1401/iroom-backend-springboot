package com.iroomclass.springbackend.domain.exam.dto.question;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제 검색 응답 DTO
 * 
 * 키워드로 문제 검색 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 검색 응답")
public record QuestionSearchResponse(
        @Schema(description = "검색 키워드", example = "자연수") String keyword,

        @Schema(description = "검색된 문제 목록") List<QuestionInfo> questions,

        @Schema(description = "전체 검색 결과 수", example = "15") int totalResults) {
    public QuestionSearchResponse {
        Objects.requireNonNull(keyword, "keyword은 필수입니다");
        Objects.requireNonNull(questions, "questions은 필수입니다");
    }

    /**
     * 검색된 문제 정보
     */
    @Schema(description = "검색된 문제 정보")
    public record QuestionInfo(
            @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID questionId,

            @Schema(description = "단원 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID unitId,

            @Schema(description = "단원명", example = "자연수와 0") String unitName,

            @Schema(description = "문제 유형", example = "SUBJECTIVE", allowableValues = {
                    "SUBJECTIVE", "MULTIPLE_CHOICE" }) String questionType,

            @Schema(description = "난이도", example = "EASY", allowableValues = { "EASY", "MEDIUM",
                    "HARD" }) String difficulty,

            @Schema(description = "문제 내용 (HTML)", example = "<p>다음 중 자연수는?</p>") String stem){
        public QuestionInfo {
            Objects.requireNonNull(questionId, "questionId은 필수입니다");
            Objects.requireNonNull(unitId, "unitId은 필수입니다");
            Objects.requireNonNull(unitName, "unitName는 필수입니다");
            Objects.requireNonNull(questionType, "questionType은 필수입니다");
            Objects.requireNonNull(difficulty, "difficulty은 필수입니다");
            Objects.requireNonNull(stem, "stem은 필수입니다");
        }
    }
}