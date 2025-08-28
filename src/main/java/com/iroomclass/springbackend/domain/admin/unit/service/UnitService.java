package com.iroomclass.springbackend.domain.admin.unit.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.admin.unit.dto.UnitListResponse;
import com.iroomclass.springbackend.domain.admin.unit.dto.UnitStatisticsResponse;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitRepository;

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
}