package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 시험지-문제 Repository
 * 
 * 시험지에 포함된 문제들 관리를 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamSheetQuestionRepository extends JpaRepository<ExamSheetQuestion, UUID> {

    /**
     * 시험지별 문제 목록 조회 (순서대로)
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지의 문제 목록 (문제 순서대로)
     */
    List<ExamSheetQuestion> findByExamSheetIdOrderBySeqNo(UUID examSheetId);

    /**
     * 시험지의 총 문제 수 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 총 문제 수
     */
    @Query("SELECT COUNT(esq) FROM ExamSheetQuestion esq WHERE esq.examSheet.id = :examSheetId")
    Long countByExamSheetId(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험지의 객관식 문제 수 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 객관식 문제 수
     */
    @Query("SELECT COUNT(esq) FROM ExamSheetQuestion esq " +
           "WHERE esq.examSheet.id = :examSheetId " +
           "AND esq.question.questionType = 'MULTIPLE_CHOICE'")
    Long countMultipleChoiceByExamSheetId(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험지의 주관식 문제 수 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 주관식 문제 수
     */
    @Query("SELECT COUNT(esq) FROM ExamSheetQuestion esq " +
           "WHERE esq.examSheet.id = :examSheetId " +
           "AND esq.question.questionType = 'SUBJECTIVE'")
    Long countSubjectiveByExamSheetId(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험지의 총 배점 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 총 배점
     */
    @Query("SELECT COALESCE(SUM(esq.points), 0) FROM ExamSheetQuestion esq " +
           "WHERE esq.examSheet.id = :examSheetId")
    Integer sumPointsByExamSheetId(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험지의 문제와 단원 정보 함께 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지의 문제 목록 (문제와 단원 정보 포함)
     */
    @Query("SELECT esq FROM ExamSheetQuestion esq " +
           "JOIN FETCH esq.question q " +
           "JOIN FETCH q.unit u " +
           "WHERE esq.examSheet.id = :examSheetId " +
           "ORDER BY esq.seqNo")
    List<ExamSheetQuestion> findByExamSheetIdWithQuestionsAndUnits(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험지에서 문제들 삭제
     * 
     * @param examSheetId 시험지 ID
     */
    void deleteByExamSheetId(UUID examSheetId);

    /**
     * 특정 문제가 시험지에 포함되어 있는지 확인
     * 
     * @param examSheetId 시험지 ID
     * @param questionId 문제 ID
     * @return 포함되어 있으면 true
     */
    boolean existsByExamSheetIdAndQuestionId(UUID examSheetId, UUID questionId);

    /**
     * 시험지에서 특정 순번의 문제 조회
     * 
     * @param examSheetId 시험지 ID
     * @param seqNo 문제 순번
     * @return 해당 순번의 문제
     */
    ExamSheetQuestion findByExamSheetIdAndSeqNo(UUID examSheetId, Integer seqNo);

    /**
     * 여러 시험지의 총점 배치 조회
     * 
     * @param examSheetIds 시험지 ID 목록
     * @return 시험지별 총점 정보
     */
    @Query("SELECT esq.examSheet.id as examSheetId, COALESCE(SUM(esq.points), 0) as totalPoints " +
           "FROM ExamSheetQuestion esq " +
           "WHERE esq.examSheet.id IN :examSheetIds " +
           "GROUP BY esq.examSheet.id")
    List<ExamSheetPointsStats> sumPointsByExamSheetIds(@Param("examSheetIds") List<UUID> examSheetIds);

    /**
     * 시험지별 총점 통계를 위한 Projection 인터페이스
     */
    interface ExamSheetPointsStats {
        UUID getExamSheetId();
        Integer getTotalPoints();
    }
}