package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * <p>시험 데이터 조회 및 관리를 위한 Repository입니다.
 * 학년별 필터링, 페이징, 검색 기능을 제공합니다.</p>
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
     * @param grade 학년
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
     * @param grade 학년
     * @param keyword 검색 키워드 (시험명)
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
     * @param keyword 검색 키워드 (시험명)
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
    Page<Exam> findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("examName") String examName, Pageable pageable);
    
    /**
     * 학년별 대소문자 구분 없는 시험명 검색
     * 
     * @param grade 학년
     * @param examName 검색할 시험명
     * @param pageable 페이징 정보
     * @return 시험 목록
     */
    @Query("SELECT e FROM Exam e WHERE e.grade = :grade AND LOWER(e.examName) LIKE LOWER(CONCAT('%', :examName, '%')) ORDER BY e.createdAt DESC")
    Page<Exam> findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("grade") Integer grade, @Param("examName") String examName, Pageable pageable);
}