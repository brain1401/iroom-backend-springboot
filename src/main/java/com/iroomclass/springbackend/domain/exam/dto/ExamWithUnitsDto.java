package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 단원 정보가 포함된 시험 응답 DTO
 * 
 * <p>기존 ExamDto에 해당 시험의 모든 문제에 사용된 단원 정보를 추가로 제공합니다.
 * N+1 쿼리 문제를 방지하기 위해 @EntityGraph 최적화가 적용된 쿼리로 조회됩니다.</p>
 */
@Schema(description = "단원 정보가 포함된 시험 응답 DTO")
public record ExamWithUnitsDto(
    @Schema(description = "시험 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "시험 설명", example = "1학년 수학 중간고사")
    String content,
    
    @Schema(description = "QR 코드 URL", example = "https://example.com/qr/exam123")
    String qrCodeUrl,
    
    @Schema(description = "시험 생성 시간", example = "2024-03-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @Schema(description = "시험지 정보")
    ExamSheetInfo examSheetInfo,
    
    @Schema(description = "시험에 포함된 모든 단원 정보")
    List<UnitSummaryDto> units,
    
    @Schema(description = "단원별 문제 수 통계")
    List<UnitQuestionCount> unitQuestionCounts
) implements Serializable {

    /**
     * 시험지 기본 정보
     */
    @Schema(description = "시험지 정보")
    public record ExamSheetInfo(
        @Schema(description = "시험지 고유 식별자")
        UUID examSheetId,
        
        @Schema(description = "시험지명")
        String examSheetName,
        
        @Schema(description = "총 문제 수")
        Integer totalQuestions,
        
        @Schema(description = "총 배점")
        Integer totalPoints
    ) implements Serializable {}
    
    /**
     * 단원별 문제 수 통계
     */
    @Schema(description = "단원별 문제 수 통계")
    public record UnitQuestionCount(
        @Schema(description = "단원 고유 식별자")
        UUID unitId,
        
        @Schema(description = "단원명")
        String unitName,
        
        @Schema(description = "해당 단원의 문제 수")
        Integer questionCount,
        
        @Schema(description = "해당 단원 문제들의 총 배점")
        Integer totalPoints
    ) implements Serializable {}

    /**
     * 기존 ExamDto에서 단원 정보를 추가하여 생성
     */
    public static ExamWithUnitsDto from(ExamDto examDto, List<UnitSummaryDto> units, List<UnitQuestionCount> unitQuestionCounts) {
        // Convert ExamDto.ExamSheetInfo to ExamWithUnitsDto.ExamSheetInfo
        ExamSheetInfo examSheetInfo = null;
        if (examDto.examSheetInfo() != null) {
            var dto = examDto.examSheetInfo();
            examSheetInfo = new ExamSheetInfo(
                dto.id(),
                dto.examName(),
                dto.totalQuestions(),
                dto.totalPoints()
            );
        }
        
        return new ExamWithUnitsDto(
            examDto.id(),
            examDto.examName(),
            examDto.grade(),
            examDto.content(),
            examDto.qrCodeUrl(),
            examDto.createdAt(),
            examSheetInfo,
            units,
            unitQuestionCounts
        );
    }

    /**
     * Compact Constructor - 필수 필드 검증
     */
    public ExamWithUnitsDto {
        if (id == null) throw new IllegalArgumentException("id는 필수입니다");
        if (examName == null || examName.isBlank()) throw new IllegalArgumentException("examName은 필수입니다");
        if (grade == null || grade < 1 || grade > 3) throw new IllegalArgumentException("grade는 1-3 사이여야 합니다");
        if (createdAt == null) throw new IllegalArgumentException("createdAt은 필수입니다");
        if (examSheetInfo == null) throw new IllegalArgumentException("examSheetInfo는 필수입니다");
        if (units == null) throw new IllegalArgumentException("units는 필수입니다");
        if (unitQuestionCounts == null) throw new IllegalArgumentException("unitQuestionCounts는 필수입니다");
    }

    /**
     * 단원 수 조회
     */
    public int getUnitCount() {
        return units.size();
    }

    /**
     * 총 문제 수 조회
     */
    public int getTotalQuestions() {
        return examSheetInfo.totalQuestions();
    }

    /**
     * 총 배점 조회  
     */
    public int getTotalPoints() {
        return examSheetInfo.totalPoints();
    }

    /**
     * 특정 단원의 문제 수 조회
     */
    public int getQuestionCountByUnit(UUID unitId) {
        return unitQuestionCounts.stream()
            .filter(count -> count.unitId().equals(unitId))
            .mapToInt(UnitQuestionCount::questionCount)
            .findFirst()
            .orElse(0);
    }
}