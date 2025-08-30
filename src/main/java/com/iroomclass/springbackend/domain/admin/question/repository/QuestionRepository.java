package com.iroomclass.springbackend.domain.admin.question.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    
    /**
     * 단원별 문제 목록 조회
     * 
     * 사용처: 문제 관리 화면에서 특정 단원의 문제 목록 표시
     * 예시: "정수의 덧셈" 단원 선택 → 해당 단원의 모든 문제 목록 표시
     * 
     * @param unitId 단원 ID
     * @return 해당 단원에 속한 문제 목록
     */
    List<Question> findByUnitId(UUID unitId);
    
    /**
     * 단원별 문제 목록 조회 (다중 단원)
     * 
     * 사용처: 시험지 생성 시 특정 단원의 문제들을 랜덤으로 선택
     * 예시: "정수의 덧셈" 단원 선택 → 해당 단원의 30개 문제 중에서 랜덤 선택
     * 
     * @param unitIds 단원 ID 목록
     * @return 해당 단원들에 속한 문제 목록
     */
    List<Question> findByUnitIdIn(List<UUID> unitIds);
    
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
    List<Question> findByUnitIdAndDifficulty(UUID unitId, Question.Difficulty difficulty);
    
    /**
     * 단원별 문제 수 카운트
     * 
     * 사용처: 단원별 문제 수 통계, 시험지 생성 시 문항 수 제한 확인
     * 예시: "정수의 덧셈" 단원에 몇 개의 문제가 있는지 확인
     * 
     * @param unitId 단원 ID
     * @return 해당 단원에 속한 문제의 총 개수
     */
    long countByUnitId(UUID unitId);
    
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
    long countByUnitIdAndDifficulty(UUID unitId, Question.Difficulty difficulty);
    
    /**
     * 문제 내용 검색
     * 
     * 사용처: 문제 검색 기능
     * 예시: "정수" 키워드로 검색 → 문제 내용에 "정수"가 포함된 문제들 검색
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 문제 목록
     */
    List<Question> findByQuestionTextContaining(String keyword);
    
    /**
     * 다중 단원별 문제 수 카운트
     * 
     * 사용처: 시험지 초안 생성 시 선택된 단원들의 총 문제 수 확인
     * 예시: "정수의 덧셈", "정수의 뺄셈" 단원 선택 → 총 50문제 확인
     * 
     * @param unitIds 단원 ID 목록
     * @return 해당 단원들에 속한 문제의 총 개수
     */
    long countByUnitIdIn(List<UUID> unitIds);
    
    /**
     * 단원별 문제 목록 조회 (페이징 지원)
     * 
     * 사용처: 문제 직접 선택 시스템에서 특정 단원의 문제 목록을 페이징으로 표시
     * 예시: "정수의 덧셈" 단원 선택 → 20개씩 페이징하여 문제 목록 표시
     * 
     * @param unitId 단원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 문제 목록
     */
    Page<Question> findByUnitId(UUID unitId, Pageable pageable);
    
    /**
     * 단원별, 난이도별 문제 목록 조회 (난이도 문자열 기반)
     * 
     * @param unitId 단원 ID
     * @param difficultyName 난이도 이름 ("하", "중", "상")
     * @return 해당 단원의 특정 난이도 문제 목록
     */
    @Query("SELECT q FROM Question q WHERE q.unit.id = :unitId AND CAST(q.difficulty AS string) = :difficultyName")
    List<Question> findByUnitIdAndDifficultyName(@Param("unitId") UUID unitId, @Param("difficultyName") String difficultyName);
    
    /**
     * 단원별, 문제 유형별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @param questionType 문제 유형
     * @return 해당 단원의 특정 유형 문제 목록
     */
    List<Question> findByUnitIdAndQuestionType(UUID unitId, Question.QuestionType questionType);
    
    /**
     * 단원별, 난이도별, 문제 유형별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @param difficultyName 난이도 이름 ("하", "중", "상")
     * @param questionType 문제 유형
     * @return 해당 조건의 문제 목록
     */
    @Query("SELECT q FROM Question q WHERE q.unit.id = :unitId AND CAST(q.difficulty AS string) = :difficultyName AND q.questionType = :questionType")
    List<Question> findByUnitIdAndDifficultyNameAndQuestionType(@Param("unitId") UUID unitId, @Param("difficultyName") String difficultyName, @Param("questionType") Question.QuestionType questionType);
    
    /**
     * 학년별 문제 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 모든 문제 목록
     */
    List<Question> findByUnit_Grade(Integer grade);
    
    /**
     * 학년별, 난이도별 문제 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @param difficultyName 난이도 이름 ("하", "중", "상")
     * @return 해당 학년의 특정 난이도 문제 목록
     */
    @Query("SELECT q FROM Question q WHERE q.unit.grade = :grade AND CAST(q.difficulty AS string) = :difficultyName")
    List<Question> findByUnit_GradeAndDifficultyName(@Param("grade") Integer grade, @Param("difficultyName") String difficultyName);
    
    /**
     * 학년별, 문제 유형별 문제 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @param questionType 문제 유형
     * @return 해당 학년의 특정 유형 문제 목록
     */
    List<Question> findByUnit_GradeAndQuestionType(Integer grade, Question.QuestionType questionType);
    
    /**
     * 학년별, 난이도별, 문제 유형별 문제 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @param difficultyName 난이도 이름 ("하", "중", "상")
     * @param questionType 문제 유형
     * @return 해당 조건의 문제 목록
     */
    @Query("SELECT q FROM Question q WHERE q.unit.grade = :grade AND CAST(q.difficulty AS string) = :difficultyName AND q.questionType = :questionType")
    List<Question> findByUnit_GradeAndDifficultyNameAndQuestionType(@Param("grade") Integer grade, @Param("difficultyName") String difficultyName, @Param("questionType") Question.QuestionType questionType);
}