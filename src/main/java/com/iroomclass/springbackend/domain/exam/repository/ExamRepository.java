package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitProjection;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitBasicProjection;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitNameProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 시험 Repository
 * 
 * <p>
 * 시험 데이터 조회 및 관리를 위한 Repository입니다.
 * 학년별 필터링, 페이징, 검색 기능을 제공합니다.
 * </p>
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    /**
     * 시험 ID로 시험 정보 조회 (시험지 정보 포함)
     * 
     * @param examId 시험 ID
     * @return 시험 정보 (시험지 포함)
     */
    @Query("SELECT e FROM Exam e LEFT JOIN FETCH e.examSheet WHERE e.id = :examId")
    Optional<Exam> findByIdWithExamSheet(@Param("examId") UUID examId);

    /**
     * 학년별 시험 목록 조회 (페이징)
     * 
     * @param grade    학년
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade ORDER BY e.createdAt DESC")
    Page<Exam> findByGradeOrderByCreatedAtDesc(@Param("grade") Integer grade, Pageable pageable);

    /**
     * 전체 시험 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e ORDER BY e.createdAt DESC")
    Page<Exam> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 학년별 시험 목록 조회 (검색 포함)
     * 
     * @param grade    학년
     * @param keyword  검색 키워드 (시험명)
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade AND e.examName LIKE %:keyword% ORDER BY e.createdAt DESC")
    Page<Exam> findByGradeAndExamNameContaining(@Param("grade") Integer grade,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 전체 시험 목록 조회 (검색 포함)
     * 
     * @param keyword  검색 키워드 (시험명)
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.examName LIKE %:keyword% ORDER BY e.createdAt DESC")
    Page<Exam> findByExamNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 학년별 최근 시험 목록 조회 (제한된 개수)
     * 
     * @param grade 학년
     * @param limit 조회할 개수
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade ORDER BY e.createdAt DESC LIMIT :limit")
    List<Exam> findRecentExamsByGrade(@Param("grade") Integer grade, @Param("limit") int limit);

    /**
     * 학년별 시험 개수 조회
     * 
     * @param grade 학년
     * @return 시험 개수
     */
    long countByGrade(Integer grade);

    /**
     * 특정 시험지로 생성된 시험 목록 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.examSheet.id = :examSheetId ORDER BY e.createdAt DESC")
    List<Exam> findByExamSheetId(@Param("examSheetId") UUID examSheetId);

    /**
     * 시험 기본 정보만 조회 (가벼운 조회용)
     * 
     * @param examId 시험 ID
     * @return 시험 기본 정보
     */
    @Query("SELECT e.id, e.examName, e.grade, e.content, e.createdAt FROM Exam e WHERE e.id = :examId")
    Optional<Exam> findBasicInfoById(@Param("examId") UUID examId);

    /**
     * 대소문자 구분 없는 시험명 검색
     * 
     * @param examName 검색할 시험명
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE LOWER(e.examName) LIKE LOWER(CONCAT('%', :examName, '%')) ORDER BY e.createdAt DESC")
    Page<Exam> findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("examName") String examName,
            Pageable pageable);

    /**
     * 학년별 대소문자 구분 없는 시험명 검색
     * 
     * @param grade    학년
     * @param examName 검색할 시험명
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade AND LOWER(e.examName) LIKE LOWER(CONCAT('%', :examName, '%')) ORDER BY e.createdAt DESC")
    Page<Exam> findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("grade") Integer grade,
            @Param("examName") String examName, Pageable pageable);

    // ========================================
    // 단원 정보 포함 최적화 쿼리 메서드
    // ========================================

    /**
     * 시험 ID로 단원 정보를 포함한 전체 시험 정보 조회 (단일 쿼리 최적화)
     * 
     * <p>@EntityGraph를 사용하여 N+1 문제 없이 한 번의 쿼리로 
     * Exam→ExamSheet→ExamSheetQuestion→Question→Unit 전체 경로를 조회합니다.</p>
     * 
     * @param examId 시험 ID
     * @return 단원 정보가 포함된 시험 엔티티
     */
    /**
     * 시험 ID로 단원 정보를 포함한 전체 시험 정보 조회 (JOIN FETCH로 N+1 문제 해결)
     * 
     * <p>명시적인 JOIN FETCH를 사용하여 FetchType.LAZY 설정을 무시하고
     * 단일 쿼리로 Exam→ExamSheet→ExamSheetQuestion→Question→Unit→UnitSubcategory→UnitCategory 
     * 전체 경로를 한 번에 로드합니다.</p>
     * 
     * <p>성능 최적화:
     * - DISTINCT를 사용하여 중복 제거
     * - LEFT JOIN FETCH로 모든 관련 엔티티 즉시 로딩
     * - 단일 SQL 쿼리로 전체 그래프 로드</p>
     * 
     * @param examId 시험 ID
     * @return 단원 정보가 포함된 시험 엔티티
     */
    @Query("""
        SELECT DISTINCT e
        FROM Exam e
        LEFT JOIN FETCH e.examSheet es
        LEFT JOIN FETCH es.questions esq
        LEFT JOIN FETCH esq.question q
        LEFT JOIN FETCH q.unit u
        LEFT JOIN FETCH u.subcategory us
        LEFT JOIN FETCH us.category uc
        WHERE e.id = :examId
        """)
    Optional<Exam> findByIdWithUnits(@Param("examId") UUID examId);

    /**
     * 다중 시험 ID로 단원 정보를 포함한 시험 정보 배치 조회
     * 
     * <p>여러 시험을 한 번에 조회할 때 사용하며, @EntityGraph로 최적화됩니다.
     * 대시보드나 목록 페이지에서 여러 시험의 단원 정보를 동시에 표시할 때 유용합니다.</p>
     * 
     * @param examIds 시험 ID 목록
     * @return 단원 정보가 포함된 시험 엔티티 목록
     */
    @Query("""
        SELECT DISTINCT e
        FROM Exam e
        LEFT JOIN FETCH e.examSheet es
        LEFT JOIN FETCH es.questions esq
        LEFT JOIN FETCH esq.question q
        LEFT JOIN FETCH q.unit u
        LEFT JOIN FETCH u.subcategory us
        LEFT JOIN FETCH us.category uc
        WHERE e.id IN :examIds
        ORDER BY e.createdAt DESC
        """)
    List<Exam> findByIdInWithUnits(@Param("examIds") List<UUID> examIds);

    /**
     * 학년별 시험 목록을 단원 정보와 함께 조회 (페이징)
     * 
     * <p>성능상의 이유로 기본 정보만 조회하며, 필요시 개별적으로 단원 정보를 조회하는 것을 권장합니다.
     * 대량 데이터 조회 시 메모리 사용량을 고려해야 합니다.</p>
     * 
     * @param grade 학년
     * @param pageable 페이징 정보
     * @return 시험 목록 (기본 정보만 포함)
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade ORDER BY e.createdAt DESC")
    Page<Exam> findByGradeForUnitFetching(@Param("grade") Integer grade, Pageable pageable);

    // ========================================
    // Projection 기반 단원 정보 조회
    // ========================================

    /**
     * 특정 시험에 포함된 모든 단원의 전체 정보를 Projection으로 조회
     * 
     * <p>계층 구조(대분류>중분류>단원) 정보를 포함한 완전한 단원 정보를 제공합니다.
     * 시험 상세 페이지에서 단원 정보를 표시할 때 사용합니다.</p>
     * 
     * @param examId 시험 ID
     * @return 단원 정보 Projection 목록 (계층 구조 포함)
     */
    @Query("""
        SELECT DISTINCT u AS id, u.unitName AS unitName, u.unitCode AS unitCode,
               u.grade AS grade, u.description AS description, u.displayOrder AS displayOrder,
               u.subcategory AS subcategory
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.id = :examId
        ORDER BY u.subcategory.category.displayOrder, u.subcategory.displayOrder, u.displayOrder
        """)
    List<UnitProjection> findUnitsByExamId(@Param("examId") UUID examId);

    /**
     * 특정 시험에 포함된 모든 단원의 기본 정보를 Projection으로 조회
     * 
     * <p>계층 구조 정보 없이 단원 자체 정보만 조회하여 더 빠른 성능을 제공합니다.
     * 간단한 단원 목록 표시나 통계 계산에 사용합니다.</p>
     * 
     * @param examId 시험 ID
     * @return 단원 기본 정보 Projection 목록
     */
    @Query("""
        SELECT DISTINCT u.id AS id, u.unitName AS unitName, u.unitCode AS unitCode,
               u.grade AS grade, u.displayOrder AS displayOrder
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.id = :examId
        ORDER BY u.displayOrder
        """)
    List<UnitBasicProjection> findBasicUnitsByExamId(@Param("examId") UUID examId);

    /**
     * 특정 시험에 포함된 단원 이름만 조회 (간소화된 버전)
     * 
     * <p>단원의 기본 정보(ID, 이름)만 조회하여 최고 성능을 제공합니다.
     * 단순한 단원 목록 표시에 사용합니다.</p>
     * 
     * @param examId 시험 ID
     * @return 단원 이름 정보 Projection 목록
     */
    @Query("""
        SELECT DISTINCT u.id AS id, u.unitName AS unitName
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.id = :examId
        ORDER BY u.unitName
        """)
    List<UnitNameProjection> findUnitNamesByExamId(@Param("examId") UUID examId);

    /**
     * 특정 시험의 단원별 문제 수를 조회
     * 
     * <p>각 단원별로 포함된 문제 수와 총 배점을 계산합니다.
     * 시험 분석이나 통계 데이터 생성에 사용됩니다.</p>
     * 
     * @param examId 시험 ID
     * @return 단원별 문제 수 및 배점 정보 [unitId, unitName, questionCount, totalPoints]
     */
    @Query("""
        SELECT u.id, u.unitName, COUNT(q.id), COALESCE(SUM(eq.points), 0)
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.id = :examId
        GROUP BY u.id, u.unitName
        ORDER BY u.displayOrder
        """)
    List<Object[]> findUnitQuestionCountsByExamId(@Param("examId") UUID examId);

    /**
     * 다중 시험의 단원별 문제 수를 배치 조회
     * 
     * <p>여러 시험에 대한 단원별 통계를 한 번에 조회합니다.
     * 대시보드나 비교 분석에 사용됩니다.</p>
     * 
     * @param examIds 시험 ID 목록
     * @return 시험별 단원별 문제 수 정보 [examId, unitId, unitName, questionCount, totalPoints]
     */
    @Query("""
        SELECT e.id, u.id, u.unitName, COUNT(q.id), COALESCE(SUM(eq.points), 0)
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.id IN :examIds
        GROUP BY e.id, u.id, u.unitName
        ORDER BY e.createdAt DESC, u.displayOrder
        """)
    List<Object[]> findUnitQuestionCountsByExamIds(@Param("examIds") List<UUID> examIds);

    /**
     * 특정 학년의 모든 시험에서 사용된 단원 목록 조회
     * 
     * <p>학년별 커리큘럼 분석이나 단원별 출제 빈도 분석에 사용됩니다.
     * 중복 제거하여 유니크한 단원만 반환합니다.</p>
     * 
     * @param grade 학년
     * @return 해당 학년 시험에 사용된 단원 목록
     */
    @Query("""
        SELECT DISTINCT u.id AS id, u.unitName AS unitName, u.unitCode AS unitCode,
               u.grade AS grade, u.displayOrder AS displayOrder
        FROM Exam e 
        JOIN e.examSheet es 
        JOIN es.questions eq 
        JOIN eq.question q 
        JOIN q.unit u
        WHERE e.grade = :grade
        ORDER BY u.displayOrder
        """)
    List<UnitBasicProjection> findDistinctUnitsByGrade(@Param("grade") Integer grade);
}