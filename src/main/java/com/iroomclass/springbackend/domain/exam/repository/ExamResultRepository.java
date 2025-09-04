package com.iroomclass.springbackend.domain.exam.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamResult;

/**
 * 시험 결과 Repository
 * 
 * AI 채점 결과 및 재채점 데이터 관리
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

    /**
     * 특정 시험과 학생의 최신 채점 결과 조회
     * 재채점이 있을 경우 가장 높은 버전을 반환
     * 
     * @param examId 시험 ID  
     * @param studentId 학생 ID
     * @return 최신 채점 결과
     */
    @Query("""
        SELECT er FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.exam.id = :examId 
        AND es.student.id = :studentId 
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.exam.id = :examId 
            AND es2.student.id = :studentId
        )
        """)
    Optional<ExamResult> findLatestResultByExamIdAndStudentId(
        @Param("examId") UUID examId, 
        @Param("studentId") Long studentId);

    /**
     * 특정 시험과 학생의 상세 답안 조회 (문제별 결과 포함)
     * 
     * @param examId 시험 ID
     * @param studentId 학생 ID  
     * @return 상세 답안 데이터
     */
    @EntityGraph(attributePaths = {
        "questionResults", 
        "questionResults.question",
        "questionResults.question.unit",
        "questionResults.question.unit.subcategory",
        "questionResults.question.unit.subcategory.category",
        "examSubmission",
        "examSubmission.student",
        "examSubmission.exam",
        "examSheet"
    })
    @Query("""
        SELECT er FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.exam.id = :examId 
        AND es.student.id = :studentId 
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.exam.id = :examId 
            AND es2.student.id = :studentId
        )
        """)
    Optional<ExamResult> findDetailedResultByExamIdAndStudentId(
        @Param("examId") UUID examId, 
        @Param("studentId") Long studentId);

    /**
     * 특정 시험의 모든 학생 채점 결과 조회 (최신 버전)
     * 
     * @param examId 시험 ID
     * @param pageable 페이징 정보
     * @return 채점 결과 목록
     */
    @Query("""
        SELECT er FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.exam.id = :examId 
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.id = es.id
        )
        ORDER BY es.student.name
        """)
    Page<ExamResult> findLatestResultsByExamId(@Param("examId") UUID examId, Pageable pageable);

    /**
     * 특정 시험의 채점 완료된 결과만 조회
     * 
     * @param examId 시험 ID
     * @return 채점 완료 결과 목록
     */
    @Query("""
        SELECT er FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.exam.id = :examId 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.id = es.id
        )
        ORDER BY er.totalScore DESC, es.student.name
        """)
    List<ExamResult> findCompletedResultsByExamId(@Param("examId") UUID examId);

    /**
     * 재채점이 필요한 결과 조회 (신뢰도가 낮거나 부분점수)
     * 
     * @param examId 시험 ID
     * @return 재채점 대상 결과 목록  
     */
    @Query("""
        SELECT DISTINCT er FROM ExamResult er 
        JOIN er.examSubmission es
        JOIN er.questionResults qr
        WHERE es.exam.id = :examId 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.id = es.id
        )
        AND (
            (qr.confidenceScore IS NOT NULL AND qr.confidenceScore < 0.8)
            OR (qr.score > 0 AND qr.score < qr.question.points)
        )
        ORDER BY es.student.name
        """)
    List<ExamResult> findResultsNeedingReview(@Param("examId") UUID examId);

    /**
     * 특정 학생의 모든 채점 결과 조회 (최신 버전)
     * 
     * @param studentId 학생 ID
     * @param pageable 페이징 정보
     * @return 채점 결과 목록
     */
    @Query("""
        SELECT er FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.student.id = :studentId 
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.id = es.id
        )
        ORDER BY es.exam.createdAt DESC
        """)
    Page<ExamResult> findLatestResultsByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * 특정 제출에 대한 모든 채점 버전 조회 (재채점 이력)
     * 
     * @param submissionId 제출 ID
     * @return 채점 버전 목록
     */
    @Query("""
        SELECT er FROM ExamResult er 
        WHERE er.examSubmission.id = :submissionId 
        ORDER BY er.version DESC
        """)
    List<ExamResult> findAllVersionsBySubmissionId(@Param("submissionId") UUID submissionId);

    /**
     * 특정 시험의 채점 통계 조회
     * 
     * @param examId 시험 ID  
     * @return 채점 통계 (완료/미완료 개수)
     */
    @Query("""
        SELECT 
            COUNT(CASE WHEN er.status = 'COMPLETED' THEN 1 END) as completed,
            COUNT(CASE WHEN er.status != 'COMPLETED' THEN 1 END) as pending,
            AVG(CASE WHEN er.status = 'COMPLETED' THEN er.totalScore END) as averageScore,
            MAX(CASE WHEN er.status = 'COMPLETED' THEN er.totalScore END) as maxScore,
            MIN(CASE WHEN er.status = 'COMPLETED' THEN er.totalScore END) as minScore
        FROM ExamResult er 
        JOIN er.examSubmission es 
        WHERE es.exam.id = :examId 
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            JOIN er2.examSubmission es2 
            WHERE es2.id = es.id
        )
        """)
    ExamGradingStats findGradingStatsByExamId(@Param("examId") UUID examId);

    /**
     * 학년별 학생 평균 점수 조회 (성적 분포도용)
     * 각 학생의 모든 시험 평균 점수를 계산
     * 
     * @param grade 학년
     * @return 학생별 평균 점수 목록
     */
    @Query("""
        SELECT AVG(er.totalScore) as averageScore
        FROM ExamResult er 
        JOIN er.examSubmission es 
        JOIN es.student s
        JOIN es.exam e
        WHERE e.grade = :grade 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission = er.examSubmission
        )
        GROUP BY s.id
        ORDER BY averageScore DESC
        """)
    List<Double> findStudentAverageScoresByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 성적 분포 통계 조회
     * 
     * @param grade 학년
     * @return 성적 분포 통계
     */
    @Query("""
        SELECT 
            CASE 
                WHEN subquery.avgScore < 40 THEN '0-39'
                WHEN subquery.avgScore < 60 THEN '40-59'
                WHEN subquery.avgScore < 70 THEN '60-69'
                WHEN subquery.avgScore < 80 THEN '70-79'
                WHEN subquery.avgScore < 90 THEN '80-89'
                ELSE '90-100'
            END as scoreRange,
            COUNT(subquery.studentId) as studentCount
        FROM (
            SELECT 
                s.id as studentId,
                AVG(er.totalScore) as avgScore
            FROM ExamResult er 
            JOIN er.examSubmission es 
            JOIN es.student s
            JOIN es.exam e
            WHERE e.grade = :grade 
            AND er.status = 'COMPLETED'
            AND er.version = (
                SELECT MAX(er2.version) 
                FROM ExamResult er2 
                WHERE er2.examSubmission = er.examSubmission
            )
            GROUP BY s.id
        ) subquery
        GROUP BY 
            CASE 
                WHEN subquery.avgScore < 40 THEN '0-39'
                WHEN subquery.avgScore < 60 THEN '40-59'
                WHEN subquery.avgScore < 70 THEN '60-69'
                WHEN subquery.avgScore < 80 THEN '70-79'
                WHEN subquery.avgScore < 90 THEN '80-89'
                ELSE '90-100'
            END
        """)
    List<ScoreRangeDistribution> findScoreDistributionByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 전체 성적 통계 조회
     * 
     * @param grade 학년
     * @return 전체 성적 통계
     */
    @Query(value = """
        SELECT 
            COUNT(DISTINCT subquery.student_id) as totalStudentCount,
            AVG(subquery.avg_score) as overallAverage,
            MAX(subquery.avg_score) as maxScore,
            MIN(subquery.avg_score) as minScore
        FROM (
            SELECT 
                s.id as student_id,
                AVG(er.total_score) as avg_score
            FROM exam_result er 
            JOIN exam_submission es ON er.submission_id = es.id
            JOIN student s ON es.student_id = s.id
            JOIN exam e ON es.exam_id = e.id
            WHERE e.grade = :grade 
            AND er.status = 'COMPLETED'
            AND er.version = (
                SELECT MAX(er2.version) 
                FROM exam_result er2 
                WHERE er2.submission_id = er.submission_id
            )
            GROUP BY s.id
        ) subquery
        """, nativeQuery = true)
    ScoreStatistics findScoreStatisticsByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 시험 결과 개수 조회 (분포도 검증용)
     * 
     * @param grade 학년
     * @return 결과 개수
     */
    @Query("""
        SELECT COUNT(DISTINCT s.id)
        FROM ExamResult er 
        JOIN er.examSubmission es 
        JOIN es.student s
        JOIN es.exam e
        WHERE e.grade = :grade 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission = er.examSubmission
        )
        """)
    Long countStudentsWithResultsByGrade(@Param("grade") Integer grade);

    /**
     * 채점 통계 투영 인터페이스
     */
    interface ExamGradingStats {
        Long getCompleted();
        Long getPending(); 
        Double getAverageScore();
        Integer getMaxScore();
        Integer getMinScore();
    }

    /**
     * 점수 구간별 분포 투영 인터페이스
     */
    interface ScoreRangeDistribution {
        String getScoreRange();
        Long getStudentCount();
    }

    /**
     * 전체 성적 통계 투영 인터페이스
     */
    interface ScoreStatistics {
        Long getTotalStudentCount();
        Double getOverallAverage();
        Double getMaxScore();
        Double getMinScore();
    }

    /**
     * 학년별 최근 시험들의 평균 점수 조회 (간단 버전)
     * 
     * @param grade 학년
     * @return 시험별 평균 점수 통계
     */
    @Query("""
        SELECT 
            e.id as examId,
            e.examName as examName,
            e.createdAt as createdAt,
            COUNT(DISTINCT er.examSubmission.student.id) as participantCount,
            AVG(er.totalScore) as averageScore,
            MAX(er.totalScore) as maxScore,
            MIN(er.totalScore) as minScore
        FROM ExamResult er 
        JOIN er.examSubmission es 
        JOIN es.exam e
        WHERE e.grade = :grade 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission = er.examSubmission
        )
        GROUP BY e.id, e.examName, e.createdAt
        ORDER BY e.createdAt DESC
        """)
    List<ExamAverageStatistics> findExamAverageStatisticsByGrade(@Param("grade") Integer grade);

    /**
     * 특정 시험의 문제 수 조회
     * 
     * @param examId 시험 ID
     * @return 문제 수
     */
    @Query("""
        SELECT COUNT(esq.id)
        FROM Exam e
        JOIN e.examSheet.questions esq
        WHERE e.id = :examId
        """)
    Long countQuestionsByExamId(@Param("examId") UUID examId);

    /**
     * 학년별 시험 개수 조회
     * 
     * @param grade 학년
     * @return 전체 시험 개수
     */
    @Query("""
        SELECT COUNT(DISTINCT e.id)
        FROM Exam e
        WHERE e.grade = :grade
        """)
    Long countExamsByGrade(@Param("grade") Integer grade);

    /**
     * 시험별 평균 통계 투영 인터페이스
     */
    interface ExamAverageStatistics {
        UUID getExamId();
        String getExamName();
        LocalDateTime getCreatedAt();
        Long getParticipantCount();
        Double getAverageScore();
        Double getMaxScore();
        Double getMinScore();
    }

    // ========== 단원별 오답률 통계 관련 메서드 ==========

    /**
     * 학년별 단원별 오답률 통계 조회
     * 
     * @param grade 학년
     * @return 단원별 오답률 통계 목록
     */
    @Query("""
        SELECT 
            u.id as unitId,
            u.unitName as unitName,
            uc.categoryName as categoryName,
            us.subcategoryName as subcategoryName,
            COUNT(DISTINCT q.id) as questionCount,
            COUNT(erq.id) as submissionCount,
            SUM(CASE WHEN erq.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswerCount
        FROM Unit u
        LEFT JOIN UnitCategory uc ON u.subcategory.category.id = uc.id
        LEFT JOIN UnitSubcategory us ON u.subcategory.id = us.id
        LEFT JOIN Question q ON q.unit.id = u.id
        LEFT JOIN ExamResultQuestion erq ON erq.question.id = q.id
        LEFT JOIN ExamResult er ON erq.examResult.id = er.id
        LEFT JOIN ExamSubmission es ON er.examSubmission.id = es.id
        LEFT JOIN Exam e ON es.exam.id = e.id
        WHERE u.grade = :grade 
        AND (er.id IS NULL OR (
            er.status = 'COMPLETED'
            AND er.version = (
                SELECT MAX(er2.version) 
                FROM ExamResult er2 
                WHERE er2.examSubmission = er.examSubmission
            )
        ))
        GROUP BY u.id, u.unitName, uc.categoryName, us.subcategoryName
        ORDER BY 
            CASE 
                WHEN COUNT(erq.id) = 0 THEN 0.0
                ELSE (SUM(CASE WHEN erq.isCorrect = false THEN 1 ELSE 0 END) * 100.0 / COUNT(erq.id))
            END DESC
        """)
    List<UnitWrongAnswerStatistics> findUnitWrongAnswerStatisticsByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 전체 문제 수 조회
     * 
     * @param grade 학년
     * @return 전체 문제 수
     */
    @Query("""
        SELECT COUNT(DISTINCT q.id)
        FROM Question q
        JOIN q.unit u
        WHERE u.grade = :grade
        """)
    Long countTotalQuestionsByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 전체 문제 제출 수 조회 (오답률 계산용)
     * 
     * @param grade 학년
     * @return 전체 제출 수
     */
    @Query("""
        SELECT COUNT(erq.id)
        FROM ExamResultQuestion erq
        JOIN erq.examResult er
        JOIN er.examSubmission es
        JOIN es.exam e
        JOIN erq.question q
        JOIN q.unit u
        WHERE u.grade = :grade 
        AND er.status = 'COMPLETED'
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission = er.examSubmission
        )
        """)
    Long countTotalSubmissionsByGrade(@Param("grade") Integer grade);

    /**
     * 학년별 전체 오답 수 조회
     * 
     * @param grade 학년
     * @return 전체 오답 수
     */
    @Query("""
        SELECT COUNT(erq.id)
        FROM ExamResultQuestion erq
        JOIN erq.examResult er
        JOIN er.examSubmission es
        JOIN es.exam e
        JOIN erq.question q
        JOIN q.unit u
        WHERE u.grade = :grade 
        AND er.status = 'COMPLETED'
        AND erq.isCorrect = false
        AND er.version = (
            SELECT MAX(er2.version) 
            FROM ExamResult er2 
            WHERE er2.examSubmission = er.examSubmission
        )
        """)
    Long countTotalWrongAnswersByGrade(@Param("grade") Integer grade);

    /**
     * 단원별 오답률 통계 투영 인터페이스
     */
    interface UnitWrongAnswerStatistics {
        UUID getUnitId();
        String getUnitName();
        String getCategoryName();
        String getSubcategoryName();
        Long getQuestionCount();
        Long getSubmissionCount();
        Long getWrongAnswerCount();
    }
}