package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 등록 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateResponse {
    
    /**
     * 시험 ID
     */
    private Long examId;
    
    /**
     * 시험명
     */
    private String examName;
    
    /**
     * 학년
     */
    private Integer grade;
    
    /**
     * 학생 수
     */
    private Integer studentCount;
    
    /**
     * QR 코드 URL
     */
    private String qrCodeUrl;
    
    /**
     * 등록일시
     */
    private LocalDateTime createdAt;
}
