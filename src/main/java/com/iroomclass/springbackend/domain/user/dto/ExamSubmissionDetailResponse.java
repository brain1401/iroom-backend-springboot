package com.iroomclass.springbackend.domain.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 제출 상세 조회 응답 DTO
 * 
 * 특정 시험 제출의 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionDetailResponse {
    
    private Long submissionId;           // 제출 ID
    private Long examId;                 // 시험 ID
    private String examName;             // 시험 이름
    private Integer grade;               // 학년
    private String studentName;          // 학생 이름
    private String studentPhone;         // 학생 전화번호
    private LocalDateTime submittedAt;   // 제출일시
    private Integer totalScore;          // 총점
    private String qrCodeUrl;            // QR 코드 URL
}
