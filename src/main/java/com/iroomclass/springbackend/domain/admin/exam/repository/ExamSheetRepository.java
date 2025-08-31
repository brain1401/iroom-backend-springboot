package com.iroomclass.springbackend.domain.admin.exam.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 시험지 Repository
 * 
 * 시험지 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamSheetRepository extends JpaRepository<ExamSheet, UUID> {
    
    /**
     * 학년별 시험지 조회
     * 
     * 사용처: 시험지 목록 화면에서 학년별 필터링
     * 예시: "중1" 선택 → 1학년 시험지들만 표시 (최신순 정렬)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 목록 (최신순)
     */
    List<ExamSheet> findByGradeOrderByCreatedAtDesc(Integer grade);
    
    /**
     * 전체 시험지 조회 (최신순)
     * 
     * 사용처: 시험지 관리에서 전체 목록 조회
     * 예시: 모든 학년의 시험지를 최신순으로 표시
     * 
     * @return 전체 시험지 목록 (최신순)
     */
    List<ExamSheet> findAllByOrderByCreatedAtDesc();
    
    /**
     * 시험지 목록 필터링 조회
     * 
     * 시험지명, 단원명, 생성일시 범위로 필터링하여 조회합니다.
     * 모든 필터 조건은 선택사항이며, NULL일 경우 해당 조건은 무시됩니다.
     * 
     * @param examName 시험지명 (부분 일치, 선택사항)
     * @param unitName 단원명 (부분 일치, 선택사항)
     * @param createdAtFrom 생성일시 시작점 (선택사항)
     * @param createdAtTo 생성일시 종료점 (선택사항)
     * @param pageable 페이징 정보
     * @return 필터링된 시험지 목록 (생성일시 내림차순)
     */
    @Query("""
        SELECT DISTINCT es FROM ExamSheet es 
        LEFT JOIN ExamSheetSelectedUnit esu ON es.id = esu.examSheet.id
        LEFT JOIN esu.unit u
        WHERE (:examName IS NULL OR es.examName LIKE %:examName%)
        AND (:unitName IS NULL OR u.unitName LIKE %:unitName%)
        AND (:createdAtFrom IS NULL OR es.createdAt >= :createdAtFrom)
        AND (:createdAtTo IS NULL OR es.createdAt <= :createdAtTo)
        ORDER BY es.createdAt DESC
        """)
    Page<ExamSheet> findWithFilters(
        @Param("examName") String examName,
        @Param("unitName") String unitName, 
        @Param("createdAtFrom") LocalDateTime createdAtFrom,
        @Param("createdAtTo") LocalDateTime createdAtTo,
        Pageable pageable
    );
    
    /**
     * 학년별 시험지 필터링 조회
     * 
     * 특정 학년의 시험지를 추가 필터 조건과 함께 조회합니다.
     * 
     * @param grade 학년 (1, 2, 3)
     * @param examName 시험지명 (부분 일치, 선택사항)
     * @param unitName 단원명 (부분 일치, 선택사항)
     * @param createdAtFrom 생성일시 시작점 (선택사항)
     * @param createdAtTo 생성일시 종료점 (선택사항)
     * @param pageable 페이징 정보
     * @return 필터링된 시험지 목록 (생성일시 내림차순)
     */
    @Query("""
        SELECT DISTINCT es FROM ExamSheet es 
        LEFT JOIN ExamSheetSelectedUnit esu ON es.id = esu.examSheet.id
        LEFT JOIN esu.unit u
        WHERE es.grade = :grade
        AND (:examName IS NULL OR es.examName LIKE %:examName%)
        AND (:unitName IS NULL OR u.unitName LIKE %:unitName%)
        AND (:createdAtFrom IS NULL OR es.createdAt >= :createdAtFrom)
        AND (:createdAtTo IS NULL OR es.createdAt <= :createdAtTo)
        ORDER BY es.createdAt DESC
        """)
    Page<ExamSheet> findByGradeWithFilters(
        @Param("grade") Integer grade,
        @Param("examName") String examName,
        @Param("unitName") String unitName, 
        @Param("createdAtFrom") LocalDateTime createdAtFrom,
        @Param("createdAtTo") LocalDateTime createdAtTo,
        Pageable pageable
    );
}