package com.iroomclass.springbackend.domain.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 제출 생성 응답 DTO
 * 
 * 시험 제출 완료 후 반환되는 정보입니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionCreateResponse {
    
    /**
     * 시험 제출 ID
     */
    private Long submissionId;
    
    /**
     * 시험 ID
     */
    private Long examId;
    
    /**
     * 시험 이름
     */
    private String examName;
    
    /**
     * 학생 이름
     */
    private String studentName;
    
    /**
     * 학생 전화번호
     */
    private String studentPhone;
    
    /**
     * 제출일시
     */
    private LocalDateTime submittedAt;
    
    /**
     * QR 코드 URL (시험 접속용)
     */
    private String qrCodeUrl;
}
