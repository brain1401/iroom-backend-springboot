package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 학생별 상세 답안 조회 응답 DTO
 */
@Schema(description = "학생별 상세 답안 조회 응답")
public record StudentAnswerDetailDto(
    @Schema(description = "학생 정보")
    StudentBasicInfo studentInfo,
    
    @Schema(description = "시험 정보")
    ExamBasicInfo examInfo,
    
    @Schema(description = "제출 정보")
    SubmissionInfo submissionInfo,
    
    @Schema(description = "채점 결과 요약")
    GradingResult gradingResult,
    
    @Schema(description = "문제별 답안 및 채점 결과")
    List<QuestionAnswer> questionAnswers
) {
    
    /**
     * 학생 기본 정보
     */
    @Schema(description = "학생 기본 정보")
    public record StudentBasicInfo(
        @Schema(description = "학생 ID")
        Long studentId,
        
        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,
        
        @Schema(description = "학생 전화번호", example = "010-1234-5678")
        String studentPhone
    ) {
        public static StudentBasicInfo create(Long studentId, String studentName, String studentPhone) {
            return new StudentBasicInfo(studentId, studentName, studentPhone);
        }
    }
    
    /**
     * 시험 기본 정보
     */
    @Schema(description = "시험 기본 정보")
    public record ExamBasicInfo(
        @Schema(description = "시험 ID")
        UUID examId,
        
        @Schema(description = "시험명", example = "1학기 중간고사")
        String examName,
        
        @Schema(description = "학년", example = "1")
        Integer grade,
        
        @Schema(description = "시험 생성일시")
        LocalDateTime createdAt,
        
        @Schema(description = "총 문제 수", example = "20")
        Integer totalQuestions,
        
        @Schema(description = "시험 만점", example = "100")
        Integer totalPossibleScore
    ) {
        public static ExamBasicInfo create(
            UUID examId,
            String examName,
            Integer grade,
            LocalDateTime createdAt,
            Integer totalQuestions,
            Integer totalPossibleScore
        ) {
            return new ExamBasicInfo(
                examId,
                examName,
                grade,
                createdAt,
                totalQuestions,
                totalPossibleScore
            );
        }
    }
    
    /**
     * 제출 정보
     */
    @Schema(description = "제출 정보")
    public record SubmissionInfo(
        @Schema(description = "제출 ID")
        UUID submissionId,
        
        @Schema(description = "제출 시간")
        LocalDateTime submittedAt,
        
        @Schema(description = "시험 소요 시간 (분)", example = "45")
        Long durationMinutes
    ) {
        public static SubmissionInfo create(
            UUID submissionId,
            LocalDateTime submittedAt,
            LocalDateTime examCreatedAt
        ) {
            Long durationMinutes = null;
            if (submittedAt != null && examCreatedAt != null) {
                durationMinutes = java.time.Duration.between(examCreatedAt, submittedAt).toMinutes();
            }
            
            return new SubmissionInfo(submissionId, submittedAt, durationMinutes);
        }
    }
    
    /**
     * 채점 결과 요약
     */
    @Schema(description = "채점 결과 요약")
    public record GradingResult(
        @Schema(description = "총점", example = "85")
        Integer totalScore,
        
        @Schema(description = "만점", example = "100")
        Integer totalPossibleScore,
        
        @Schema(description = "득점률 (%)", example = "85.0")
        BigDecimal scorePercentage,
        
        @Schema(description = "정답 개수", example = "17")
        Integer correctAnswers,
        
        @Schema(description = "오답 개수", example = "3")
        Integer incorrectAnswers,
        
        @Schema(description = "채점 시간")
        LocalDateTime gradedAt,
        
        @Schema(description = "채점 버전", example = "1")
        Integer version,
        
        @Schema(description = "전체 코멘트", example = "전반적으로 잘 풀었으나 계산 실수가 있었습니다.")
        String overallComment
    ) {
        public static GradingResult create(
            Integer totalScore,
            Integer totalPossibleScore,
            Integer correctAnswers,
            Integer incorrectAnswers,
            LocalDateTime gradedAt,
            Integer version,
            String overallComment
        ) {
            // Null safety: totalScore가 null인 경우 0으로 처리
            int safeTotalScore = totalScore != null ? totalScore : 0;
            int safeTotalPossibleScore = totalPossibleScore != null ? totalPossibleScore : 0;
            
            BigDecimal scorePercentage = BigDecimal.ZERO;
            if (safeTotalPossibleScore > 0) {
                scorePercentage = BigDecimal.valueOf(safeTotalScore)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(safeTotalPossibleScore), 1, RoundingMode.HALF_UP);
            }
            
            return new GradingResult(
                safeTotalScore,
                safeTotalPossibleScore,
                scorePercentage,
                correctAnswers != null ? correctAnswers : 0,
                incorrectAnswers != null ? incorrectAnswers : 0,
                gradedAt,
                version != null ? version : 1,
                overallComment != null ? overallComment : ""
            );
        }
    }
    
    /**
     * 문제별 답안 및 채점 결과
     */
    @Schema(description = "문제별 답안 및 채점 결과")
    public record QuestionAnswer(
        @Schema(description = "문제 ID")
        UUID questionId,
        
        @Schema(description = "문제 순서", example = "1")
        Integer questionOrder,
        
        @Schema(description = "문제 내용 (요약)", example = "다음 식을 계산하시오: 2 + 3 = ?")
        String questionSummary,
        
        @Schema(description = "문제 유형 (주관식/객관식)")
        String questionType,
        
        @Schema(description = "문제 난이도")
        String difficulty,
        
        @Schema(description = "배점", example = "5")
        Integer points,
        
        @Schema(description = "학생 답안", example = "5")
        String studentAnswer,
        
        @Schema(description = "정답", example = "5")
        String correctAnswer,
        
        @Schema(description = "획득 점수", example = "5")
        Integer earnedScore,
        
        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,
        
        @Schema(description = "문제별 피드백", example = "정답입니다. 잘했어요!")
        String feedback,
        
        @Schema(description = "관련 단원 정보")
        UnitInfo unitInfo
    ) {
        /**
         * 단원 정보
         */
        @Schema(description = "단원 정보")
        public record UnitInfo(
            @Schema(description = "단원 ID")
            UUID unitId,
            
            @Schema(description = "단원명", example = "정수의 덧셈")
            String unitName,
            
            @Schema(description = "중분류", example = "정수와 유리수")
            String subcategoryName,
            
            @Schema(description = "대분류", example = "수와 연산")
            String categoryName
        ) {
            public static UnitInfo create(
                UUID unitId,
                String unitName,
                String subcategoryName,
                String categoryName
            ) {
                return new UnitInfo(unitId, unitName, subcategoryName, categoryName);
            }
        }
        
        /**
         * 문제 배점 반환 (maxPoints() 호환)
         * @return 문제 배점
         */
        public Integer maxPoints() {
            return this.points;
        }
        
        public static QuestionAnswer create(
            UUID questionId,
            Integer questionOrder,
            String questionSummary,
            String questionType,
            String difficulty,
            Integer points,
            String studentAnswer,
            String correctAnswer,
            Integer earnedScore,
            Boolean isCorrect,
            String feedback,
            UnitInfo unitInfo
        ) {
            return new QuestionAnswer(
                questionId,
                questionOrder,
                questionSummary,
                questionType,
                difficulty,
                points,
                studentAnswer != null ? studentAnswer : "",
                correctAnswer != null ? correctAnswer : "",
                earnedScore,
                isCorrect,
                feedback != null ? feedback : "",
                unitInfo
            );
        }
    }
    
    /**
     * 전체 정보를 조합하여 StudentAnswerDetailDto 생성
     */
    public static StudentAnswerDetailDto create(
        StudentBasicInfo studentInfo,
        ExamBasicInfo examInfo,
        SubmissionInfo submissionInfo,
        GradingResult gradingResult,
        List<QuestionAnswer> questionAnswers
    ) {
        return new StudentAnswerDetailDto(
            studentInfo,
            examInfo,
            submissionInfo,
            gradingResult,
            questionAnswers
        );
    }
}