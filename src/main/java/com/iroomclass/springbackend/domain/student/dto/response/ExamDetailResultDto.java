package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 상세 시험 결과 응답 DTO
 * 
 * <p>학생의 응시한 시험의 상세 결과를 담는 DTO입니다.
 * 단원 정보, 문제별 질문과 답안을 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "상세 시험 결과 정보")
public record ExamDetailResultDto(
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "채점 날짜", example = "2024-08-17T15:00:00")
    LocalDateTime gradedAt,
    
    @Schema(description = "총 문제 문항 수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "객관식 문제 수", example = "15")
    Integer objectiveCount,
    
    @Schema(description = "주관식 문제 수", example = "5")
    Integer subjectiveCount,
    
    @Schema(description = "총 점수", example = "85")
    Integer totalScore,
    
    @Schema(description = "단원 정보 목록")
    List<UnitInfo> units,
    
    @Schema(description = "문제별 질문과 답안 목록")
    List<QuestionAnswer> questionAnswers
    
) {
    /**
     * 단원 정보 내부 클래스
     */
    @Schema(description = "단원 정보")
    public record UnitInfo(
        @Schema(description = "단원 고유 ID") UUID unitId,
        @Schema(description = "단원명", example = "일차방정식") String unitName
    ) {}
    
    /**
     * 문제별 질문과 답안 내부 클래스
     */
    @Schema(description = "문제별 질문과 답안")
    public record QuestionAnswer(
        @Schema(description = "문제 번호", example = "1") Integer questionNumber,
        @Schema(description = "문제 내용") String questionText,
        @Schema(description = "학생이 입력한 답안") String studentAnswer,
        @Schema(description = "문제 유형", example = "OBJECTIVE") String questionType
    ) {}
    
    /**
     * 정적 팩토리 메서드
     */
    public static ExamDetailResultDto of(String examName, LocalDateTime gradedAt,
                                        Integer totalQuestions, Integer objectiveCount, Integer subjectiveCount,
                                        Integer totalScore, List<UnitInfo> units, List<QuestionAnswer> questionAnswers) {
        return new ExamDetailResultDto(examName, gradedAt, totalQuestions, 
                                     objectiveCount, subjectiveCount, totalScore, units, questionAnswers);
    }
}