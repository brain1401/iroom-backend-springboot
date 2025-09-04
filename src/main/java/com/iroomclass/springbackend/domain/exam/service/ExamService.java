package com.iroomclass.springbackend.domain.exam.service;

import com.iroomclass.springbackend.domain.exam.dto.ExamDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamFilterRequest;
import com.iroomclass.springbackend.domain.exam.dto.ExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamWithUnitsDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitSummaryDto;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository.ExamSubmissionStats;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitProjection;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitBasicProjection;
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
    
    /**
     * 통합 필터링을 통한 시험 목록 조회
     * 
     * <p>다양한 필터링 조건을 조합하여 시험 목록을 조회합니다.
     * 기존 개별 메서드들을 통합한 단일 진입점입니다.</p>
     * 
     * @param filter 필터링 조건
     * @param pageable 페이징 정보
     * @return 필터링된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findExamsWithFilter(ExamFilterRequest filter, Pageable pageable) {
        log.info("통합 필터링 시험 조회: {}, page={}, size={}", 
                filter.getFilterDescription(), pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ExamDto> result;
        
        // 필터 조건에 따라 적절한 메서드 호출
        if (filter.hasGradeFilter() && filter.hasSearchFilter()) {
            // 학년 + 검색어 복합 필터
            result = searchByGradeAndExamName(filter.grade(), filter.search(), pageable);
            
        } else if (filter.hasGradeFilter()) {
            // 학년별 필터
            result = findByGrade(filter.grade(), pageable);
            
        } else if (filter.hasSearchFilter()) {
            // 검색어 필터
            result = searchByExamName(filter.search(), pageable);
            
        } else {
            // 전체 조회 또는 최근 시험
            result = findAll(pageable);
        }
        
        log.info("통합 필터링 시험 조회 완료: {}, totalElements={}", 
                filter.getFilterDescription(), result.getTotalElements());
        
        return result;
    }
    
    /**
     * 시험 통계 조회 (통합)
     * 
     * @param statisticsType 통계 타입 ("by-grade" 등)
     * @return 통계 데이터 맵
     */
    public Map<String, Object> getExamStatistics(String statisticsType) {
        log.info("시험 통계 조회: type={}", statisticsType);
        
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        switch (statisticsType) {
            case "by-grade" -> {
                // 학년별 통계
                long grade1Count = examRepository.countByGrade(1);
                long grade2Count = examRepository.countByGrade(2);
                long grade3Count = examRepository.countByGrade(3);
                long totalCount = grade1Count + grade2Count + grade3Count;
                
                statistics.put("grade1", grade1Count);
                statistics.put("grade2", grade2Count);
                statistics.put("grade3", grade3Count);
                statistics.put("total", totalCount);
                
                // 비율 계산
                if (totalCount > 0) {
                    Map<String, Double> percentages = new java.util.HashMap<>();
                    percentages.put("grade1Percentage", Math.round((double) grade1Count / totalCount * 100 * 100.0) / 100.0);
                    percentages.put("grade2Percentage", Math.round((double) grade2Count / totalCount * 100 * 100.0) / 100.0);
                    percentages.put("grade3Percentage", Math.round((double) grade3Count / totalCount * 100 * 100.0) / 100.0);
                    statistics.put("percentages", percentages);
                }
            }
            default -> throw new IllegalArgumentException("지원하지 않는 통계 타입입니다: " + statisticsType);
        }
        
        log.info("시험 통계 조회 완료: type={}, resultKeys={}", statisticsType, statistics.keySet());
        return statistics;
    }

    // ========================================
    // 단원 정보 포함 최적화 메서드
    // ========================================

    /**
     * 시험 ID로 단원 정보가 포함된 상세 정보 조회 (성능 최적화)
     * 
     * <p>@EntityGraph를 사용하여 N+1 쿼리 문제 없이 한 번의 쿼리로
     * 시험과 관련된 모든 단원 정보를 가져옵니다.</p>
     * 
     * @param examId 시험 식별자
     * @return 단원 정보가 포함된 시험 DTO
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamWithUnitsDto findByIdWithUnits(UUID examId) {
        log.info("단원 정보 포함 시험 조회 시작: examId={}", examId);
        
        // 성능 최적화된 쿼리로 시험 + 단원 정보 조회
        Exam exam = examRepository.findByIdWithUnits(examId)
            .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));
        
        // 기본 시험 DTO 생성
        ExamDto examDto = ExamDto.fromWithExamSheet(exam);
        
        // 단원 정보 추출 및 변환
        List<UnitSummaryDto> units = extractUnitsFromExam(exam);
        List<ExamWithUnitsDto.UnitQuestionCount> unitQuestionCounts = calculateUnitQuestionCounts(exam);
        
        log.info("단원 정보 포함 시험 조회 완료: examId={}, unitCount={}", examId, units.size());
        
        return ExamWithUnitsDto.from(examDto, units, unitQuestionCounts);
    }

    /**
     * 다중 시험 ID로 단원 정보가 포함된 시험 목록 배치 조회
     * 
     * <p>여러 시험을 한 번에 조회하여 대시보드나 목록 페이지에서
     * 여러 시험의 단원 정보를 효율적으로 표시할 때 사용합니다.</p>
     * 
     * @param examIds 시험 식별자 목록
     * @return 단원 정보가 포함된 시험 DTO 목록
     */
    public List<ExamWithUnitsDto> findByIdsWithUnits(List<UUID> examIds) {
        log.info("다중 시험 단원 정보 배치 조회 시작: examCount={}", examIds.size());
        
        if (examIds.isEmpty()) {
            return List.of();
        }
        
        // 배치 조회 (최적화된 쿼리)
        List<Exam> exams = examRepository.findByIdInWithUnits(examIds);
        
        // 각 시험별로 DTO 변환
        List<ExamWithUnitsDto> result = exams.stream()
            .map(exam -> {
                ExamDto examDto = ExamDto.fromWithExamSheet(exam);
                List<UnitSummaryDto> units = extractUnitsFromExam(exam);
                List<ExamWithUnitsDto.UnitQuestionCount> unitQuestionCounts = calculateUnitQuestionCounts(exam);
                return ExamWithUnitsDto.from(examDto, units, unitQuestionCounts);
            })
            .collect(Collectors.toList());
        
        log.info("다중 시험 단원 정보 배치 조회 완료: examCount={}, resultCount={}", examIds.size(), result.size());
        return result;
    }

    /**
     * Projection을 사용한 경량 단원 정보 조회
     * 
     * <p>메모리 사용량을 최소화하면서 필요한 단원 정보만 조회합니다.
     * 대량의 시험 데이터를 다룰 때 유용합니다.</p>
     * 
     * @param examId 시험 식별자
     * @return 단원 정보 Projection 목록
     */
    public List<UnitSummaryDto> findUnitsByExamId(UUID examId) {
        log.info("시험별 단원 정보 Projection 조회 시작: examId={}", examId);
        
        List<UnitProjection> unitProjections = examRepository.findUnitsByExamId(examId);
        
        List<UnitSummaryDto> units = unitProjections.stream()
            .map(this::convertToUnitSummaryDto)
            .collect(Collectors.toList());
        
        log.info("시험별 단원 정보 Projection 조회 완료: examId={}, unitCount={}", examId, units.size());
        return units;
    }

    /**
     * 단원별 문제 수 통계 조회
     * 
     * <p>특정 시험의 각 단원별 문제 수와 배점을 계산합니다.
     * 시험 분석이나 리포트 생성에 활용됩니다.</p>
     * 
     * @param examId 시험 식별자
     * @return 단원별 문제 수 통계
     */
    public List<ExamWithUnitsDto.UnitQuestionCount> getUnitQuestionCounts(UUID examId) {
        log.info("시험별 단원 문제 수 통계 조회 시작: examId={}", examId);
        
        List<Object[]> rawData = examRepository.findUnitQuestionCountsByExamId(examId);
        
        List<ExamWithUnitsDto.UnitQuestionCount> counts = rawData.stream()
            .map(row -> new ExamWithUnitsDto.UnitQuestionCount(
                (UUID) row[0],      // unitId
                (String) row[1],    // unitName
                ((Number) row[2]).intValue(),  // questionCount
                ((Number) row[3]).intValue()   // totalPoints
            ))
            .collect(Collectors.toList());
        
        log.info("시험별 단원 문제 수 통계 조회 완료: examId={}, unitCount={}", examId, counts.size());
        return counts;
    }

    /**
     * 다중 시험의 단원별 문제 수 통계 배치 조회
     * 
     * @param examIds 시험 식별자 목록
     * @return 시험별 단원별 문제 수 통계 맵
     */
    public Map<UUID, List<ExamWithUnitsDto.UnitQuestionCount>> getUnitQuestionCountsByExamIds(List<UUID> examIds) {
        log.info("다중 시험 단원 문제 수 통계 배치 조회 시작: examCount={}", examIds.size());
        
        if (examIds.isEmpty()) {
            return Map.of();
        }
        
        List<Object[]> rawData = examRepository.findUnitQuestionCountsByExamIds(examIds);
        
        Map<UUID, List<ExamWithUnitsDto.UnitQuestionCount>> result = rawData.stream()
            .collect(Collectors.groupingBy(
                row -> (UUID) row[0], // examId로 그룹핑
                Collectors.mapping(
                    row -> new ExamWithUnitsDto.UnitQuestionCount(
                        (UUID) row[1],      // unitId
                        (String) row[2],    // unitName
                        ((Number) row[3]).intValue(),  // questionCount
                        ((Number) row[4]).intValue()   // totalPoints
                    ),
                    Collectors.toList()
                )
            ));
        
        log.info("다중 시험 단원 문제 수 통계 배치 조회 완료: examCount={}, resultCount={}", examIds.size(), result.size());
        return result;
    }

    /**
     * 특정 학년의 모든 시험에서 사용된 단원 목록 조회
     * 
     * <p>학년별 커리큘럼 분석이나 단원별 출제 빈도 분석에 사용됩니다.</p>
     * 
     * @param grade 학년
     * @return 해당 학년 시험에 사용된 단원 목록
     */
    public List<UnitSummaryDto> getDistinctUnitsByGrade(Integer grade) {
        log.info("학년별 사용된 단원 목록 조회 시작: grade={}", grade);
        
        List<UnitBasicProjection> basicProjections = examRepository.findDistinctUnitsByGrade(grade);
        
        List<UnitSummaryDto> units = basicProjections.stream()
            .map(this::convertBasicToUnitSummaryDto)
            .collect(Collectors.toList());
        
        log.info("학년별 사용된 단원 목록 조회 완료: grade={}, unitCount={}", grade, units.size());
        return units;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * 시험 엔티티에서 단원 정보를 추출하여 DTO로 변환
     */
    private List<UnitSummaryDto> extractUnitsFromExam(Exam exam) {
        return exam.getExamSheet().getQuestions().stream()
            .map(esq -> esq.getQuestion().getUnit())
            .distinct()
            .map(this::convertUnitToDto)
            .sorted((u1, u2) -> {
                // 대분류 > 중분류 > 단원 순서로 정렬
                int categoryCompare = u1.category().displayOrder().compareTo(u2.category().displayOrder());
                if (categoryCompare != 0) return categoryCompare;
                
                int subcategoryCompare = u1.subcategory().displayOrder().compareTo(u2.subcategory().displayOrder());
                if (subcategoryCompare != 0) return subcategoryCompare;
                
                return u1.displayOrder().compareTo(u2.displayOrder());
            })
            .collect(Collectors.toList());
    }

    /**
     * 시험 엔티티에서 단원별 문제 수를 계산
     */
    private List<ExamWithUnitsDto.UnitQuestionCount> calculateUnitQuestionCounts(Exam exam) {
        Map<UUID, Long> questionCountByUnit = exam.getExamSheet().getQuestions().stream()
            .collect(Collectors.groupingBy(
                esq -> esq.getQuestion().getUnit().getId(),
                Collectors.counting()
            ));
        
        Map<UUID, Integer> totalPointsByUnit = exam.getExamSheet().getQuestions().stream()
            .collect(Collectors.groupingBy(
                esq -> esq.getQuestion().getUnit().getId(),
                Collectors.summingInt(esq -> esq.getPoints() != null ? esq.getPoints() : 0)
            ));
        
        return exam.getExamSheet().getQuestions().stream()
            .map(esq -> esq.getQuestion().getUnit())
            .distinct()
            .map(unit -> new ExamWithUnitsDto.UnitQuestionCount(
                unit.getId(),
                unit.getUnitName(),
                questionCountByUnit.get(unit.getId()).intValue(),
                totalPointsByUnit.get(unit.getId())
            ))
            .collect(Collectors.toList());
    }

    /**
     * Unit 엔티티를 UnitSummaryDto로 변환
     */
    private UnitSummaryDto convertUnitToDto(com.iroomclass.springbackend.domain.unit.entity.Unit unit) {
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
            unit.getSubcategory().getId(),
            unit.getSubcategory().getSubcategoryName(),
            unit.getSubcategory().getDescription(),
            unit.getSubcategory().getDisplayOrder()
        );
        
        var category = new UnitSummaryDto.UnitCategoryInfo(
            unit.getSubcategory().getCategory().getId(),
            unit.getSubcategory().getCategory().getCategoryName(),
            unit.getSubcategory().getCategory().getDescription(),
            unit.getSubcategory().getCategory().getDisplayOrder()
        );
        
        return new UnitSummaryDto(
            unit.getId(),
            unit.getUnitName(),
            unit.getUnitCode(),
            unit.getGrade(),
            unit.getDescription(),
            unit.getDisplayOrder(),
            subcategory,
            category
        );
    }

    /**
     * UnitProjection을 UnitSummaryDto로 변환
     */
    private UnitSummaryDto convertToUnitSummaryDto(UnitProjection projection) {
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
            projection.getSubcategory().getId(),
            projection.getSubcategory().getSubcategoryName(),
            projection.getSubcategory().getDescription(),
            projection.getSubcategory().getDisplayOrder()
        );
        
        var category = new UnitSummaryDto.UnitCategoryInfo(
            projection.getSubcategory().getCategory().getId(),
            projection.getSubcategory().getCategory().getCategoryName(),
            projection.getSubcategory().getCategory().getDescription(),
            projection.getSubcategory().getCategory().getDisplayOrder()
        );
        
        return new UnitSummaryDto(
            projection.getId(),
            projection.getUnitName(),
            projection.getUnitCode(),
            projection.getGrade(),
            projection.getDescription(),
            projection.getDisplayOrder(),
            subcategory,
            category
        );
    }

    /**
     * UnitBasicProjection을 UnitSummaryDto로 변환 (계층 정보 없음)
     */
    private UnitSummaryDto convertBasicToUnitSummaryDto(UnitBasicProjection projection) {
        // 기본 projection은 계층 정보가 없으므로 별도 조회가 필요할 수 있음
        // 여기서는 null로 설정하고, 필요시 추가 조회 로직 구현
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
            null, "정보 없음", null, 0
        );
        
        var category = new UnitSummaryDto.UnitCategoryInfo(
            null, "정보 없음", null, 0
        );
        
        return new UnitSummaryDto(
            projection.getId(),
            projection.getUnitName(),
            projection.getUnitCode(),
            projection.getGrade(),
            null, // description 없음
            projection.getDisplayOrder(),
            subcategory,
            category
        );
    }
}