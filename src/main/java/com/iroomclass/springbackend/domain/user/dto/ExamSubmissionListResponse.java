package com.iroomclass.springbackend.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 제출 목록 조회 응답 DTO
 * 
 * 시험별 제출 현황 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionListResponse {
    
    private Long examId;                    // 시험 ID
    private String examName;                // 시험 이름
    private Integer grade;                  // 학년
    private List<SubmissionInfo> submissions; // 제출 목록
    private int totalCount;                 // 총 제출 개수
    
    /**
     * 제출 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionInfo {
        private Long submissionId;           // 제출 ID
        private String studentName;          // 학생 이름
        private String studentPhone;         // 학생 전화번호
        private LocalDateTime submittedAt;   // 제출일시
        private Integer totalScore;          // 총점
    }
}
