package com.iroomclass.springbackend.domain.exam.dto;

import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 시험지 응답 DTO
 * 
 * <p>시험지 정보를 클라이언트에 전달하기 위한 데이터 전송 객체입니다.</p>
 */
@Schema(name = "ExamSheetDto", description = "시험지 정보 응답 DTO")
public record ExamSheetDto(
    
    @Schema(description = "시험지 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "총 문제 수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "객관식 문제 수", example = "15")
    Integer multipleChoiceCount,
    
    @Schema(description = "주관식 문제 수", example = "5")
    Integer subjectiveCount,
    
    @Schema(description = "총 배점", example = "100")
    Integer totalPoints,
    
    @Schema(description = "평균 문제 배점", example = "5.0")
    Double averagePointsPerQuestion,
    
    @Schema(description = "생성 시간", example = "2024-08-17T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정 시간", example = "2024-08-17T15:45:00")
    LocalDateTime updatedAt,
    
    @Schema(description = "문제 목록 (상세 조회 시에만 포함)")
    List<QuestionInfo> questions
    
) {
    
    /**
     * ExamSheet 엔티티로부터 기본 DTO 생성 (문제 목록 제외)
     * 
     * @param examSheet ExamSheet 엔티티
     * @return ExamSheetDto
     */
    public static ExamSheetDto from(ExamSheet examSheet) {
        return new ExamSheetDto(
            examSheet.getId(),
            examSheet.getExamName(),
            examSheet.getGrade(),
            examSheet.getTotalQuestions(),
            examSheet.getMultipleChoiceCount(),
            examSheet.getSubjectiveCount(),
            calculateTotalPoints(examSheet),
            calculateAveragePointsPerQuestion(examSheet),
            examSheet.getCreatedAt(),
            examSheet.getUpdatedAt(),
            null // 문제 목록은 상세 조회 시에만 포함
        );
    }
    
    /**
     * ExamSheet 엔티티로부터 상세 DTO 생성 (문제 목록 포함)
     * 
     * @param examSheet ExamSheet 엔티티 (문제 목록 포함)
     * @return ExamSheetDto
     */
    public static ExamSheetDto fromWithQuestions(ExamSheet examSheet) {
        List<QuestionInfo> questionInfos = null;
        
        if (examSheet.getQuestions() != null) {
            questionInfos = examSheet.getQuestions().stream()
                .map(QuestionInfo::from)
                .toList();
        }
        
        return new ExamSheetDto(
            examSheet.getId(),
            examSheet.getExamName(),
            examSheet.getGrade(),
            examSheet.getTotalQuestions(),
            examSheet.getMultipleChoiceCount(),
            examSheet.getSubjectiveCount(),
            calculateTotalPoints(examSheet),
            calculateAveragePointsPerQuestion(examSheet),
            examSheet.getCreatedAt(),
            examSheet.getUpdatedAt(),
            questionInfos
        );
    }
    
    /**
     * 총 배점 계산
     * 
     * @param examSheet ExamSheet 엔티티
     * @return 총 배점
     */
    private static Integer calculateTotalPoints(ExamSheet examSheet) {
        if (examSheet.getQuestions() == null || examSheet.getQuestions().isEmpty()) {
            return 100; // 기본 총점
        }
        
        return examSheet.getQuestions().stream()
            .mapToInt(ExamSheetQuestion::getPoints)
            .sum();
    }
    
    /**
     * 평균 문제 배점 계산
     * 
     * @param examSheet ExamSheet 엔티티
     * @return 평균 배점 (소수점 1자리)
     */
    private static Double calculateAveragePointsPerQuestion(ExamSheet examSheet) {
        Integer totalQuestions = examSheet.getTotalQuestions();
        if (totalQuestions == null || totalQuestions == 0) {
            return 0.0;
        }
        
        Integer totalPoints = calculateTotalPoints(examSheet);
        return Math.round((double) totalPoints / totalQuestions * 10.0) / 10.0;
    }
    
    /**
     * 문제 정보 DTO
     * 
     * <p>시험지에 포함된 문제의 기본 정보를 제공합니다.</p>
     */
    @Schema(name = "QuestionInfo", description = "문제 정보 DTO")
    public record QuestionInfo(
        
        @Schema(description = "문제 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID questionId,
        
        @Schema(description = "문제 순서", example = "1")
        Integer seqNo,
        
        @Schema(description = "문제 배점", example = "5")
        Integer points,
        
        @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE", 
                allowableValues = {"MULTIPLE_CHOICE", "SUBJECTIVE"})
        String questionType,
        
        @Schema(description = "문제 난이도", example = "MEDIUM", 
                allowableValues = {"EASY", "MEDIUM", "HARD"})
        String difficulty,
        
        @Schema(description = "문제 텍스트 (간략)", example = "다음 중 올바른 답을 고르시오.")
        String questionText,
        
        @Schema(description = "단원 정보")
        UnitInfo unitInfo,
        
        @Schema(description = "선택 방식", example = "MANUAL", 
                allowableValues = {"MANUAL", "RANDOM"})
        String selectionMethod
        
    ) {
        
        /**
         * ExamSheetQuestion으로부터 QuestionInfo 생성
         * 
         * @param examSheetQuestion ExamSheetQuestion 엔티티
         * @return QuestionInfo
         */
        public static QuestionInfo from(ExamSheetQuestion examSheetQuestion) {
            Question question = examSheetQuestion.getQuestion();
            UnitInfo unitInfo = null;
            
            if (question != null && question.getUnit() != null) {
                unitInfo = UnitInfo.from(question.getUnit());
            }
            
            return new QuestionInfo(
                question != null ? question.getId() : null,
                examSheetQuestion.getSeqNo(),
                examSheetQuestion.getPoints(),
                question != null ? question.getQuestionType().name() : null,
                question != null ? question.getDifficulty().name() : null,
                question != null ? truncateText(question.getQuestionText(), 50) : null,
                unitInfo,
                examSheetQuestion.getSelectionMethod().name()
            );
        }
        
        /**
         * 텍스트 자르기 (미리보기용)
         * 
         * @param text 원본 텍스트
         * @param maxLength 최대 길이
         * @return 자른 텍스트
         */
        private static String truncateText(String text, int maxLength) {
            if (text == null) return null;
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength) + "...";
        }
    }
    
    /**
     * 단원 정보 DTO
     * 
     * <p>문제가 속한 단원의 기본 정보를 제공합니다.</p>
     */
    @Schema(name = "UnitInfo", description = "단원 정보 DTO")
    public record UnitInfo(
        
        @Schema(description = "단원 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID unitId,
        
        @Schema(description = "단원명", example = "정수")
        String unitName,
        
        @Schema(description = "단원 코드", example = "MATH_1_01")
        String unitCode,
        
        @Schema(description = "중분류명", example = "정수와 유리수")
        String subcategoryName,
        
        @Schema(description = "대분류명", example = "수와 연산")
        String categoryName
        
    ) {
        
        /**
         * Unit 엔티티로부터 UnitInfo 생성
         * 
         * @param unit Unit 엔티티
         * @return UnitInfo
         */
        public static UnitInfo from(com.iroomclass.springbackend.domain.unit.entity.Unit unit) {
            return new UnitInfo(
                unit.getId(),
                unit.getUnitName(),
                unit.getUnitCode(),
                unit.getSubcategory() != null ? unit.getSubcategory().getSubcategoryName() : null,
                unit.getSubcategory() != null && unit.getSubcategory().getCategory() != null 
                    ? unit.getSubcategory().getCategory().getCategoryName() : null
            );
        }
    }
}