package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 초안 목록 조회 응답 DTO
 * 
 * 학년별 시험지 초안 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftListResponse {
    
    private Integer grade;              // 학년 (전체 목록일 경우 null)
    private List<ExamDraftInfo> examDrafts;  // 시험지 초안 목록
    private int totalCount;         // 총 개수
    
    /**
     * 시험지 초안 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamDraftInfo {
        private Long examDraftId;       // 시험지 초안 ID
        private String examName;        // 시험지 이름
        private int grade;              // 학년
        private int totalQuestions;     // 총 문제 개수
        private int selectedUnitCount;  // 선택된 단원 수
    }
}
