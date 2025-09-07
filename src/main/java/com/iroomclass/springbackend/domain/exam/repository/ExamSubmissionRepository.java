package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 시험 제출 Repository
 * 
 * <p>시험 제출 데이터 조회 및 관리를 위한 Repository입니다.
 * 제출 현황 통계 및 제출자 정보 조회 기능을 제공합니다.</p>
 */
@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, UUID> {
    
    /**
     * 특정 시험의 총 제출 수 조회
     * 
     * @param examId 시험 ID
     * @return 제출 수
     */
    long countByExamId(UUID examId);
    
    /**
     * 특정 시험의 제출자 목록 조회
     * 
     * @param examId 시험 ID
     * @return 제출자 목록 (학생 정보 포함)
     */
    @Query("SELECT es FROM ExamSubmission es LEFT JOIN FETCH es.student WHERE es.exam.id = :examId ORDER BY es.submittedAt DESC")
    List<ExamSubmission> findByExamIdWithStudent(@Param("examId") UUID examId);
    
    /**
     * 특정 시험의 제출자 목록 조회 (기본 정보만)
     * 
     * @param examId 시험 ID
     * @return 제출자 목록
     */
    @Query("SELECT es FROM ExamSubmission es WHERE es.exam.id = :examId ORDER BY es.submittedAt DESC")
    List<ExamSubmission> findByExamIdOrderBySubmittedAtDesc(@Param("examId") UUID examId);
    
    /**
     * 학생별 특정 시험 제출 여부 확인
     * 
     * @param examId 시험 ID
     * @param studentId 학생 ID
     * @return 제출 기록 존재 여부
     */
    boolean existsByExamIdAndStudentId(UUID examId, Long studentId);
    
    /**
     * 특정 학생의 시험 제출 기록 조회
     * 
     * @param studentId 학생 ID
     * @return 제출 기록 목록
     */
    @Query("SELECT es FROM ExamSubmission es LEFT JOIN FETCH es.exam WHERE es.student.id = :studentId ORDER BY es.submittedAt DESC")
    List<ExamSubmission> findByStudentIdWithExam(@Param("studentId") Long studentId);
    
    /**
     * 기간별 제출 현황 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 제출 기록 목록
     */
    @Query("SELECT es FROM ExamSubmission es WHERE es.submittedAt BETWEEN :startDate AND :endDate ORDER BY es.submittedAt DESC")
    List<ExamSubmission> findBySubmittedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 학년별 전체 제출 현황 통계
     * 
     * @param grade 학년
     * @return 제출 수
     */
    @Query("SELECT COUNT(es) FROM ExamSubmission es WHERE es.exam.grade = :grade")
    long countByExamGrade(@Param("grade") Integer grade);
    
    /**
     * 시험별 제출 현황 통계 (여러 시험 한 번에)
     * 
     * @param examIds 시험 ID 목록
     * @return 시험 ID별 제출 수 통계
     */
    @Query("SELECT es.exam.id as examId, COUNT(es) as submissionCount " +
           "FROM ExamSubmission es " +
           "WHERE es.exam.id IN :examIds " +
           "GROUP BY es.exam.id")
    List<ExamSubmissionStats> countByExamIds(@Param("examIds") List<UUID> examIds);
    
    /**
     * 학년별 최근 제출 현황 조회
     * 
     * @param grade 학년
     * @param limit 조회할 개수
     * @return 최근 제출 기록 목록
     */
    @Query("SELECT es FROM ExamSubmission es " +
           "WHERE es.exam.grade = :grade " +
           "ORDER BY es.submittedAt DESC " +
           "LIMIT :limit")
    List<ExamSubmission> findRecentSubmissionsByGrade(@Param("grade") Integer grade, @Param("limit") int limit);
    
    /**
     * 특정 시험의 시간별 제출 분포 조회
     * 
     * @param examId 시험 ID
     * @return 시간별 제출 분포
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', es.submittedAt, '%Y-%m-%d %H:00:00') as hourGroup, COUNT(es) as count " +
           "FROM ExamSubmission es " +
           "WHERE es.exam.id = :examId " +
           "GROUP BY FUNCTION('DATE_FORMAT', es.submittedAt, '%Y-%m-%d %H:00:00') " +
           "ORDER BY hourGroup")
    List<HourlySubmissionStats> findHourlySubmissionStats(@Param("examId") UUID examId);
    
    /**
     * 특정 시험과 학생의 제출 기록 조회
     * 
     * @param examId 시험 ID
     * @param studentId 학생 ID
     * @return 제출 기록 (없으면 empty)
     */
    @Query("SELECT es FROM ExamSubmission es WHERE es.exam.id = :examId AND es.student.id = :studentId")
    Optional<ExamSubmission> findByExamIdAndStudentId(@Param("examId") UUID examId, @Param("studentId") Long studentId);
    
    /**
     * 시험 ID별 제출 수 통계를 위한 Projection 인터페이스
     */
    interface ExamSubmissionStats {
        UUID getExamId();
        Long getSubmissionCount();
    }
    
    /**
     * 시간별 제출 통계를 위한 Projection 인터페이스 (ExamService 호환용)
     */
    interface HourlySubmissionStats {
        String getHourGroup();
        Long getCount();
    }
    
    /**
     * 시간별 제출 통계를 위한 Projection 인터페이스 (기존)
     */
    interface SubmissionHourlyStats {
        String getHourGroup();
        Long getCount();
    }
    
    /**
     * 학년별 전체 학생 수 조회 (해당 학년에 시험을 본 학생들 기준)
     * 
     * @param grade 학년
     * @return 학생 수
     */
    @Query("SELECT COUNT(DISTINCT es.student.id) FROM ExamSubmission es WHERE es.exam.grade = :grade")
    Long countStudentsByGrade(@Param("grade") Integer grade);
    
    /**
     * 시험별 제출 통계 조회 (시험 메타데이터 포함)
     * 카르테시안 곱 문제 해결을 위해 서브쿼리로 분리
     * 
     * @param examIds 시험 ID 목록
     * @return 시험별 상세 통계
     */
    @Query("SELECT e.id as examId, e.examName as examName, e.createdAt as createdAt, " +
           "(SELECT COUNT(es2) FROM ExamSubmission es2 WHERE es2.exam.id = e.id) as submissionCount, " +
           "(SELECT COUNT(esq) FROM ExamSheetQuestion esq WHERE esq.examSheet.id = e.examSheet.id) as questionCount " +
           "FROM Exam e " +
           "WHERE e.id IN :examIds")
    List<ExamDetailStats> findExamDetailStats(@Param("examIds") List<UUID> examIds);
    
    /**
     * 특정 시험의 응시자 목록을 페이징하여 조회
     * 
     * @param examId 시험 ID
     * @param pageable 페이징 정보
     * @return 응시자 페이지
     */
    @Query("SELECT es FROM ExamSubmission es " +
           "LEFT JOIN FETCH es.student s " +
           "LEFT JOIN FETCH es.exam e " +
           "WHERE es.exam.id = :examId " +
           "ORDER BY es.submittedAt DESC")
    Page<ExamSubmission> findByExamIdWithStudentAndExam(@Param("examId") UUID examId, Pageable pageable);
    
    /**
     * 특정 시험의 응시자 목록을 페이징하여 조회 (최적화 버전)
     * 
     * @param examId 시험 ID
     * @param pageable 페이징 정보
     * @return 응시자 페이지
     */
    @Query(value = "SELECT es FROM ExamSubmission es " +
                   "LEFT JOIN FETCH es.student " +
                   "WHERE es.exam.id = :examId",
           countQuery = "SELECT COUNT(es) FROM ExamSubmission es WHERE es.exam.id = :examId")
    Page<ExamSubmission> findAttendeesByExamId(@Param("examId") UUID examId, Pageable pageable);
    
    /**
     * 시험별 상세 통계를 위한 Projection 인터페이스
     */
    interface ExamDetailStats {
        UUID getExamId();
        String getExamName();
        LocalDateTime getCreatedAt();
        Long getSubmissionCount();
        Long getQuestionCount();
    }
}