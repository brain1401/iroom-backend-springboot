package com.iroomclass.springbackend.domain.unit.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.repository.UnitRepository;
import com.iroomclass.springbackend.domain.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.unit.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.unit.dto.UnitStatisticsResponse;

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
                
                return UnitListResponse.UnitInfo.builder()
                    .unitId(unit.getId())
                    .unitName(unit.getUnitName())
                    .description(unit.getDescription())
                    .displayOrder(unit.getDisplayOrder())
                    .questionCount((int) questionCount)
                    .build();
            })
            .collect(Collectors.toList());
        
        // 3단계: 전체 통계 계산
        int totalUnits = unitInfos.size();
        int totalQuestions = unitInfos.stream()
            .mapToInt(UnitListResponse.UnitInfo::getQuestionCount)
            .sum();
        
        log.info("학년 {} 단원 목록 조회 완료: {}개 단원, {}개 문제", grade, totalUnits, totalQuestions);
        
        return UnitListResponse.builder()
            .grade(grade)
            .units(unitInfos)
            .totalUnits(totalUnits)
            .totalQuestions(totalQuestions)
            .build();
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
                UnitStatisticsResponse.DifficultyCount difficultyCount = 
                    UnitStatisticsResponse.DifficultyCount.builder()
                        .easy(0)
                        .medium(0)
                        .hard(0)
                        .build();
                
                return UnitStatisticsResponse.UnitStat.builder()
                    .unitId(unit.getId())
                    .unitName(unit.getUnitName())
                    .totalQuestions((int) totalQuestions)
                    .difficultyCount(difficultyCount)
                    .build();
            })
            .collect(Collectors.toList());
        
        // 3단계: 전체 통계 계산
        int totalUnits = unitStats.size();
        int totalQuestions = unitStats.stream()
            .mapToInt(UnitStatisticsResponse.UnitStat::getTotalQuestions)
            .sum();
        
        // 전체 난이도별 문제 수 (현재는 모두 0)
        UnitStatisticsResponse.DifficultyCount totalDifficultyCount = 
            UnitStatisticsResponse.DifficultyCount.builder()
                .easy(0)
                .medium(0)
                .hard(0)
                .build();
        
        UnitStatisticsResponse.TotalStat totalStat = 
            UnitStatisticsResponse.TotalStat.builder()
                .totalUnits(totalUnits)
                .totalQuestions(totalQuestions)
                .difficultyCount(totalDifficultyCount)
                .build();
        
        log.info("학년 {} 단원 통계 조회 완료: {}개 단원, {}개 문제", grade, totalUnits, totalQuestions);
        
        return UnitStatisticsResponse.builder()
            .grade(grade)
            .unitStats(unitStats)
            .totalStat(totalStat)
            .build();
    }
}