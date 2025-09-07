package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 학생 시험 이력 DTO
 * 
 * <p>학생이 응시한 시험의 정보를 담는 DTO입니다.
 * 시험명, 시험 ID, 응시일, 문제 수, 점수 정보를 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = "학생 시험 이력 정보",
    example = """
        {
          "examId": "550e8400-e29b-41d4-a716-446655440000",
          "examName": "1학년 1학기 중간고사",
          "submittedAt": "2025-01-09T14:30:00",
          "totalQuestions": 20,
          "totalScore": 85
        }
        """
)
public record StudentExamHistoryDto(
    
    @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID examId,
    
    @Schema(description = "시험명", example = "1학년 1학기 중간고사")
    String examName,
    
    @Schema(description = "응시일시", example = "2025-01-09T14:30:00")
    LocalDateTime submittedAt,
    
    @Schema(description = "총 문제 수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "총점", example = "85")
    Integer totalScore
    
) {
    /**
     * 정적 팩토리 메서드 - ExamSubmission과 ExamResult로부터 생성
     * 
     * @param examId 시험 ID
     * @param examName 시험명
     * @param submittedAt 제출일시
     * @param totalQuestions 총 문제 수
     * @param totalScore 총점
     * @return StudentExamHistoryDto 인스턴스
     */
    public static StudentExamHistoryDto of(
            UUID examId,
            String examName,
            LocalDateTime submittedAt,
            Integer totalQuestions,
            Integer totalScore) {
        return new StudentExamHistoryDto(
            examId,
            examName,
            submittedAt,
            totalQuestions != null ? totalQuestions : 0,
            totalScore != null ? totalScore : 0
        );
    }
}