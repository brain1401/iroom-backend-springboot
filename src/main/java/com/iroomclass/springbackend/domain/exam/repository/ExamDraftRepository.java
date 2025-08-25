package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 시험지 초안 Repository
 * 
 * 시험지 초안 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamDraftRepository extends JpaRepository<ExamDraft, Long> {
    
    /**
     * 학년별 시험지 초안 조회
     * 
     * 사용처: 시험지 목록 화면에서 학년별 필터링
     * 예시: "중1" 선택 → 1학년 시험지들만 표시 (최신순 정렬)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 초안 목록 (최신순)
     */
    List<ExamDraft> findByGradeOrderByIdDesc(Integer grade);
}
