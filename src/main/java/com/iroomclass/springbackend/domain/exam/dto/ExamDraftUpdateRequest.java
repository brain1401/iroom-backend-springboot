package com.iroomclass.springbackend.domain.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 초안 수정 요청 DTO
 * 
 * 시험지 초안 문제 교체 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftUpdateRequest {
    
    private int seqNo;  // 교체할 문제 번호
}
