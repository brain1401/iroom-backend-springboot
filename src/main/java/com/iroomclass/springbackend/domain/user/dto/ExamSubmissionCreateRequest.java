package com.iroomclass.springbackend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 제출 생성 요청 DTO
 * 
 * 학생이 시험을 제출할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionCreateRequest {
    
    /**
     * 시험 ID
     */
    @NotNull(message = "시험 ID는 필수입니다.")
    private Long examId;
    
    /**
     * 학생 이름
     */
    @NotBlank(message = "학생 이름은 필수입니다.")
    private String studentName;
    
    /**
     * 학생 전화번호
     */
    @NotBlank(message = "학생 전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", 
             message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
    private String studentPhone;
}
