package com.iroomclass.springbackend.domain.admin.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraft;

/**
 * 시험 Repository
 * 
 * 시험 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    /**
     * 시험지 초안별 시험 조회
     * 
     * 사용처: 시험지 초안에서 실제 시험 발행 시
     * 예시: "1학년 중간고사" 시험지 초안 → 실제 시험 정보 조회
     * 
     * @param examDraft 시험지 초안
     * @return 해당 시험지 초안에서 발행된 시험 목록
     */
    List<Exam> findByExamDraft(ExamDraft examDraft);

    /**
     * 학년별 시험 조회
     * 
     * 사용처: 시험 관리에서 학년별 시험 목록 조회
     * 예시: "중1" 선택 → 1학년 시험들만 표시
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험 목록 (최신순)
     */
    List<Exam> findByGradeOrderByCreatedAtDesc(Integer grade);
    
    /**
     * 학년별 시험 조회 (순서 무관)
     * 
     * 사용처: 대시보드에서 학년별 시험 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험 목록
     */
    List<Exam> findByGrade(Integer grade);
    
    /**
     * 전체 시험 조회 (최신순)
     * 
     * 사용처: 시험 관리에서 전체 시험 목록 조회
     * 예시: 모든 학년의 시험을 최신순으로 표시
     * 
     * @return 전체 시험 목록 (최신순)
     */
    List<Exam> findAllByOrderByCreatedAtDesc();
}
