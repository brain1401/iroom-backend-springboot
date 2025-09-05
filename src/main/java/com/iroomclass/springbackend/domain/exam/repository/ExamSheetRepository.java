package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 시험지 데이터 접근을 위한 Repository 인터페이스
 * 
 * <p>시험지 조회, 검색, 페이징 및 통계 관련 데이터 접근 기능을 제공합니다.</p>
 */
@Repository
public interface ExamSheetRepository extends JpaRepository<ExamSheet, UUID> {
    
    /**
     * 시험지 ID로 상세 조회 (문제 목록 포함)
     * 
     * @param examSheetId 시험지 식별자
     * @return 시험지 정보 (문제 목록 포함)
     */
    @Query("SELECT es FROM ExamSheet es LEFT JOIN FETCH es.questions WHERE es.id = :examSheetId")
    Optional<ExamSheet> findByIdWithQuestions(@Param("examSheetId") UUID examSheetId);
    
    /**
     * 시험지 ID로 상세 조회 (문제 목록 및 단원 정보 포함)
     * N+1 문제 방지를 위해 Question -> Unit -> Subcategory -> Category 전체 계층을 한 번에 조회
     * 
     * @param examSheetId 시험지 식별자
     * @return 시험지 정보 (문제 목록 및 단원 정보 포함)
     */
    @Query("SELECT DISTINCT es FROM ExamSheet es " +
           "LEFT JOIN FETCH es.questions esq " +
           "LEFT JOIN FETCH esq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "LEFT JOIN FETCH u.subcategory sc " +
           "LEFT JOIN FETCH sc.category c " +
           "WHERE es.id = :examSheetId")
    Optional<ExamSheet> findByIdWithQuestionsAndUnits(@Param("examSheetId") UUID examSheetId);
    
    /**
     * 시험지 목록을 단원 정보와 함께 효율적으로 조회 (페이징)
     * N+1 문제 방지를 위해 필요한 단원 정보를 한 번에 조회
     * 
     * @param pageable 페이징 정보
     * @return 시험지 목록 (단원 정보 포함)
     */
    @Query(value = "SELECT DISTINCT es FROM ExamSheet es " +
           "LEFT JOIN FETCH es.questions esq " +
           "LEFT JOIN FETCH esq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "LEFT JOIN FETCH u.subcategory sc " +
           "LEFT JOIN FETCH sc.category c",
           countQuery = "SELECT COUNT(DISTINCT es) FROM ExamSheet es")
    Page<ExamSheet> findAllWithQuestionsAndUnits(Pageable pageable);
    
    /**
     * 학년별 시험지 목록을 단원 정보와 함께 효율적으로 조회 (페이징)
     * 
     * @param grade 학년
     * @param pageable 페이징 정보
     * @return 시험지 목록 (단원 정보 포함)
     */
    @Query(value = "SELECT DISTINCT es FROM ExamSheet es " +
           "LEFT JOIN FETCH es.questions esq " +
           "LEFT JOIN FETCH esq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "LEFT JOIN FETCH u.subcategory sc " +
           "LEFT JOIN FETCH sc.category c " +
           "WHERE es.grade = :grade",
           countQuery = "SELECT COUNT(DISTINCT es) FROM ExamSheet es WHERE es.grade = :grade")
    Page<ExamSheet> findByGradeWithQuestionsAndUnits(@Param("grade") Integer grade, Pageable pageable);
    
    /**
     * 시험지명으로 검색하면서 단원 정보와 함께 효율적으로 조회
     * 
     * @param examName 검색할 시험지명
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록 (단원 정보 포함)
     */
    @Query(value = "SELECT DISTINCT es FROM ExamSheet es " +
           "LEFT JOIN FETCH es.questions esq " +
           "LEFT JOIN FETCH esq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "LEFT JOIN FETCH u.subcategory sc " +
           "LEFT JOIN FETCH sc.category c " +
           "WHERE es.examName LIKE %:examName%",
           countQuery = "SELECT COUNT(DISTINCT es) FROM ExamSheet es WHERE es.examName LIKE %:examName%")
    Page<ExamSheet> findByExamNameContainingIgnoreCaseWithQuestionsAndUnits(@Param("examName") String examName, Pageable pageable);
    
    /**
     * 학년 + 시험지명 복합 검색하면서 단원 정보와 함께 효율적으로 조회
     * 
     * @param grade 학년
     * @param examName 검색할 시험지명
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록 (단원 정보 포함)
     */
    @Query(value = "SELECT DISTINCT es FROM ExamSheet es " +
           "LEFT JOIN FETCH es.questions esq " +
           "LEFT JOIN FETCH esq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "LEFT JOIN FETCH u.subcategory sc " +
           "LEFT JOIN FETCH sc.category c " +
           "WHERE es.grade = :grade AND es.examName LIKE %:examName%",
           countQuery = "SELECT COUNT(DISTINCT es) FROM ExamSheet es WHERE es.grade = :grade AND es.examName LIKE %:examName%")
    Page<ExamSheet> findByGradeAndExamNameContainingIgnoreCaseWithQuestionsAndUnits(
            @Param("grade") Integer grade, @Param("examName") String examName, Pageable pageable);
    
    /**
     * 학년별 시험지 목록 조회 (최신 순)
     * 
     * @param grade 학년 (1, 2, 3)
     * @param pageable 페이징 정보
     * @return 시험지 목록
     */
    Page<ExamSheet> findByGradeOrderByCreatedAtDesc(Integer grade, Pageable pageable);
    
    /**
     * 전체 시험지 목록 조회 (최신 순)
     * 
     * @param pageable 페이징 정보
     * @return 시험지 목록
     */
    Page<ExamSheet> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 시험지명으로 검색 (대소문자 무시, 부분 일치)
     * 
     * @param examName 검색할 시험지명
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록
     */
    Page<ExamSheet> findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(String examName, Pageable pageable);
    
    /**
     * 학년 + 시험지명 복합 검색
     * 
     * @param grade 학년
     * @param examName 검색할 시험지명
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록
     */
    Page<ExamSheet> findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(
            Integer grade, String examName, Pageable pageable);
    
    /**
     * 학년별 시험지 개수 조회
     * 
     * @param grade 학년
     * @return 시험지 개수
     */
    Long countByGrade(Integer grade);
    
    /**
     * 특정 기간 내 생성된 시험지 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 시험지 목록
     */
    Page<ExamSheet> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 특정 학년의 특정 기간 내 생성된 시험지 조회
     * 
     * @param grade 학년
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 시험지 목록
     */
    Page<ExamSheet> findByGradeAndCreatedAtBetweenOrderByCreatedAtDesc(
            Integer grade, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 시험지명 존재 여부 확인 (중복 검사용)
     * 
     * @param examName 시험지명
     * @return 존재하면 true
     */
    boolean existsByExamName(String examName);
    
    /**
     * 특정 학년에서 시험지명 존재 여부 확인
     * 
     * @param grade 학년
     * @param examName 시험지명
     * @return 존재하면 true
     */
    boolean existsByGradeAndExamName(Integer grade, String examName);
    
    /**
     * 학년별 시험지 통계 조회
     * 
     * @return 학년별 시험지 개수 통계
     */
    @Query("SELECT es.grade as grade, COUNT(es) as count FROM ExamSheet es GROUP BY es.grade ORDER BY es.grade")
    List<ExamSheetGradeStats> findExamSheetStatsByGrade();
    
    /**
     * 최근 N개월간의 월별 시험지 생성 통계
     * 
     * @param monthsAgo 몇 개월 전부터
     * @return 월별 생성 통계
     */
    @Query("SELECT YEAR(es.createdAt) as year, MONTH(es.createdAt) as month, COUNT(es) as count " +
           "FROM ExamSheet es " +
           "WHERE es.createdAt >= :startDate " +
           "GROUP BY YEAR(es.createdAt), MONTH(es.createdAt) " +
           "ORDER BY YEAR(es.createdAt), MONTH(es.createdAt)")
    List<ExamSheetMonthlyStats> findMonthlyCreationStats(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 문제 개수가 특정 범위에 있는 시험지 조회
     * 문제 개수는 ExamSheetQuestion과의 관계를 통해 계산됩니다.
     * 
     * @param minQuestions 최소 문제 개수
     * @param maxQuestions 최대 문제 개수
     * @param pageable 페이징 정보
     * @return 시험지 목록
     */
    @Query("SELECT es FROM ExamSheet es " +
           "WHERE (SELECT COUNT(esq) FROM ExamSheetQuestion esq WHERE esq.examSheet = es) " +
           "BETWEEN :minQuestions AND :maxQuestions " +
           "ORDER BY es.createdAt DESC")
    Page<ExamSheet> findByQuestionCountBetween(
            @Param("minQuestions") Integer minQuestions, 
            @Param("maxQuestions") Integer maxQuestions, 
            Pageable pageable);
    
    /**
     * 가장 최근에 생성된 시험지 N개 조회
     * 
     * @param limit 조회할 개수
     * @return 최근 시험지 목록
     */
    @Query("SELECT es FROM ExamSheet es ORDER BY es.createdAt DESC")
    List<ExamSheet> findTopNRecent(@Param("limit") int limit);
    
    /**
     * 특정 학년의 가장 최근에 생성된 시험지 N개 조회
     * 
     * @param grade 학년
     * @param limit 조회할 개수
     * @return 최근 시험지 목록
     */
    @Query("SELECT es FROM ExamSheet es WHERE es.grade = :grade ORDER BY es.createdAt DESC")
    List<ExamSheet> findTopNRecentByGrade(@Param("grade") Integer grade, @Param("limit") int limit);
    
    /**
     * 특정 시험지로 생성된 시험 개수 조회 (사용 횟수)
     * 
     * @param examSheetId 시험지 ID
     * @return 시험 개수
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.examSheet.id = :examSheetId")
    Long countExamsByExamSheetId(@Param("examSheetId") UUID examSheetId);
    
    /**
     * 특정 시험지의 가장 최근 사용 시간 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 가장 최근에 생성된 시험의 생성 시간 (없으면 null)
     */
    @Query("SELECT MAX(e.createdAt) FROM Exam e WHERE e.examSheet.id = :examSheetId")
    LocalDateTime findLastUsedAtByExamSheetId(@Param("examSheetId") UUID examSheetId);
    
    /**
     * 학년별 시험지 통계를 위한 Projection 인터페이스
     */
    interface ExamSheetGradeStats {
        Integer getGrade();
        Long getCount();
    }
    
    /**
     * 월별 시험지 생성 통계를 위한 Projection 인터페이스
     */
    interface ExamSheetMonthlyStats {
        Integer getYear();
        Integer getMonth();
        Long getCount();
    }
}