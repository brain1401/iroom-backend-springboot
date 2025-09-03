package com.iroomclass.springbackend.domain.student.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.student.dto.response.ExamResultSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 학생 시험 결과 Repository
 * 
 * <p>학생 API에서 사용하는 시험 결과 관련 복잡한 조회를 담당합니다.
 * N+1 문제 방지를 위해 EntityGraph와 fetch join을 활용합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentExamResultRepository extends JpaRepository<ExamResult, UUID> {
    
    /**
     * 학생의 시험 결과 요약 목록 조회 (페이지네이션)
     * 
     * <p>Endpoint 2: 응시한 시험들의 결과
     * - 시험명, 채점 날짜, 맞춘 문제 수, 틀린 문제 수, 총 문항 수
     * - 최신 버전의 ExamResult만 사용 (재채점 고려)
     * - 페이지네이션 지원</p>
     * 
     * @param studentId 학생 ID
     * @param pageable  페이지네이션 정보
     * @return 시험 결과 요약 페이지
     */
    @Query("""
        SELECT new com.iroomclass.springbackend.domain.student.dto.response.ExamResultSummaryDto(
            e.examName,
            er.gradedAt,
            CAST(SUM(CASE WHEN erq.isCorrect = true THEN 1 ELSE 0 END) AS long),
            CAST(SUM(CASE WHEN erq.isCorrect = false THEN 1 ELSE 0 END) AS long),
            CAST(COUNT(erq.id) AS long)
        )
        FROM ExamResult er
        JOIN er.examSubmission es
        JOIN es.exam e
        JOIN er.questionResults erq
        WHERE es.student.id = :studentId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        GROUP BY e.id, e.examName, er.gradedAt, er.id
        ORDER BY er.gradedAt DESC
        """)
    Page<ExamResultSummaryDto> findExamResultsSummaryByStudentId(@Param("studentId") Long studentId, Pageable pageable);
    
    /**
     * 특정 시험의 상세 결과 조회 (N+1 방지)
     * 
     * <p>Endpoint 3: 상세 시험 결과 조회
     * - ExamResult와 관련된 모든 데이터를 fetch join으로 한 번에 조회
     * - questionResults, question, unit 등을 포함
     * - 최신 버전의 결과만 조회</p>
     * 
     * @param studentId 학생 ID
     * @param examId    시험 ID
     * @return 상세 시험 결과 (최신 버전)
     */
    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSubmission es
        JOIN FETCH es.exam e
        JOIN FETCH er.questionResults erq
        JOIN FETCH erq.question q
        JOIN FETCH q.unit u
        WHERE es.student.id = :studentId 
        AND es.exam.id = :examId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        """)
    Optional<ExamResult> findDetailedExamResultByStudentIdAndExamId(
        @Param("studentId") Long studentId, 
        @Param("examId") UUID examId);
    
    /**
     * 특정 학생과 시험의 특정 문제 결과 조회
     * 
     * <p>Endpoint 4: 문제 full 정보 조회
     * - 문제 번호로 특정 문제의 상세 정보 조회
     * - Question과 관련된 모든 정보 포함</p>
     * 
     * @param studentId     학생 ID
     * @param examId        시험 ID
     * @param questionOrder 문제 번호 (1부터 시작)
     * @return 문제별 결과와 문제 상세 정보
     */
    @Query("""
        SELECT erq FROM ExamResultQuestion erq
        JOIN FETCH erq.examResult er
        JOIN FETCH er.examSubmission es
        JOIN FETCH erq.question q
        JOIN FETCH q.unit u
        JOIN es.exam e
        JOIN e.examSheet esh
        JOIN esh.questions esq
        WHERE es.student.id = :studentId
        AND es.exam.id = :examId
        AND esq.question.id = erq.question.id
        AND esq.seqNo = :questionOrder
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        """)
    Optional<com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion> 
    findQuestionResultByStudentIdAndExamIdAndQuestionOrder(
        @Param("studentId") Long studentId,
        @Param("examId") UUID examId, 
        @Param("questionOrder") Integer questionOrder);
    
    /**
     * 학생의 최신 시험 결과 조회 (학년 정보용)
     * 
     * <p>학생 정보 조회 시 가장 최근 응시한 시험의 학년을 가져오기 위해 사용</p>
     * 
     * @param studentId 학생 ID
     * @return 가장 최근 시험의 학년 정보
     */
    @Query("""
        SELECT e.grade 
        FROM ExamResult er
        JOIN er.examSubmission es
        JOIN es.exam e
        WHERE es.student.id = :studentId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        ORDER BY er.gradedAt DESC 
        LIMIT 1
        """)
    Integer findLatestGradeByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 학생의 총 응시 시험 수 조회
     * 
     * <p>중복 제거하여 실제 응시한 시험 수만 계산 (재채점은 제외)</p>
     * 
     * @param studentId 학생 ID
     * @return 총 응시 시험 수
     */
    @Query("""
        SELECT COUNT(DISTINCT er.examSubmission.id) 
        FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.student.id = :studentId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        """)
    long countDistinctExamsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 특정 시험 결과의 문제 수 통계 조회
     * 
     * <p>상세 시험 결과에서 객관식/주관식 문제 수를 계산하기 위해 사용</p>
     * 
     * @param studentId 학생 ID
     * @param examId    시험 ID
     * @return 문제 유형별 통계 (객관식 수, 주관식 수, 총 문제 수)
     */
    @Query("""
        SELECT COUNT(CASE WHEN q.questionType = 'MULTIPLE_CHOICE' THEN 1 END) as objectiveCount,
               COUNT(CASE WHEN q.questionType = 'SUBJECTIVE' THEN 1 END) as subjectiveCount,
               COUNT(q.id) as totalCount
        FROM ExamResult er
        JOIN er.examSubmission es
        JOIN er.questionResults erq
        JOIN erq.question q
        WHERE es.student.id = :studentId
        AND es.exam.id = :examId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        """)
    Object[] findQuestionTypeStatsByStudentIdAndExamId(
        @Param("studentId") Long studentId, 
        @Param("examId") UUID examId);
        
    /**
     * 학생의 평균 점수 조회
     * 
     * <p>학생이 응시한 모든 시험의 평균 점수를 계산합니다.
     * 재채점된 경우 최신 버전의 결과만 사용합니다.</p>
     * 
     * @param studentId 학생 ID
     * @return 평균 점수 (응시한 시험이 없으면 null)
     */
    @Query("""
        SELECT AVG(er.totalScore) 
        FROM ExamResult er
        JOIN er.examSubmission es
        WHERE es.student.id = :studentId
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission.id = er.examSubmission.id
        )
        """)
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);
    
    /**
     * EntityGraph를 사용한 ExamResult 조회 (성능 최적화)
     * 
     * <p>복잡한 fetch join 대신 EntityGraph를 사용하여 
     * 필요한 연관 엔티티들을 한 번에 조회합니다.</p>
     * 
     * @param id ExamResult ID
     * @return ExamResult with loaded associations
     */
    @EntityGraph(attributePaths = {
        "examSubmission", 
        "examSubmission.student", 
        "examSubmission.exam", 
        "questionResults", 
        "questionResults.question",
        "questionResults.question.unit"
    })
    Optional<ExamResult> findWithAllAssociationsById(UUID id);
}