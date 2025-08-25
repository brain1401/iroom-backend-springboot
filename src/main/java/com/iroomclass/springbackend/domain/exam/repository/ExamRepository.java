package com.iroomclass.springbackend.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.exam.entity.Exam;
import java.util.List;
import com.iroomclass.springbackend.domain.exam.entity.ExamDraft;

/**
 * 시험 Repository
 * 
 * 시험 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    /**
     * 시험지 초안별 시험 조회
     * 
     * 사용처: 시험지 초안에서 실제 시험 발행 시
     * 예시: "1학년 중간고사" 시험지 초안 → 실제 시험 정보 조회
     * 
     * @param examDraft 시험지 초안
     * @return 해당 시험지 초안에서 발행된 시험 목록
     */
    List<Exam> findByExamDraft(ExamDraft examDraft);

    /**
     * 학년별 시험 조회
     * 
     * 사용처: 시험 관리에서 학년별 시험 목록 조회
     * 예시: "중1" 선택 → 1학년 시험들만 표시
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험 목록 (최신순)
     */
    List<Exam> findByGradeOrderByCreatedAtDesc(Integer grade);

    /**
     * QR코드 URL로 시험 조회
     * 
     * 사용처: 학생이 시험지 종이의 QR코드를 찍어서 시험 제출 화면으로 이동
     * 예시: QR코드 스캔 → 해당 시험의 제출 화면으로 이동
     * 
     * @param qrCodeUrl QR코드 URL
     * @return 해당 QR코드의 시험 정보
     */
    Exam findByQrCodeUrl(String qrCodeUrl);
}
