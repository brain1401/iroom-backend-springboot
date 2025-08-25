package com.iroomclass.springbackend.domain.question.repository;

import com.iroomclass.springbackend.domain.question.entity.Question;
import com.iroomclass.springbackend.domain.unit.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 문제 Repository
 * 
 * 문제 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 상세단원별 문제 조회
     * 
     * 사용처: 시험지 등록에서 단원 선택 후 해당 단원의 문제들 조회
     * 예시: "정수의 덧셈" 단원 선택 → 해당 단원의 모든 문제들 표시 (난이도 랜덤)
     * 
     * @param unit 상세단원
     * @return 해당 상세단원의 문제 목록
     */
    List<Question> findByUnit(Unit unit);
}