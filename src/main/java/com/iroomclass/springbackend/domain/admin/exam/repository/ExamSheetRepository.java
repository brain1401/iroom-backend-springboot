package com.iroomclass.springbackend.domain.admin.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;

import java.util.List;

/**
 * 시험지 Repository
 * 
 * 시험지 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamSheetRepository extends JpaRepository<ExamSheet, Long> {
    
    /**
     * 학년별 시험지 조회
     * 
     * 사용처: 시험지 목록 화면에서 학년별 필터링
     * 예시: "중1" 선택 → 1학년 시험지들만 표시 (최신순 정렬)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 목록 (최신순)
     */
    List<ExamSheet> findByGradeOrderByIdDesc(Integer grade);
    
    /**
     * 전체 시험지 조회 (최신순)
     * 
     * 사용처: 시험지 관리에서 전체 목록 조회
     * 예시: 모든 학년의 시험지를 최신순으로 표시
     * 
     * @return 전체 시험지 목록 (최신순)
     */
    List<ExamSheet> findAllByOrderByIdDesc();
}