package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 목록 조회 응답 DTO
 * 
 * 학년별 시험지 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 목록 조회 응답")
public record ExamSheetListResponse(
    @Schema(description = "학년 (전체 목록일 경우 null)", example = "1", nullable = true)
    Integer grade,
    
    @Schema(description = "시험지 목록")
    List<ExamSheetInfo> examSheets,
    
    @Schema(description = "총 개수", example = "10")
    int totalCount
) {
    public ExamSheetListResponse {
        Objects.requireNonNull(examSheets, "examSheets는 필수입니다");
    }
    
    /**
     * 시험지 정보
     */
    @Schema(description = "시험지 정보")
    public record ExamSheetInfo(
        @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID examSheetId,
        
        @Schema(description = "시험지 이름", example = "1학년 중간고사")
        String examName,
        
        @Schema(description = "학년", example = "1")
        Integer grade,
        
        @Schema(description = "총 문제 개수", example = "20")
        Integer totalQuestions,
        
        @Schema(description = "선택된 단원 수", example = "3")
        Integer selectedUnitCount
    ) {
        public ExamSheetInfo {
            Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
            Objects.requireNonNull(examName, "examName은 필수입니다");
            Objects.requireNonNull(grade, "grade는 필수입니다");
            Objects.requireNonNull(totalQuestions, "totalQuestions는 필수입니다");
            Objects.requireNonNull(selectedUnitCount, "selectedUnitCount는 필수입니다");
        }
    }
}