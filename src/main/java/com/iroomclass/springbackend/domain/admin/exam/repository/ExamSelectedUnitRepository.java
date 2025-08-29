package com.iroomclass.springbackend.domain.admin.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSelectedUnit;

import java.util.List;

/**
 * 시험지 선택 단원 Repository
 * 
 * 시험지에 선택된 단원들의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamSelectedUnitRepository extends JpaRepository<ExamSelectedUnit, Long> {
    
    /**
     * 시험지별 선택된 단원 조회
     * 
     * 사용처: 시험지 등록에서 선택한 단원들 확인
     * 예시: "1학년 중간고사" 시험지 → "정수의 덧셈", "정수의 뺄셈" 단원들 표시
     * 
     * @param examSheet 시험지
     * @return 해당 시험지에 선택된 단원 목록
     */
    List<ExamSelectedUnit> findByExamSheet(ExamSheet examSheet);
    
    /**
     * 시험지 ID로 선택된 단원 조회
     * 
     * 사용처: 시험지 상세 보기에서 선택된 단원들 확인
     * 예시: 시험지 ID로 해당 시험지에 포함된 단원들 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지에 선택된 단원 목록
     */
    List<ExamSelectedUnit> findByExamSheetId(Long examSheetId);
    
    /**
     * 시험지 ID로 선택된 단원 수 카운트
     * 
     * 사용처: 시험지 목록에서 선택된 단원 수 표시
     * 예시: "1학년 중간고사" → "3개 단원 선택" 표시
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지에 선택된 단원 수
     */
    long countByExamSheetId(Long examSheetId);
}
