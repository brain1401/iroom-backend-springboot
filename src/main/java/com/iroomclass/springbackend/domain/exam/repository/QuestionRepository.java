package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
     * 단원별 문제 조회
     * 
     * @param unitId 단원 ID
     * @return 해당 단원의 문제 목록
     */
    List<Question> findByUnitId(UUID unitId);

    /**
     * 학년별 문제 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 문제 목록
     */
    @Query("SELECT q FROM Question q JOIN q.unit u WHERE u.grade = :grade")
    List<Question> findByGrade(@Param("grade") Integer grade);

    /**
     * 난이도별 문제 조회
     * 
     * @param difficulty 난이도
     * @return 해당 난이도의 문제 목록
     */
    List<Question> findByDifficulty(Question.Difficulty difficulty);

    /**
     * 문제 유형별 조회
     * 
     * @param questionType 문제 유형
     * @return 해당 유형의 문제 목록
     */
    List<Question> findByQuestionType(Question.QuestionType questionType);

    /**
     * 단원과 난이도로 문제 조회
     * 
     * @param unitId 단원 ID
     * @param difficulty 난이도
     * @return 조건에 맞는 문제 목록
     */
    List<Question> findByUnitIdAndDifficulty(UUID unitId, Question.Difficulty difficulty);

    /**
     * 단원과 문제 유형으로 조회
     * 
     * @param unitId 단원 ID
     * @param questionType 문제 유형
     * @return 조건에 맞는 문제 목록
     */
    List<Question> findByUnitIdAndQuestionType(UUID unitId, Question.QuestionType questionType);

    /**
     * 학년과 문제 유형으로 조회
     * 
     * @param grade 학년
     * @param questionType 문제 유형
     * @return 조건에 맞는 문제 목록
     */
    @Query("SELECT q FROM Question q JOIN q.unit u " +
           "WHERE u.grade = :grade AND q.questionType = :questionType")
    List<Question> findByGradeAndQuestionType(@Param("grade") Integer grade, 
                                            @Param("questionType") Question.QuestionType questionType);

    /**
     * 복합 조건으로 문제 조회 (페이징)
     * 
     * @param unitId 단원 ID (선택적)
     * @param difficulty 난이도 (선택적)
     * @param questionType 문제 유형 (선택적)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 문제 목록 (페이징)
     */
    @Query("SELECT q FROM Question q " +
           "WHERE (:unitId IS NULL OR q.unit.id = :unitId) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:questionType IS NULL OR q.questionType = :questionType)")
    Page<Question> findByConditions(@Param("unitId") UUID unitId,
                                  @Param("difficulty") Question.Difficulty difficulty,
                                  @Param("questionType") Question.QuestionType questionType,
                                  Pageable pageable);

    /**
     * 문제와 단원 정보 함께 조회
     * 
     * @param questionId 문제 ID
     * @return 문제 (단원 정보 포함)
     */
    @Query("SELECT q FROM Question q " +
           "JOIN FETCH q.unit u " +
           "JOIN FETCH u.subcategory sc " +
           "JOIN FETCH sc.category c " +
           "WHERE q.id = :questionId")
    Question findByIdWithUnit(@Param("questionId") UUID questionId);

    /**
     * 단원별 문제 수 조회
     * 
     * @param unitId 단원 ID
     * @return 문제 수
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.unit.id = :unitId")
    Long countByUnitId(@Param("unitId") UUID unitId);

    /**
     * 학년별 문제 수 조회
     * 
     * @param grade 학년
     * @return 문제 수
     */
    @Query("SELECT COUNT(q) FROM Question q JOIN q.unit u WHERE u.grade = :grade")
    Long countByGrade(@Param("grade") Integer grade);

    /**
     * 문제 유형별 문제 수 조회
     * 
     * @param questionType 문제 유형
     * @return 문제 수
     */
    Long countByQuestionType(Question.QuestionType questionType);
    
    /**
     * 여러 단원의 문제들을 한 번에 조회 (N+1 문제 방지)
     * 
     * @param unitIds 단원 ID 목록
     * @return 해당 단원들의 모든 문제 목록
     */
    List<Question> findByUnitIdIn(List<UUID> unitIds);
}