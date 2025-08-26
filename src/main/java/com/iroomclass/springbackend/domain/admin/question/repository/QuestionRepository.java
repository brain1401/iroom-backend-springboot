package com.iroomclass.springbackend.domain.admin.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.admin.question.entity.Question;

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
     * 단원별 문제 목록 조회
     * 
     * 사용처: 문제 관리 화면에서 특정 단원의 문제 목록 표시
     * 예시: "정수의 덧셈" 단원 선택 → 해당 단원의 모든 문제 목록 표시
     * 
     * @param unitId 단원 ID
     * @return 해당 단원에 속한 문제 목록
     */
    List<Question> findByUnitId(Long unitId);
    
    /**
     * 단원별 문제 목록 조회 (다중 단원)
     * 
     * 사용처: 시험지 생성 시 특정 단원의 문제들을 랜덤으로 선택
     * 예시: "정수의 덧셈" 단원 선택 → 해당 단원의 30개 문제 중에서 랜덤 선택
     * 
     * @param unitIds 단원 ID 목록
     * @return 해당 단원들에 속한 문제 목록
     */
    List<Question> findByUnitIdIn(List<Long> unitIds);
    
    /**
     * 단원별, 난이도별 문제 목록 조회
     * 
     * 사용처: 문제 관리 화면에서 특정 단원의 특정 난이도 문제 목록 표시
     * 예시: "정수의 덧셈" 단원의 "중" 난이도 문제들만 표시
     * 
     * @param unitId 단원 ID
     * @param difficulty 난이도
     * @return 해당 단원의 특정 난이도 문제 목록
     */
    List<Question> findByUnitIdAndDifficulty(Long unitId, Question.Difficulty difficulty);
    
    /**
     * 단원별 문제 수 카운트
     * 
     * 사용처: 단원별 문제 수 통계, 시험지 생성 시 문항 수 제한 확인
     * 예시: "정수의 덧셈" 단원에 몇 개의 문제가 있는지 확인
     * 
     * @param unitId 단원 ID
     * @return 해당 단원에 속한 문제의 총 개수
     */
    long countByUnitId(Long unitId);
    
    /**
     * 단원별, 난이도별 문제 수 카운트
     * 
     * 사용처: 단원별 난이도별 문제 수 통계
     * 예시: "정수의 덧셈" 단원의 "중" 난이도 문제가 몇 개인지 확인
     * 
     * @param unitId 단원 ID
     * @param difficulty 난이도
     * @return 해당 단원의 특정 난이도 문제 개수
     */
    long countByUnitIdAndDifficulty(Long unitId, Question.Difficulty difficulty);
    
    /**
     * 문제 내용 검색
     * 
     * 사용처: 문제 검색 기능
     * 예시: "정수" 키워드로 검색 → 문제 내용에 "정수"가 포함된 문제들 검색
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 문제 목록
     */
    List<Question> findByStemContaining(String keyword);
    
    /**
     * 다중 단원별 문제 수 카운트
     * 
     * 사용처: 시험지 초안 생성 시 선택된 단원들의 총 문제 수 확인
     * 예시: "정수의 덧셈", "정수의 뺄셈" 단원 선택 → 총 50문제 확인
     * 
     * @param unitIds 단원 ID 목록
     * @return 해당 단원들에 속한 문제의 총 개수
     */
    long countByUnitIdIn(List<Long> unitIds);
}