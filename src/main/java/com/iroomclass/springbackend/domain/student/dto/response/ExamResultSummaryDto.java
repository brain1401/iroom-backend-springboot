package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 시험 결과 요약 응답 DTO
 * 
 * <p>학생의 응시한 시험 결과 요약 정보를 담는 DTO입니다.
 * 맞춘 문제 수, 틀린 문제 수, 총 문항 수를 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 결과 요약 정보", example = """
    {
      "examName": "중간고사",
      "gradedAt": "2024-08-17T15:00:00",
      "correctCount": 15,
      "incorrectCount": 5,
      "totalQuestions": 20
    }
    """)
public record ExamResultSummaryDto(
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "채점 날짜", example = "2024-08-17T15:00:00")
    LocalDateTime gradedAt,
    
    @Schema(description = "맞춘 문제 수", example = "15")
    Long correctCount,
    
    @Schema(description = "틀린 문제 수", example = "5")
    Long incorrectCount,
    
    @Schema(description = "총 문항 수", example = "20")
    Long totalQuestions
    
) {
    /**
     * 정적 팩토리 메서드
     * 
     * @param examName       시험명
     * @param gradedAt       채점 날짜
     * @param correctCount   맞춘 문제 수
     * @param incorrectCount 틀린 문제 수
     * @param totalQuestions 총 문항 수
     * @return ExamResultSummaryDto 인스턴스
     */
    public static ExamResultSummaryDto of(String examName, LocalDateTime gradedAt, 
                                         Long correctCount, Long incorrectCount, Long totalQuestions) {
        return new ExamResultSummaryDto(examName, gradedAt, correctCount, incorrectCount, totalQuestions);
    }
    
    /**
     * 정답률 계산
     * 
     * @return 정답률 (0.0 ~ 1.0)
     */
    public double getAccuracyRate() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) correctCount / totalQuestions;
    }
}