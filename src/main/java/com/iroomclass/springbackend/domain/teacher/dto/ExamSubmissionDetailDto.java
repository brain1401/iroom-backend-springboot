package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 시험 제출자 상세 현황 응답 DTO
 */
@Schema(description = "시험 제출자 상세 현황 응답")
public record ExamSubmissionDetailDto(
    @Schema(description = "시험 기본 정보")
    ExamBasicInfo examInfo,
    
    @Schema(description = "제출 통계")
    SubmissionStatistics statistics,
    
    @Schema(description = "제출한 학생 목록")
    List<StudentSubmissionInfo> submittedStudents,
    
    @Schema(description = "미제출 학생 목록")
    List<StudentInfo> notSubmittedStudents
) {
    
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
        
        @Schema(description = "문제 개수", example = "20")
        Integer questionCount,
        
        @Schema(description = "시험 설명", example = "1학기 중간고사 수학 시험")
        String examDescription
    ) {
        public static ExamBasicInfo create(
            UUID examId,
            String examName,
            Integer grade,
            LocalDateTime createdAt,
            Integer questionCount,
            String examDescription
        ) {
            return new ExamBasicInfo(
                examId,
                examName,
                grade,
                createdAt,
                questionCount,
                examDescription != null ? examDescription : ""
            );
        }
    }
    
    /**
     * 제출 통계 정보
     */
    @Schema(description = "제출 통계 정보")
    public record SubmissionStatistics(
        @Schema(description = "총 대상 학생 수 (해당 학년)", example = "30")
        Long totalStudentCount,
        
        @Schema(description = "제출 학생 수", example = "25") 
        Long submittedCount,
        
        @Schema(description = "미제출 학생 수", example = "5")
        Long notSubmittedCount,
        
        @Schema(description = "제출률 (%)", example = "83.33")
        BigDecimal submissionRate,
        
        @Schema(description = "평균 제출 시간 (시험 생성 후 경과 시간, 분 단위)", example = "45.5")
        BigDecimal averageSubmissionTime
    ) {
        public static SubmissionStatistics create(
            Long totalStudentCount,
            Long submittedCount,
            BigDecimal averageSubmissionTime
        ) {
            Long notSubmittedCount = totalStudentCount - submittedCount;
            
            BigDecimal submissionRate = BigDecimal.ZERO;
            if (totalStudentCount > 0) {
                submissionRate = BigDecimal.valueOf(submittedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalStudentCount), 2, RoundingMode.HALF_UP);
            }
            
            return new SubmissionStatistics(
                totalStudentCount,
                submittedCount,
                notSubmittedCount,
                submissionRate,
                averageSubmissionTime != null ? averageSubmissionTime : BigDecimal.ZERO
            );
        }
    }
    
    /**
     * 학생 기본 정보
     */
    @Schema(description = "학생 기본 정보")
    public record StudentInfo(
        @Schema(description = "학생 ID")
        Long studentId,
        
        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,
        
        @Schema(description = "학생 전화번호", example = "010-1234-5678")
        String studentPhone
    ) {
        public static StudentInfo create(Long studentId, String studentName, String studentPhone) {
            return new StudentInfo(studentId, studentName, studentPhone);
        }
    }
    
    /**
     * 제출한 학생 정보 (제출 시간 포함)
     */
    @Schema(description = "제출한 학생 정보")
    public record StudentSubmissionInfo(
        @Schema(description = "학생 ID")
        Long studentId,
        
        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,
        
        @Schema(description = "학생 전화번호", example = "010-1234-5678")
        String studentPhone,
        
        @Schema(description = "제출 시간")
        LocalDateTime submittedAt,
        
        @Schema(description = "시험 생성 후 제출까지 소요 시간 (분)", example = "45")
        Long submissionDurationMinutes,
        
        @Schema(description = "제출 순서 (1부터 시작)", example = "1")
        Integer submissionOrder
    ) {
        public static StudentSubmissionInfo create(
            Long studentId,
            String studentName, 
            String studentPhone,
            LocalDateTime submittedAt,
            LocalDateTime examCreatedAt,
            Integer submissionOrder
        ) {
            // 제출까지 소요 시간 계산 (분 단위)
            Long durationMinutes = null;
            if (submittedAt != null && examCreatedAt != null) {
                durationMinutes = java.time.Duration.between(examCreatedAt, submittedAt).toMinutes();
            }
            
            return new StudentSubmissionInfo(
                studentId,
                studentName,
                studentPhone,
                submittedAt,
                durationMinutes,
                submissionOrder
            );
        }
    }
    
    /**
     * 전체 정보를 조합하여 ExamSubmissionDetailDto 생성
     */
    public static ExamSubmissionDetailDto create(
        ExamBasicInfo examInfo,
        Long totalStudentCount,
        List<StudentSubmissionInfo> submittedStudents,
        List<StudentInfo> notSubmittedStudents
    ) {
        // 평균 제출 시간 계산
        BigDecimal averageSubmissionTime = BigDecimal.ZERO;
        if (!submittedStudents.isEmpty()) {
            double avgMinutes = submittedStudents.stream()
                .filter(s -> s.submissionDurationMinutes() != null)
                .mapToLong(StudentSubmissionInfo::submissionDurationMinutes)
                .average()
                .orElse(0.0);
            
            averageSubmissionTime = BigDecimal.valueOf(avgMinutes)
                .setScale(1, RoundingMode.HALF_UP);
        }
        
        // 통계 정보 생성
        SubmissionStatistics statistics = SubmissionStatistics.create(
            totalStudentCount,
            (long) submittedStudents.size(),
            averageSubmissionTime
        );
        
        return new ExamSubmissionDetailDto(
            examInfo,
            statistics,
            submittedStudents,
            notSubmittedStudents
        );
    }
}