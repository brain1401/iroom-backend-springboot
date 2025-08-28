package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 초안 상세 조회 응답 DTO
 * 
 * 시험지 초안 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftDetailResponse {
    
    private Long examDraftId;       // 시험지 초안 ID
    private String examName;        // 시험지 이름
    private int grade;              // 학년
    private int totalQuestions;     // 총 문제 개수
    private List<UnitInfo> units;   // 선택된 단원 목록
    private List<QuestionInfo> questions;  // 선택된 문제 목록
    
    /**
     * 단원 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInfo {
        private Long unitId;        // 단원 ID
        private String unitName;    // 단원명
    }
    
    /**
     * 문제 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private int seqNo;          // 문제 번호
        private Long questionId;    // 문제 ID
        private Long unitId;        // 단원 ID
        private String unitName;    // 단원명
        private String difficulty;  // 난이도
        private String stem;        // 문제 내용 (HTML)
        private int points;         // 배점
    }
}
