package com.iroomclass.springbackend.domain.curriculum.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.curriculum.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.curriculum.dto.UnitStatisticsResponse;
import com.iroomclass.springbackend.domain.curriculum.dto.UnitTreeResponse;
import com.iroomclass.springbackend.domain.curriculum.entity.Unit;
import com.iroomclass.springbackend.domain.curriculum.entity.UnitCategory;
import com.iroomclass.springbackend.domain.curriculum.entity.UnitSubcategory;
import com.iroomclass.springbackend.domain.curriculum.repository.UnitRepository;
import com.iroomclass.springbackend.domain.curriculum.repository.UnitCategoryRepository;
import com.iroomclass.springbackend.domain.curriculum.repository.UnitSubcategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 단원 관리 서비스
 * 
 * 학년별 단원 목록 조회, 통계 정보를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnitService {
    
    private final UnitRepository unitRepository;
    private final UnitCategoryRepository unitCategoryRepository;
    private final UnitSubcategoryRepository unitSubcategoryRepository;
    private final QuestionRepository questionRepository;
    
    /**
     * 특정 학년의 단원 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 단원 목록과 각 단원별 문제 수
     */
    public UnitListResponse getUnitsByGrade(int grade) {
        log.info("학년 {} 단원 목록 조회 요청", grade);
        
        // 1단계: 해당 학년의 모든 단원 조회 (ID 순으로 정렬)
        List<Unit> units = unitRepository.findByGradeOrderById(grade);
        
        // 2단계: 각 단원별 문제 수 계산
        List<UnitListResponse.UnitInfo> unitInfos = units.stream()
            .map(unit -> {
                // 각 단원의 문제 수 조회
                long questionCount = questionRepository.countByUnitId(unit.getId());
                
                return new UnitListResponse.UnitInfo(
                    unit.getId(),
                    unit.getUnitName(),
                    unit.getDescription(),
                    unit.getDisplayOrder(),
                    (int) questionCount
                );
            })
            .collect(Collectors.toList());
        
        // 3단계: 전체 통계 계산
        int totalUnits = unitInfos.size();
        int totalQuestions = unitInfos.stream()
            .mapToInt(UnitListResponse.UnitInfo::questionCount)
            .sum();
        
        log.info("학년 {} 단원 목록 조회 완료: {}개 단원, {}개 문제", grade, totalUnits, totalQuestions);
        
        return new UnitListResponse(
            grade,
            unitInfos,
            unitInfos.size(),
            totalQuestions
        );
    }
    
    /**
     * 특정 학년의 단원별 문제 수 통계 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 단원별 문제 수와 난이도별 분포
     */
    public UnitStatisticsResponse getUnitStatisticsByGrade(int grade) {
        log.info("학년 {} 단원 통계 조회 요청", grade);
        
        // 1단계: 해당 학년의 모든 단원 조회 (ID 순으로 정렬)
        List<Unit> units = unitRepository.findByGradeOrderById(grade);
        
        // 2단계: 각 단원별 문제 수와 난이도별 분포 계산
        List<UnitStatisticsResponse.UnitStat> unitStats = units.stream()
            .map(unit -> {
                // 해당 단원의 전체 문제 수
                long totalQuestions = questionRepository.countByUnitId(unit.getId());
                
                // 난이도별 문제 수 (실제로는 Question 엔티티에 difficulty 필드가 있어야 함)
                // 현재는 임시로 0으로 설정
                int easyCount = 0;
                int mediumCount = 0;
                int hardCount = 0;
                
                UnitStatisticsResponse.DifficultyCount difficultyCount = 
                    new UnitStatisticsResponse.DifficultyCount(
                        easyCount,
                        mediumCount,
                        hardCount
                    );
                
                return new UnitStatisticsResponse.UnitStat(
                    unit.getId(),
                    unit.getUnitName(),
                    (int) totalQuestions,
                    difficultyCount
                );
            })
            .collect(Collectors.toList());
        
        // 3단계: 전체 통계 계산
        int totalUnits = unitStats.size();
        int totalQuestions = unitStats.stream()
            .mapToInt(UnitStatisticsResponse.UnitStat::totalQuestions)
            .sum();
        
        // 전체 난이도별 문제 수 (현재는 모두 0)
        int totalEasyCount = 0;
        int totalMediumCount = 0; 
        int totalHardCount = 0;
        
        UnitStatisticsResponse.DifficultyCount totalDifficultyCount = 
            new UnitStatisticsResponse.DifficultyCount(
                        totalEasyCount,
                        totalMediumCount,
                        totalHardCount
                    );
        
        UnitStatisticsResponse.TotalStat totalStat = 
            new UnitStatisticsResponse.TotalStat(
                unitStats.size(),
                totalQuestions,
                totalDifficultyCount
            );
        
        log.info("학년 {} 단원 통계 조회 완료: {}개 단원, {}개 문제", grade, totalUnits, totalQuestions);
        
        return new UnitStatisticsResponse(
            grade,
            unitStats,
            totalStat
        );
    }
    
    /**
     * 전체 단원 트리 구조 조회
     * 
     * 대분류 → 중분류 → 세부단원의 계층 구조를 반환합니다.
     * 문제 직접 선택 시스템에서 사용됩니다.
     * 
     * @return 전체 단원 트리 구조
     */
    public List<UnitTreeResponse> getUnitTree() {
        log.info("단원 트리 구조 조회 시작");
        
        // 1단계: 모든 대분류를 표시 순서로 조회
        List<UnitCategory> categories = unitCategoryRepository.findAllByOrderByDisplayOrder();
        
        List<UnitTreeResponse> treeResponse = categories.stream()
            .map(category -> {
                // 2단계: 해당 대분류의 모든 중분류 조회
                List<UnitSubcategory> subcategories = unitSubcategoryRepository
                    .findByCategoryOrderByDisplayOrder(category);
                
                List<UnitTreeResponse.UnitSubcategoryNode> subcategoryNodes = subcategories.stream()
                    .map(subcategory -> {
                        // 3단계: 해당 중분류의 모든 세부단원 조회
                        List<Unit> units = unitRepository
                            .findBySubcategoryOrderByDisplayOrder(subcategory);
                        
                        List<UnitTreeResponse.UnitNode> unitNodes = units.stream()
                            .map(unit -> new UnitTreeResponse.UnitNode(
                                unit.getId(),
                                unit.getUnitName(),
                                unit.getUnitCode(),
                                unit.getGrade(),
                                unit.getDisplayOrder(),
                                unit.getDescription()
                            ))
                            .collect(Collectors.toList());
                        
                        return new UnitTreeResponse.UnitSubcategoryNode(
                            subcategory.getId(),
                            subcategory.getSubcategoryName(),
                            subcategory.getDisplayOrder(),
                            subcategory.getDescription(),
                            unitNodes
                        );
                    })
                    .collect(Collectors.toList());
                
                return new UnitTreeResponse(
                    category.getId(),
                    category.getCategoryName(),
                    category.getDisplayOrder(),
                    category.getDescription(),
                    subcategoryNodes
                );
            })
            .collect(Collectors.toList());
        
        log.info("단원 트리 구조 조회 완료: {}개 대분류", treeResponse.size());
        
        return treeResponse;
    }
    
    /**
     * 특정 단원의 문제 목록 조회 (페이징 지원)
     * 
     * 문제 직접 선택 시스템에서 사용됩니다.
     * 
     * @param unitId 단원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 문제 목록
     */
    public Page<UnitQuestionInfo> getUnitQuestions(UUID unitId, Pageable pageable) {
        log.info("단원 {} 문제 목록 조회 시작 (페이지: {}, 크기: {})", 
                unitId, pageable.getPageNumber(), pageable.getPageSize());
        
        // 1단계: 단원 정보 확인
        Unit unit = unitRepository.findById(unitId)
            .orElseThrow(() -> new RuntimeException("단원을 찾을 수 없습니다: " + unitId));
        
        // 2단계: 페이징된 문제 목록 조회
        Page<Question> questions = questionRepository.findByUnitId(unitId, pageable);
        
        // 3단계: DTO 변환
        Page<UnitQuestionInfo> result = questions.map(question -> new UnitQuestionInfo(
            question.getId(),
            question.getQuestionType().name(),
            question.getDifficulty().name(),
            unit.getUnitName(),
            unit.getId()
        ));
        
        log.info("단원 {} 문제 목록 조회 완료: {}개 문제 (전체 {}개 중 {}페이지)", 
                unitId, result.getNumberOfElements(), result.getTotalElements(), result.getNumber() + 1);
        
        return result;
    }
    
    /**
     * 단원별 문제 정보 DTO
     */
    public record UnitQuestionInfo(
        UUID questionId,
        String questionType,
        String difficulty,
        String unitName,
        UUID unitId
    ) {}
}