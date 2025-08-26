package com.iroomclass.springbackend.domain.unit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.unit.entity.Unit;

/**
 * 세부단원 Repository
 * 
 * 세부단원 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    
    /**
     * 학년별 세부단원 조회 (ID 순으로 정렬)
     * 
     * 사용처: 시험지 등록 화면에서 학년 선택 후 세부단원 목록 표시
     * 예시: 중1 선택 → "정수의 덧셈", "정수의 뺄셈", "유리수의 덧셈" 등 표시
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 세부단원 목록 (ID 순으로 정렬)
     */
    List<Unit> findByGradeOrderById(Integer grade);
    
    /**
     * 학년별 세부단원 조회 (표시 순서대로 정렬)
     * 
     * 사용처: 시험지 등록 화면에서 학년 선택 후 세부단원 목록 표시
     * 예시: 중1 선택 → "정수의 덧셈", "정수의 뺄셈", "유리수의 덧셈" 등 표시
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 세부단원 목록 (표시 순서대로 정렬)
     */
    List<Unit> findByGradeOrderByDisplayOrder(Integer grade);

    /**
     * 중분류별 세부단원 조회
     * 
     * 사용처: 단원 계층 구조 조회 시 중분류 아래의 세부단원들을 표시
     * 예시: "정수와 유리수" 중분류 선택 → "정수의 덧셈", "정수의 뺄셈" 등 표시
     * 
     * @param subcategoryId 중분류 ID
     * @return 해당 중분류에 속한 세부단원 목록 (표시 순서대로 정렬)
     */
    List<Unit> findBySubcategoryIdOrderByDisplayOrder(Long subcategoryId);
}
