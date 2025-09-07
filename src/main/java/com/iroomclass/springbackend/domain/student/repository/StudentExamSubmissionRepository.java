package com.iroomclass.springbackend.domain.student.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.student.dto.response.RecentSubmissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 학생 시험 제출 Repository
 * 
 * <p>학생 API에서 사용하는 시험 제출 관련 조회를 담당합니다.
 * 페이지네이션 및 복잡한 JOIN 쿼리를 포함합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentExamSubmissionRepository extends JpaRepository<ExamSubmission, UUID> {
    
    /**
     * 학생의 최근 제출한 시험 목록 조회 (페이지네이션)
     * 
     * <p>Endpoint 1: 학생이 최근 제출한 시험
     * - 시험명, 날짜, 내용, 총 문항 수를 포함
     * - 페이지네이션 지원으로 성능 최적화</p>
     * 
     * @param studentId 학생 ID
     * @param pageable  페이지네이션 정보
     * @return 최근 제출 시험 페이지
     */
    @Query("""
        SELECT new com.iroomclass.springbackend.domain.student.dto.response.RecentSubmissionDto(
            e.examName, 
            es.submittedAt, 
            e.content, 
            CAST(COUNT(esq.id) AS long)
        )
        FROM ExamSubmission es
        JOIN es.exam e
        JOIN ExamSheetQuestion esq ON esq.examSheet.id = e.examSheet.id
        WHERE es.student.id = :studentId
        GROUP BY e.id, e.examName, es.submittedAt, e.content
        ORDER BY es.submittedAt DESC
        """)
    Page<RecentSubmissionDto> findRecentSubmissionsByStudentId(@Param("studentId") Long studentId, Pageable pageable);
    
    /**
     * 특정 학생의 시험 제출 수 조회
     * 
     * <p>학생 정보 조회 시 총 응시 시험 수 확인용</p>
     * 
     * @param studentId 학생 ID
     * @return 해당 학생의 총 제출 시험 수
     */
    @Query("SELECT COUNT(es) FROM ExamSubmission es WHERE es.student.id = :studentId")
    long countSubmissionsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 학생의 가장 최근 응시한 시험의 학년 조회
     * 
     * <p>학생 정보에서 학년을 표시하기 위해 사용.
     * Student 엔티티에 grade 필드가 없으므로 최근 시험의 학년을 사용</p>
     * 
     * @param studentId 학생 ID
     * @return 가장 최근 응시 시험의 학년 (없으면 null)
     */
    @Query("""
        SELECT e.grade 
        FROM ExamSubmission es 
        JOIN es.exam e 
        WHERE es.student.id = :studentId 
        ORDER BY es.submittedAt DESC 
        LIMIT 1
        """)
    Integer findLatestGradeByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 특정 시험에 대한 학생의 제출 정보 조회
     * 
     * <p>상세 결과 조회 시 해당 시험에 학생이 제출했는지 확인</p>
     * 
     * @param studentId 학생 ID
     * @param examId    시험 ID
     * @return 제출 정보 (있으면 ExamSubmission, 없으면 empty)
     */
    @Query("""
        SELECT es FROM ExamSubmission es
        WHERE es.student.id = :studentId AND es.exam.id = :examId
        """)
    java.util.Optional<ExamSubmission> findByStudentIdAndExamId(@Param("studentId") Long studentId, @Param("examId") UUID examId);
    
    /**
     * 학생의 시험 이력 조회 (페이지네이션)
     * 
     * <p>학생이 응시한 모든 시험의 정보를 조회합니다.
     * 시험 ID, 시험명, 응시일, 문제 수, 점수를 포함합니다.</p>
     * 
     * @param studentId 학생 ID
     * @param pageable  페이지네이션 정보
     * @return 학생의 시험 이력 페이지
     */
    @Query("""
        SELECT new com.iroomclass.springbackend.domain.student.dto.response.StudentExamHistoryDto(
            e.id,
            e.examName,
            es.submittedAt,
            CAST(COUNT(DISTINCT esq.id) AS int),
            er.totalScore
        )
        FROM ExamSubmission es
        JOIN es.exam e
        JOIN e.examSheet exs
        JOIN ExamSheetQuestion esq ON esq.examSheet.id = exs.id
        LEFT JOIN ExamResult er ON er.examSubmission.id = es.id
        WHERE es.student.id = :studentId
            AND (er.id IS NULL OR er.status = 'COMPLETED' OR er.status = 'REGRADED')
        GROUP BY e.id, e.examName, es.submittedAt, er.totalScore
        ORDER BY es.submittedAt DESC
        """)
    Page<com.iroomclass.springbackend.domain.student.dto.response.StudentExamHistoryDto> findExamHistoryByStudentId(
            @Param("studentId") Long studentId, 
            Pageable pageable);
}