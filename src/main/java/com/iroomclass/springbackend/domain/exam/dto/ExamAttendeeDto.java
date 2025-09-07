package com.iroomclass.springbackend.domain.exam.dto;

import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시험 응시자 정보 DTO
 * 
 * <p>
 * 특정 시험에 응시한 학생들의 정보를 제공합니다.
 * 페이징 처리를 통해 대량의 응시자 정보를 효율적으로 조회할 수 있습니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 응시자 정보")
public record ExamAttendeeDto(
    @Schema(description = "제출 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID submissionId,
    
    @Schema(description = "학생 ID", example = "1")
    Long studentId,
    
    @Schema(description = "학생 이름", example = "홍길동")
    String studentName,
    
    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String studentPhone,
    
    @Schema(description = "학생 생년월일", example = "2008-03-15")
    LocalDate studentBirthDate,
    
    @Schema(description = "제출 일시", example = "2025-01-20T14:30:00")
    LocalDateTime submittedAt,
    
    @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID examId,
    
    @Schema(description = "시험명", example = "2학년 중간고사")
    String examName
) {
    /**
     * ExamSubmission 엔티티로부터 DTO 생성
     * 
     * @param submission 시험 제출 엔티티 (Student와 Exam이 fetch된 상태여야 함)
     * @return 응시자 정보 DTO
     */
    public static ExamAttendeeDto from(ExamSubmission submission) {
        return new ExamAttendeeDto(
            submission.getId(),
            submission.getStudent().getId(),
            submission.getStudent().getName(),
            submission.getStudent().getPhone(),
            submission.getStudent().getBirthDate(),
            submission.getSubmittedAt(),
            submission.getExam().getId(),
            submission.getExam().getExamName()
        );
    }
    
    /**
     * 간단한 응시자 정보 생성 (시험 정보 제외)
     * 
     * @param submission 시험 제출 엔티티
     * @return 응시자 정보 DTO (시험 정보 null)
     */
    public static ExamAttendeeDto fromWithoutExam(ExamSubmission submission) {
        return new ExamAttendeeDto(
            submission.getId(),
            submission.getStudent().getId(),
            submission.getStudent().getName(),
            submission.getStudent().getPhone(),
            submission.getStudent().getBirthDate(),
            submission.getSubmittedAt(),
            null,
            null
        );
    }
}