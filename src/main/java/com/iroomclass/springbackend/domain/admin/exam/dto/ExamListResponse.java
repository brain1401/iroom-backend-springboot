package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 목록 조회 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamListResponse {
    
    /**
     * 학년
     */
    private Integer grade;
    
    /**
     * 시험 목록
     */
    private List<ExamInfo> exams;
    
    /**
     * 총 개수
     */
    private Integer totalCount;
    
    /**
     * 시험 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamInfo {
        
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
}
