package com.iroomclass.springbackend.domain.admin.submission.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자용 시험 제출 목록 응답 DTO
 * 
 * 관리자가 시험별 제출 현황을 조회할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자용 시험 제출 목록 응답")
public class ExamSubmissionListResponse {
    
    @Schema(description = "시험 ID", example = "1")
    private Long examId;
    
    @Schema(description = "시험명", example = "1학년 중간고사")
    private String examName;
    
    @Schema(description = "학년", example = "1학년")
    private String grade;
    
    @Schema(description = "제출 목록")
    private List<SubmissionInfo> submissions;
    
    @Schema(description = "총 제출 수", example = "25")
    private int totalCount;
    
    /**
     * 제출 정보 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "제출 정보")
    public static class SubmissionInfo {
        
        @Schema(description = "제출 ID", example = "1")
        private Long submissionId;
        
        @Schema(description = "학생 이름", example = "김철수")
        private String studentName;
        
        @Schema(description = "학생 전화번호", example = "010-1234-5678")
        private String studentPhone;
        
        @Schema(description = "제출일시", example = "2024-06-01T12:34:56")
        private LocalDateTime submittedAt;
        
        @Schema(description = "총점", example = "85", nullable = true)
        private Integer totalScore;
    }
}
