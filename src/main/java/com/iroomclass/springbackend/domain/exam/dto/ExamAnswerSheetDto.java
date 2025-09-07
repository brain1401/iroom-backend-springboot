package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult.ResultStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 시험 답안지 조회 응답 DTO
 * 
 * <p>특정 시험 제출에 대한 학생의 전체 답안 정보를 담는 DTO입니다.
 * 학생 정보, 시험 정보, 각 문제별 답안 및 채점 결과를 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안지 상세 정보 (채점 결과 포함)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamAnswerSheetDto(
    
    @Schema(description = "제출 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID submissionId,
    
    @Schema(description = "학생 정보")
    StudentInfo studentInfo,
    
    @Schema(description = "시험 정보")
    ExamInfo examInfo,
    
    @Schema(description = "답안 제출 시간", example = "2025-01-20T10:30:00")
    LocalDateTime submittedAt,
    
    @Schema(description = "총 문제 수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "답변한 문제 수", example = "18")
    Integer answeredQuestions,
    
    @Schema(description = "문제별 답안 목록")
    List<QuestionAnswerDto> questionAnswers,
    
    // === 채점 결과 정보 추가 ===
    @Schema(description = "채점 결과 정보 (채점이 완료된 경우)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    GradingResult gradingResult
) {
    
    /**
     * 학생 정보 내부 레코드
     */
    @Schema(description = "학생 정보")
    public record StudentInfo(
        @Schema(description = "학생 ID", example = "1")
        Long studentId,
        
        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,
        
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber
    ) {}
    
    /**
     * 시험 정보 내부 레코드
     */
    @Schema(description = "시험 정보")
    public record ExamInfo(
        @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID examId,
        
        @Schema(description = "시험명", example = "2025년 1학기 중간고사")
        String examName,
        
        @Schema(description = "학년", example = "2")
        Integer grade,
        
        @Schema(description = "시험 생성일", example = "2025-01-15T09:00:00")
        LocalDateTime createdAt
    ) {}
    
    /**
     * 채점 결과 정보 내부 레코드
     */
    @Schema(description = "채점 결과 정보")
    public record GradingResult(
        @Schema(description = "채점 결과 ID", example = "550e8400-e29b-41d4-a716-446655440004")
        UUID examResultId,
        
        @Schema(description = "총점", example = "85")
        Integer totalScore,
        
        @Schema(description = "채점 상태", example = "COMPLETED", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "REGRADED"})
        ResultStatus status,
        
        @Schema(description = "채점 완료 시간", example = "2025-01-20T11:00:00")
        LocalDateTime gradedAt,
        
        @Schema(description = "채점 코멘트", example = "잘했습니다. 다음번에는 계산 실수를 줄이면 더 좋은 결과가 있을 것입니다.")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        String scoringComment,
        
        @Schema(description = "정답 문제 수", example = "17")
        Integer correctAnswers,
        
        @Schema(description = "오답 문제 수", example = "3")
        Integer wrongAnswers
    ) {}
    
    /**
     * 문제별 답안 내부 레코드
     */
    @Schema(description = "문제별 답안 정보")
    public record QuestionAnswerDto(
        @Schema(description = "문제 번호", example = "1")
        Integer questionNumber,
        
        @Schema(description = "문제 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID questionId,
        
        @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE", allowableValues = {"MULTIPLE_CHOICE", "SUBJECTIVE"})
        String questionType,
        
        @Schema(description = "문제 내용", example = "다음 중 가장 큰 수는?")
        String questionText,
        
        @Schema(description = "객관식 선택지 (객관식인 경우만)", example = "[\"1. 15\", \"2. 20\", \"3. 25\", \"4. 30\", \"5. 35\"]")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> choices,
        
        @Schema(description = "학생 답안", example = "3")
        String studentAnswer,
        
        @Schema(description = "정답", example = "5")
        String correctAnswer,
        
        @Schema(description = "답안 제출 여부", example = "true")
        Boolean isAnswered,
        
        @Schema(description = "정답 여부 (채점된 경우)", example = "false")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Boolean isCorrect,
        
        @Schema(description = "획득 점수 (채점된 경우)", example = "0")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer score,
        
        @Schema(description = "배점", example = "5")
        Integer maxScore,
        
        @Schema(description = "채점 피드백 (채점된 경우)", example = "정답은 5번입니다.")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String feedback,
        
        @Schema(description = "단원 정보")
        UnitInfo unitInfo
    ) {
        /**
         * 단원 정보 내부 레코드
         */
        @Schema(description = "단원 정보")
        public record UnitInfo(
            @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440003")
            UUID unitId,
            
            @Schema(description = "단원명", example = "방정식")
            String unitName,
            
            @Schema(description = "단원 코드", example = "MAT-2-01")
            String unitCode
        ) {}
    }
}