package com.iroomclass.springbackend.domain.unit.repository;

import com.iroomclass.springbackend.domain.unit.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 학년별 세부단원 조회
     * 
     * 사용처: 시험지 등록 화면에서 학년 선택 후 세부단원 목록 표시
     * 예시: 중1 선택 → "정수의 덧셈", "정수의 뺄셈", "유리수의 덧셈" 등 표시
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 세부단원 목록
     */
    List<Unit> findByGradeOrderBySeqNo(Integer grade);
}
