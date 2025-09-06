package com.iroomclass.springbackend.domain.exam.dto;

import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * 시험 문제 조회 응답 DTO
 * 
 * <p>시험에 포함된 모든 문제 정보와 통계를 포함하는 응답 데이터입니다.</p>
 * <p>학생이 시험을 제출할 때 필요한 모든 정보를 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 문제 조회 응답")
public record ExamQuestionsResponseDto(
    @Schema(description = "시험 고유 식별자")
    UUID examId,
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "학년", example = "2")
    Integer grade,
    
    @Schema(description = "총 문제 수", example = "10")
    Integer totalQuestions,
    
    @Schema(description = "객관식 문제 수", example = "5")
    Integer multipleChoiceCount,
    
    @Schema(description = "주관식 문제 수", example = "5")
    Integer subjectiveCount,
    
    @Schema(description = "총 배점", example = "100")
    Integer totalPoints,
    
    @Schema(description = "문제 목록 (순서대로 정렬)")
    List<QuestionDetailDto> questions
) {
    /**
     * Exam과 ExamSheetQuestion 목록으로부터 응답 DTO 생성
     * 
     * @param exam 시험 엔티티
     * @param examSheetQuestions 시험지 문제 목록 (seqNo 순으로 정렬된 상태)
     * @return 시험 문제 응답 DTO
     */
    public static ExamQuestionsResponseDto from(Exam exam, List<ExamSheetQuestion> examSheetQuestions) {
        List<QuestionDetailDto> questions = examSheetQuestions.stream()
                .map(QuestionDetailDto::from)
                .toList();
        
        // 문제 유형별 개수 계산
        long multipleChoiceCount = questions.stream()
                .filter(QuestionDetailDto::isMultipleChoice)
                .count();
        
        long subjectiveCount = questions.stream()
                .filter(QuestionDetailDto::isSubjective)
                .count();
        
        // 총 배점 계산
        int totalPoints = questions.stream()
                .mapToInt(QuestionDetailDto::points)
                .sum();
        
        return new ExamQuestionsResponseDto(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                questions.size(),
                (int) multipleChoiceCount,
                (int) subjectiveCount,
                totalPoints,
                questions
        );
    }
    
    /**
     * 문제가 있는지 확인
     * 
     * @return 문제가 하나라도 있으면 true
     */
    public boolean hasQuestions() {
        return questions != null && !questions.isEmpty();
    }
    
    /**
     * 객관식 문제가 있는지 확인
     * 
     * @return 객관식 문제가 있으면 true
     */
    public boolean hasMultipleChoiceQuestions() {
        return multipleChoiceCount != null && multipleChoiceCount > 0;
    }
    
    /**
     * 주관식 문제가 있는지 확인
     * 
     * @return 주관식 문제가 있으면 true
     */
    public boolean hasSubjectiveQuestions() {
        return subjectiveCount != null && subjectiveCount > 0;
    }
    
    /**
     * 문제 유형별 비율 정보
     * 
     * @return 문제 유형 비율 정보
     */
    @Schema(description = "문제 유형 비율 정보")
    public QuestionTypeRatio getQuestionTypeRatio() {
        if (!hasQuestions()) {
            return new QuestionTypeRatio(0.0, 0.0);
        }
        
        double multipleChoiceRatio = (double) multipleChoiceCount / totalQuestions * 100;
        double subjectiveRatio = (double) subjectiveCount / totalQuestions * 100;
        
        return new QuestionTypeRatio(
                Math.round(multipleChoiceRatio * 10.0) / 10.0,  // 소수점 첫째 자리까지
                Math.round(subjectiveRatio * 10.0) / 10.0
        );
    }
    
    /**
     * 문제 유형별 비율 정보
     */
    @Schema(description = "문제 유형 비율")
    public record QuestionTypeRatio(
        @Schema(description = "객관식 비율 (%)", example = "50.0")
        Double multipleChoiceRatio,
        
        @Schema(description = "주관식 비율 (%)", example = "50.0")  
        Double subjectiveRatio
    ) {}
    
    /**
     * 시험 요약 정보
     * 
     * @return 시험 요약 정보
     */
    @Schema(description = "시험 요약 정보")
    public ExamSummary getExamSummary() {
        return new ExamSummary(
                examName,
                grade,
                totalQuestions,
                totalPoints,
                String.format("객관식 %d문항, 주관식 %d문항", multipleChoiceCount, subjectiveCount)
        );
    }
    
    /**
     * 시험 요약 정보
     */
    @Schema(description = "시험 요약")
    public record ExamSummary(
        @Schema(description = "시험명")
        String examName,
        
        @Schema(description = "학년")
        Integer grade,
        
        @Schema(description = "총 문제 수")
        Integer totalQuestions,
        
        @Schema(description = "총 배점") 
        Integer totalPoints,
        
        @Schema(description = "문제 구성 설명", example = "객관식 5문항, 주관식 5문항")
        String questionComposition
    ) {}
}