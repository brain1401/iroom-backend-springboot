package com.iroomclass.springbackend.domain.user.exam.repository;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 시험 제출 Repository
 * 
 * 시험 제출 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    
    /**
     * 시험별 제출 목록 조회
     * 
     * 사용처: 시험 관리에서 제출 현황 확인
     * 예시: "1학년 중간고사" → 제출한 학생들 목록 (이름, 전화번호, 제출일시)
     * 
     * @param exam 시험
     * @return 해당 시험의 제출 목록 (최신순)
     */
    List<ExamSubmission> findByExamOrderBySubmittedAtDesc(Exam exam);
    
    /**
     * 시험 ID로 제출 목록 조회
     * 
     * 사용처: 시험 관리에서 제출 현황 확인 (ID로 조회)
     * 예시: 시험 ID로 해당 시험의 제출 학생들 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 목록 (최신순)
     */
    List<ExamSubmission> findByExamIdOrderBySubmittedAtDesc(Long examId);
    
    /**
     * 특정 시험의 제출 학생 수 조회
     * 
     * 사용처: 제출 현황에서 제출한 학생 수 계산
     * 예시: "1학년 중간고사" → 제출한 학생 수만 빠르게 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 학생 수
     */
    long countByExamId(Long examId);
    
    /**
     * 학생 이름과 전화번호로 제출 조회
     * 
     * 사용처: 학생이 시험 점수 확인 시
     * 예시: "김철수" + "010-1234-5678" → 해당 학생의 제출 기록 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록
     */
    List<ExamSubmission> findByStudentNameAndStudentPhone(String studentName, String studentPhone);
    
    /**
     * 학생 이름과 전화번호로 제출 조회 (최신순)
     * 
     * 사용처: 학생 로그인 및 결과 조회
     * 예시: "김철수" + "010-1234-5678" → 해당 학생의 제출 기록 조회 (최신순)
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록 (최신순)
     */
    List<ExamSubmission> findByStudentNameAndStudentPhoneOrderBySubmittedAtDesc(String studentName, String studentPhone);
    
    /**
     * 학생 이름과 전화번호로 제출 수 조회
     * 
     * 사용처: 학생 로그인 시 존재 여부 확인
     * 예시: "김철수" + "010-1234-5678" → 해당 학생의 제출 기록 수 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 기록 수
     */
    long countByStudentNameAndStudentPhone(String studentName, String studentPhone);
    
    /**
     * 특정 시험에서 학생 제출 여부 확인
     * 
     * 사용처: 중복 제출 방지
     * 예시: "김철수"가 "1학년 중간고사"에 이미 제출했는지 확인
     * 
     * @param examId 시험 ID
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 제출 여부 (true: 제출됨, false: 제출 안됨)
     */
    boolean existsByExamIdAndStudentNameAndStudentPhone(Long examId, String studentName, String studentPhone);
    
    /**
     * 학년별 시험 제출 조회
     * 
     * 사용처: 대시보드에서 학년별 성적 분포도 조회
     * 예시: "중1" 선택 → 1학년 시험 제출 기록들 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험 제출 목록
     */
    List<ExamSubmission> findByExamGrade(Integer grade);
    
    /**
     * 시험 ID로 제출 목록 조회 (순서 무관)
     * 
     * 사용처: 통계에서 시험별 평균 점수 계산
     * 예시: 시험 ID로 해당 시험의 모든 제출 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 목록
     */
    List<ExamSubmission> findByExamId(Long examId);
}
