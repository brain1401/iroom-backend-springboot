package com.iroomclass.springbackend.domain.exam.service;

import com.iroomclass.springbackend.domain.exam.dto.ExamDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository.ExamSubmissionStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 시험 관련 비즈니스 로직 처리 서비스
 * 
 * <p>시험 조회, 시험 제출 현황 통계, 시험 관리 기능을 제공합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExamService {
    
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    
    /**
     * 시험 ID로 상세 정보 조회
     * 
     * @param examId 시험 식별자
     * @return 시험 상세 정보 DTO
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamDto findById(UUID examId) {
        log.info("시험 조회 시작: examId={}", examId);
        
        Exam exam = examRepository.findByIdWithExamSheet(examId)
            .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));
        
        log.info("시험 조회 완료: examId={}, examName={}", examId, exam.getExamName());
        return ExamDto.fromWithExamSheet(exam);
    }
    
    /**
     * 시험별 제출 현황 상세 조회
     * 
     * @param examId 시험 식별자
     * @return 시험 제출 현황 상세 정보
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamSubmissionStatusDto getExamSubmissionStatus(UUID examId) {
        log.info("시험 제출 현황 조회 시작: examId={}", examId);
        
        // 시험 정보 조회
        Exam exam = examRepository.findByIdWithExamSheet(examId)
            .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));
        
        // 제출 통계 조회
        Long submissionCount = examSubmissionRepository.countByExamId(examId);
        
        // 해당 학년의 전체 학생 수 조회 (예상 제출 수)
        Long totalStudentsInGrade = examSubmissionRepository.countStudentsByGrade(exam.getGrade());
        
        // 시간별 제출 통계 조회
        List<ExamSubmissionRepository.HourlySubmissionStats> hourlyStats = 
            examSubmissionRepository.findHourlySubmissionStats(examId);
        
        // 최근 제출자 목록 (최대 5명)
        List<com.iroomclass.springbackend.domain.exam.entity.ExamSubmission> recentSubmissionEntities = 
            examSubmissionRepository.findByExamIdWithStudent(examId);
        List<ExamSubmissionStatusDto.RecentSubmission> recentSubmissions = recentSubmissionEntities.stream()
            .limit(5)
            .map(ExamSubmissionStatusDto.RecentSubmission::from)
            .collect(Collectors.toList());
        
        log.info("시험 제출 현황 조회 완료: examId={}, submissionCount={}, totalExpected={}", 
                examId, submissionCount, totalStudentsInGrade);
        
        return new ExamSubmissionStatusDto(
            ExamSubmissionStatusDto.ExamInfo.from(exam),
            ExamSubmissionStatusDto.SubmissionStats.create(totalStudentsInGrade, submissionCount),
            recentSubmissions,
            hourlyStats.stream()
                .map(ExamSubmissionStatusDto.HourlyStats::from)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 학년별 시험 목록 조회 (페이징 지원)
     * 
     * @param grade 학년 (1, 2, 3)
     * @param pageable 페이징 정보
     * @return 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findByGrade(Integer grade, Pageable pageable) {
        log.info("학년별 시험 목록 조회: grade={}, page={}, size={}", 
                grade, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Exam> examPage = examRepository.findByGradeOrderByCreatedAtDesc(grade, pageable);
        
        log.info("학년별 시험 목록 조회 완료: grade={}, totalElements={}, totalPages={}", 
                grade, examPage.getTotalElements(), examPage.getTotalPages());
        
        return examPage.map(ExamDto::from);
    }
    
    /**
     * 모든 시험 목록 조회 (페이징 지원)
     * 
     * @param pageable 페이징 정보
     * @return 전체 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findAll(Pageable pageable) {
        log.info("전체 시험 목록 조회: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Exam> examPage = examRepository.findAllOrderByCreatedAtDesc(pageable);
        
        log.info("전체 시험 목록 조회 완료: totalElements={}, totalPages={}", 
                examPage.getTotalElements(), examPage.getTotalPages());
        
        return examPage.map(ExamDto::from);
    }
    
    /**
     * 여러 시험의 제출 현황 통계 조회
     * 
     * @param examIds 시험 식별자 목록
     * @return 시험별 제출 통계 맵 (examId -> submissionCount)
     */
    public Map<UUID, Long> getSubmissionCountsByExamIds(List<UUID> examIds) {
        log.info("여러 시험 제출 통계 조회: examCount={}", examIds.size());
        
        List<ExamSubmissionStats> stats = examSubmissionRepository.countByExamIds(examIds);
        
        Map<UUID, Long> result = stats.stream()
            .collect(Collectors.toMap(
                ExamSubmissionStats::getExamId,
                ExamSubmissionStats::getSubmissionCount
            ));
        
        log.info("여러 시험 제출 통계 조회 완료: resultCount={}", result.size());
        return result;
    }
    
    /**
     * 시험명으로 검색 (페이징 지원)
     * 
     * @param examName 검색할 시험명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> searchByExamName(String examName, Pageable pageable) {
        log.info("시험명 검색: examName={}, page={}, size={}", 
                examName, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Exam> examPage = examRepository.findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(
                examName, pageable);
        
        log.info("시험명 검색 완료: examName={}, totalElements={}", 
                examName, examPage.getTotalElements());
        
        return examPage.map(ExamDto::from);
    }
    
    /**
     * 학년별 및 시험명으로 복합 검색
     * 
     * @param grade 학년 (1, 2, 3)
     * @param examName 검색할 시험명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> searchByGradeAndExamName(Integer grade, String examName, Pageable pageable) {
        log.info("학년 및 시험명 복합 검색: grade={}, examName={}", grade, examName);
        
        Page<Exam> examPage = examRepository.findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(
                grade, examName, pageable);
        
        log.info("학년 및 시험명 복합 검색 완료: grade={}, examName={}, totalElements={}", 
                grade, examName, examPage.getTotalElements());
        
        return examPage.map(ExamDto::from);
    }
}