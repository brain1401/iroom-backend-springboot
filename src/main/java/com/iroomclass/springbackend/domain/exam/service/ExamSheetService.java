package com.iroomclass.springbackend.domain.exam.service;

import com.iroomclass.springbackend.domain.exam.dto.ExamSheetDto;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 시험지 관련 비즈니스 로직 처리 서비스
 * 
 * <p>시험지 조회, 검색, 통계 및 관리 기능을 제공합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExamSheetService {
    
    private final ExamSheetRepository examSheetRepository;
    
    /**
     * 시험지 ID로 상세 정보 조회
     * 
     * @param examSheetId 시험지 식별자
     * @return 시험지 상세 정보 DTO (문제 목록 포함)
     * @throws RuntimeException 시험지를 찾을 수 없을 때
     */
    public ExamSheetDto findById(UUID examSheetId) {
        log.info("시험지 상세 조회 시작: examSheetId={}", examSheetId);
        
        ExamSheet examSheet = examSheetRepository.findByIdWithQuestions(examSheetId)
            .orElseThrow(() -> new RuntimeException("시험지를 찾을 수 없습니다: " + examSheetId));
        
        log.info("시험지 상세 조회 완료: examSheetId={}, examName={}, questionCount={}", 
                examSheetId, examSheet.getExamName(), examSheet.getTotalQuestions());
        
        return ExamSheetDto.fromWithQuestions(examSheet);
    }
    
    /**
     * 시험지 ID로 기본 정보 조회 (문제 목록 제외)
     * 
     * @param examSheetId 시험지 식별자
     * @return 시험지 기본 정보 DTO
     * @throws RuntimeException 시험지를 찾을 수 없을 때
     */
    public ExamSheetDto findByIdBasic(UUID examSheetId) {
        log.info("시험지 기본 조회 시작: examSheetId={}", examSheetId);
        
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
            .orElseThrow(() -> new RuntimeException("시험지를 찾을 수 없습니다: " + examSheetId));
        
        log.info("시험지 기본 조회 완료: examSheetId={}, examName={}", 
                examSheetId, examSheet.getExamName());
        
        return ExamSheetDto.from(examSheet);
    }
    
    /**
     * 학년별 시험지 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @param pageable 페이징 정보
     * @return 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findByGrade(Integer grade, Pageable pageable) {
        log.info("학년별 시험지 목록 조회: grade={}, page={}, size={}", 
                grade, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamSheet> examSheetPage = examSheetRepository.findByGradeOrderByCreatedAtDesc(grade, pageable);
        
        log.info("학년별 시험지 목록 조회 완료: grade={}, totalElements={}, totalPages={}", 
                grade, examSheetPage.getTotalElements(), examSheetPage.getTotalPages());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 전체 시험지 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 전체 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findAll(Pageable pageable) {
        log.info("전체 시험지 목록 조회: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamSheet> examSheetPage = examSheetRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        log.info("전체 시험지 목록 조회 완료: totalElements={}, totalPages={}", 
                examSheetPage.getTotalElements(), examSheetPage.getTotalPages());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 시험지명으로 검색
     * 
     * @param examName 검색할 시험지명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> searchByExamName(String examName, Pageable pageable) {
        log.info("시험지명 검색: examName={}, page={}, size={}", 
                examName, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamSheet> examSheetPage = examSheetRepository
            .findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(examName, pageable);
        
        log.info("시험지명 검색 완료: examName={}, totalElements={}", 
                examName, examSheetPage.getTotalElements());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 학년 및 시험지명으로 복합 검색
     * 
     * @param grade 학년 (1, 2, 3)
     * @param examName 검색할 시험지명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> searchByGradeAndExamName(Integer grade, String examName, Pageable pageable) {
        log.info("학년 및 시험지명 복합 검색: grade={}, examName={}", grade, examName);
        
        Page<ExamSheet> examSheetPage = examSheetRepository
            .findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(grade, examName, pageable);
        
        log.info("학년 및 시험지명 복합 검색 완료: grade={}, examName={}, totalElements={}", 
                grade, examName, examSheetPage.getTotalElements());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 특정 기간 내 생성된 시험지 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("기간별 시험지 조회: startDate={}, endDate={}", startDate, endDate);
        
        Page<ExamSheet> examSheetPage = examSheetRepository
            .findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        
        log.info("기간별 시험지 조회 완료: startDate={}, endDate={}, totalElements={}", 
                startDate, endDate, examSheetPage.getTotalElements());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 학년별 특정 기간 내 생성된 시험지 조회
     * 
     * @param grade 학년
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findByGradeAndDateRange(Integer grade, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("학년별 기간별 시험지 조회: grade={}, startDate={}, endDate={}", grade, startDate, endDate);
        
        Page<ExamSheet> examSheetPage = examSheetRepository
            .findByGradeAndCreatedAtBetweenOrderByCreatedAtDesc(grade, startDate, endDate, pageable);
        
        log.info("학년별 기간별 시험지 조회 완료: grade={}, totalElements={}", grade, examSheetPage.getTotalElements());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 문제 개수가 특정 범위에 있는 시험지 조회
     * 
     * @param minQuestions 최소 문제 개수
     * @param maxQuestions 최대 문제 개수
     * @param pageable 페이징 정보
     * @return 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findByQuestionCountRange(Integer minQuestions, Integer maxQuestions, Pageable pageable) {
        log.info("문제 개수별 시험지 조회: minQuestions={}, maxQuestions={}", minQuestions, maxQuestions);
        
        Page<ExamSheet> examSheetPage = examSheetRepository
            .findByQuestionCountBetween(minQuestions, maxQuestions, pageable);
        
        log.info("문제 개수별 시험지 조회 완료: minQuestions={}, maxQuestions={}, totalElements={}", 
                minQuestions, maxQuestions, examSheetPage.getTotalElements());
        
        return examSheetPage.map(ExamSheetDto::from);
    }
    
    /**
     * 학년별 시험지 개수 조회
     * 
     * @param grade 학년
     * @return 시험지 개수
     */
    public Long countByGrade(Integer grade) {
        log.info("학년별 시험지 개수 조회: grade={}", grade);
        
        Long count = examSheetRepository.countByGrade(grade);
        
        log.info("학년별 시험지 개수 조회 완료: grade={}, count={}", grade, count);
        
        return count;
    }
    
    /**
     * 시험지명 중복 확인
     * 
     * @param examName 시험지명
     * @return 중복이면 true
     */
    public boolean isExamNameExists(String examName) {
        log.info("시험지명 중복 확인: examName={}", examName);
        
        boolean exists = examSheetRepository.existsByExamName(examName);
        
        log.info("시험지명 중복 확인 완료: examName={}, exists={}", examName, exists);
        
        return exists;
    }
    
    /**
     * 특정 학년에서 시험지명 중복 확인
     * 
     * @param grade 학년
     * @param examName 시험지명
     * @return 중복이면 true
     */
    public boolean isExamNameExistsInGrade(Integer grade, String examName) {
        log.info("학년별 시험지명 중복 확인: grade={}, examName={}", grade, examName);
        
        boolean exists = examSheetRepository.existsByGradeAndExamName(grade, examName);
        
        log.info("학년별 시험지명 중복 확인 완료: grade={}, examName={}, exists={}", grade, examName, exists);
        
        return exists;
    }
    
    /**
     * 최근 생성된 시험지 N개 조회
     * 
     * @param limit 조회할 개수
     * @return 최근 시험지 목록
     */
    public List<ExamSheetDto> findRecentExamSheets(int limit) {
        log.info("최근 시험지 조회: limit={}", limit);
        
        List<ExamSheet> recentExamSheets = examSheetRepository.findTopNRecent(limit);
        
        log.info("최근 시험지 조회 완료: limit={}, resultCount={}", limit, recentExamSheets.size());
        
        return recentExamSheets.stream()
            .map(ExamSheetDto::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 특정 학년의 최근 생성된 시험지 N개 조회
     * 
     * @param grade 학년
     * @param limit 조회할 개수
     * @return 최근 시험지 목록
     */
    public List<ExamSheetDto> findRecentExamSheetsByGrade(Integer grade, int limit) {
        log.info("학년별 최근 시험지 조회: grade={}, limit={}", grade, limit);
        
        List<ExamSheet> recentExamSheets = examSheetRepository.findTopNRecentByGrade(grade, limit);
        
        log.info("학년별 최근 시험지 조회 완료: grade={}, limit={}, resultCount={}", 
                grade, limit, recentExamSheets.size());
        
        return recentExamSheets.stream()
            .map(ExamSheetDto::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 학년별 시험지 통계 조회
     * 
     * @return 학년별 통계 목록
     */
    public List<ExamSheetGradeStatsDto> getGradeStatistics() {
        log.info("학년별 시험지 통계 조회 시작");
        
        List<ExamSheetRepository.ExamSheetGradeStats> stats = examSheetRepository.findExamSheetStatsByGrade();
        
        List<ExamSheetGradeStatsDto> result = stats.stream()
            .map(stat -> new ExamSheetGradeStatsDto(stat.getGrade(), stat.getCount()))
            .collect(Collectors.toList());
        
        log.info("학년별 시험지 통계 조회 완료: statsCount={}", result.size());
        
        return result;
    }
    
    /**
     * 최근 N개월간의 월별 시험지 생성 통계
     * 
     * @param monthsAgo 몇 개월 전부터
     * @return 월별 생성 통계
     */
    public List<ExamSheetMonthlyStatsDto> getMonthlyStatistics(int monthsAgo) {
        log.info("월별 시험지 생성 통계 조회: monthsAgo={}", monthsAgo);
        
        LocalDateTime startDate = LocalDateTime.now().minusMonths(monthsAgo);
        List<ExamSheetRepository.ExamSheetMonthlyStats> stats = 
            examSheetRepository.findMonthlyCreationStats(startDate);
        
        List<ExamSheetMonthlyStatsDto> result = stats.stream()
            .map(stat -> new ExamSheetMonthlyStatsDto(stat.getYear(), stat.getMonth(), stat.getCount()))
            .collect(Collectors.toList());
        
        log.info("월별 시험지 생성 통계 조회 완료: monthsAgo={}, statsCount={}", monthsAgo, result.size());
        
        return result;
    }
    
    /**
     * 필터링이 가능한 전체 시험지 목록 조회
     * 
     * @param pageable 페이징 정보
     * @param grade 학년 필터 (null이면 전체)
     * @param search 검색어 (null이면 전체)
     * @return 시험지 목록 (페이지 형태)
     */
    public Page<ExamSheetDto> findAll(Pageable pageable, Integer grade, String search) {
        log.info("필터링 시험지 목록 조회: grade={}, search={}", grade, search);
        
        if (grade != null && search != null && !search.trim().isEmpty()) {
            // 학년 + 검색어
            return examSheetRepository
                .findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(grade, search.trim(), pageable)
                .map(ExamSheetDto::from);
        } else if (grade != null) {
            // 학년만
            return examSheetRepository
                .findByGradeOrderByCreatedAtDesc(grade, pageable)
                .map(ExamSheetDto::from);
        } else if (search != null && !search.trim().isEmpty()) {
            // 검색어만
            return examSheetRepository
                .findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(search.trim(), pageable)
                .map(ExamSheetDto::from);
        } else {
            // 전체 조회
            return examSheetRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(ExamSheetDto::from);
        }
    }
    
    /**
     * 학년별 시험지 통계 조회 (컨트롤러 호환)
     * 
     * @return 학년별 통계 정보
     */
    public GradeStatistics getStatisticsByGrade() {
        log.info("학년별 시험지 통계 조회");
        
        List<ExamSheetGradeStatsDto> gradeStats = getGradeStatistics();
        GradeStatistics statistics = GradeStatistics.from(gradeStats);
        
        log.info("학년별 시험지 통계 조회 완료: grade1={}, grade2={}, grade3={}, total={}", 
                statistics.grade1Count(), statistics.grade2Count(), statistics.grade3Count(), statistics.totalCount());
        
        return statistics;
    }
    
    /**
     * 특정 시험지의 사용 현황 통계
     * 
     * @param examSheetId 시험지 ID
     * @return 사용 통계 정보
     */
    public UsageStatistics getUsageStatistics(UUID examSheetId) {
        log.info("시험지 사용 통계 조회: examSheetId={}", examSheetId);
        
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
            .orElseThrow(() -> new RuntimeException("시험지를 찾을 수 없습니다: " + examSheetId));
        
        // 이 시험지로 생성된 시험 개수 (사용 횟수)
        Long usageCount = examSheetRepository.countExamsByExamSheetId(examSheetId);
        
        // 가장 최근에 사용된 시간 (가장 최근에 생성된 시험의 생성 시간)
        LocalDateTime lastUsedAt = examSheetRepository.findLastUsedAtByExamSheetId(examSheetId);
        
        UsageStatistics statistics = new UsageStatistics(
            examSheet.getId(),
            examSheet.getExamName(),
            usageCount,
            lastUsedAt
        );
        
        log.info("시험지 사용 통계 조회 완료: examSheetId={}, usageCount={}, lastUsedAt={}", 
                examSheetId, usageCount, lastUsedAt);
        
        return statistics;
    }
    
    /**
     * 학년별 시험지 통계 DTO
     */
    public record ExamSheetGradeStatsDto(Integer grade, Long count) {}
    
    /**
     * 월별 시험지 생성 통계 DTO
     */
    public record ExamSheetMonthlyStatsDto(Integer year, Integer month, Long count) {}
    
    /**
     * 학년별 시험지 통계 (컨트롤러 호환)
     */
    public record GradeStatistics(
        Long grade1Count,
        Long grade2Count, 
        Long grade3Count,
        Long totalCount
    ) {
        public static GradeStatistics from(List<ExamSheetGradeStatsDto> gradeStats) {
            Long grade1 = gradeStats.stream().filter(s -> s.grade() == 1).mapToLong(ExamSheetGradeStatsDto::count).sum();
            Long grade2 = gradeStats.stream().filter(s -> s.grade() == 2).mapToLong(ExamSheetGradeStatsDto::count).sum();
            Long grade3 = gradeStats.stream().filter(s -> s.grade() == 3).mapToLong(ExamSheetGradeStatsDto::count).sum();
            Long total = grade1 + grade2 + grade3;
            
            return new GradeStatistics(grade1, grade2, grade3, total);
        }
    }
    
    /**
     * 시험지 사용 현황 통계
     */
    public record UsageStatistics(
        UUID examSheetId,
        String examSheetName,
        Long usageCount,
        LocalDateTime lastUsedAt
    ) {}
}