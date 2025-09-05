package com.iroomclass.springbackend.domain.exam.dto;

import com.iroomclass.springbackend.domain.exam.entity.Exam;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시험 응답 DTO
 */
@Schema(description = "시험 응답")
public record ExamDto(
    @Schema(description = "시험 ID")
    UUID id,
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "학년", example = "2")
    Integer grade,
    
    @Schema(description = "시험 내용/설명", example = "2학년 중간고사입니다")
    String content,
    
    @Schema(description = "QR 코드 URL")
    String qrCodeUrl,
    
    @Schema(description = "시험 생성일시")
    LocalDateTime createdAt,
    
    @Schema(description = "연결된 시험지 정보")
    ExamSheetInfo examSheetInfo
) {
    /**
     * Exam 엔티티로부터 기본 DTO 생성
     * 
     * @param exam 시험 엔티티
     * @return 시험 DTO
     */
    public static ExamDto from(Exam exam) {
        return new ExamDto(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getContent(),
                exam.getQrCodeUrl(),
                exam.getCreatedAt(),
                null // ExamSheet 정보 없음
        );
    }
    
    /**
     * Exam 엔티티로부터 DTO 생성 (시험지 정보 포함)
     * 
     * @param exam 시험 엔티티 (ExamSheet 포함)
     * @return 시험 DTO
     */
    public static ExamDto fromWithExamSheet(Exam exam) {
        ExamSheetInfo examSheetInfo = null;
        if (exam.getExamSheet() != null) {
            examSheetInfo = ExamSheetInfo.from(exam.getExamSheet());
        }
        
        return new ExamDto(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getContent(),
                exam.getQrCodeUrl(),
                exam.getCreatedAt(),
                examSheetInfo
        );
    }
    
    /**
     * 시험지 정보 DTO (간단한 정보만)
     */
    @Schema(description = "시험지 정보")
    public record ExamSheetInfo(
        @Schema(description = "시험지 ID")
        UUID id,
        
        @Schema(description = "시험지 이름", example = "중간고사 문제지")
        String examName,
        
        @Schema(description = "총 문제 수", example = "20")
        Integer totalQuestions,
        
        @Schema(description = "총 배점", example = "100")
        Integer totalPoints,
        
        @Schema(description = "시험지 생성일시")
        LocalDateTime createdAt
    ) {
        public static ExamSheetInfo from(com.iroomclass.springbackend.domain.exam.entity.ExamSheet examSheet) {
            // Repository 기반 정확한 데이터 사용 (카르테시안 곱 문제 해결)
            Integer totalQuestions = examSheet.getTotalQuestions();
            Integer totalPoints = examSheet.getTotalPoints();
            
            return new ExamSheetInfo(
                    examSheet.getId(),
                    examSheet.getExamName(),
                    totalQuestions,
                    totalPoints,
                    examSheet.getCreatedAt()
            );
        }
    }
}