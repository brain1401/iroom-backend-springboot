package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 초안 생성 요청 DTO
 * 
 * 시험지 초안 생성 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftCreateRequest {
    
    private String examName;        // 시험지 이름
    private int grade;              // 학년 (1, 2, 3)
    private int totalQuestions;     // 총 문제 개수 (1~30)
    private List<Long> unitIds;     // 선택된 단원 ID 목록
}
