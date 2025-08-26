package com.iroomclass.springbackend.domain.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 초안 생성 응답 DTO
 * 
 * 시험지 초안 생성 완료 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftCreateResponse {
    
    private Long examDraftId;       // 시험지 초안 ID
    private String examName;        // 시험지 이름
    private int grade;              // 학년
    private int totalQuestions;     // 총 문제 개수
    private int selectedUnitCount;  // 선택된 단원 수
}
