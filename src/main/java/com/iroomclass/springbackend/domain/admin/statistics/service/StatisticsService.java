package com.iroomclass.springbackend.domain.admin.statistics.service;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.statistics.dto.GradeStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamAnswerRepository examAnswerRepository;

    /**
     * 학년별 통계 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 통계 정보
     */
    public GradeStatisticsResponse getGradeStatistics(Integer grade) {
        log.info("학년별 통계 조회: 학년={}", grade);
        
        // 1단계: 최근 시험 5개 조회 (최신순)
        List<Exam> recentExams = examRepository.findByGradeOrderByCreatedAtDesc(grade)
            .stream()
            .limit(5)
            .collect(Collectors.toList());
        
        // 2단계: 최근 시험별 평균 점수 계산
        List<GradeStatisticsResponse.RecentExamAverage> recentExamAverages = 
            calculateRecentExamAverages(recentExams);
        
        // 3단계: 오답률 높은 세부 단원 조회 (상위 5개)
        List<GradeStatisticsResponse.HighErrorRateUnit> highErrorRateUnits = 
            calculateHighErrorRateUnits(grade);
        
        log.info("학년별 통계 조회 완료: 학년={}, 최근 시험 수={}, 오답률 높은 단원 수={}", 
            grade, recentExams.size(), highErrorRateUnits.size());
        
        return new GradeStatisticsResponse(
            grade,
            "중" + grade,
            recentExamAverages,
            highErrorRateUnits
        );
    }

    /**
     * 최근 시험별 평균 점수 계산
     */
    private List<GradeStatisticsResponse.RecentExamAverage> calculateRecentExamAverages(List<Exam> exams) {
        List<GradeStatisticsResponse.RecentExamAverage> averages = new ArrayList<>();
        
        for (Exam exam : exams) {
            // 해당 시험의 모든 제출 조회
            List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(exam.getId());
            
            if (submissions.isEmpty()) {
                // 제출이 없는 경우 0점으로 처리
                averages.add(new GradeStatisticsResponse.RecentExamAverage(
                    exam.getId(),
                    exam.getExamName(),
                    0.0,
                    0,
                    exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ));
                continue;
            }
            
            // 유효한 성적이 있는 제출만 필터링
            List<ExamSubmission> validSubmissions = submissions.stream()
                .filter(submission -> submission.getTotalScore() != null)
                .collect(Collectors.toList());
            
            if (validSubmissions.isEmpty()) {
                // 유효한 성적이 없는 경우 0점으로 처리
                averages.add(new GradeStatisticsResponse.RecentExamAverage(
                    exam.getId(),
                    exam.getExamName(),
                    0.0,
                    submissions.size(),
                    exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ));
                continue;
            }
            
            // 평균 점수 계산
            double averageScore = validSubmissions.stream()
                .mapToInt(submission -> submission.getTotalScore())
                .average()
                .orElse(0.0);
            
            averages.add(new GradeStatisticsResponse.RecentExamAverage(
                exam.getId(),
                exam.getExamName(),
                Math.round(averageScore * 10.0) / 10.0, // 소수점 첫째자리까지
                validSubmissions.size(),
                exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ));
        }
        
        return averages;
    }

    /**
     * 오답률 높은 세부 단원 계산 (상위 5개)
     */
    private List<GradeStatisticsResponse.HighErrorRateUnit> calculateHighErrorRateUnits(Integer grade) {
        log.info("오답률 높은 단원 계산 시작: 학년={}", grade);
        
        // 1단계: 해당 학년의 모든 시험 제출 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamGrade(grade);
        
        if (submissions.isEmpty()) {
            log.info("해당 학년의 시험 제출 데이터가 없습니다: 학년={}", grade);
            return new ArrayList<>();
        }
        
        // 2단계: 모든 답안 데이터 조회
        List<Long> submissionIds = submissions.stream()
            .map(submission -> submission.getId())
            .collect(Collectors.toList());
        
        List<ExamAnswer> allAnswers = examAnswerRepository.findByExamSubmissionIdIn(submissionIds);
        
        if (allAnswers.isEmpty()) {
            log.info("해당 학년의 답안 데이터가 없습니다: 학년={}", grade);
            return new ArrayList<>();
        }
        
        // 3단계: 단원별 오답률 계산
        Map<Long, UnitErrorStats> unitErrorStatsMap = new HashMap<>();
        
        for (ExamAnswer answer : allAnswers) {
            // 문제 정보 조회 (단원 정보 포함)
            Question question = answer.getQuestion();
            if (question == null || question.getUnit() == null) {
                continue; // 단원 정보가 없는 문제는 건너뛰기
            }
            
            Long unitId = question.getUnit().getId();
            UnitErrorStats stats = unitErrorStatsMap.computeIfAbsent(unitId, 
                k -> new UnitErrorStats(question.getUnit().getUnitName()));
            
            stats.totalQuestions++;
            if (Boolean.FALSE.equals(answer.getIsCorrect())) {
                stats.wrongAnswers++;
            }
        }
        
        // 4단계: 오답률 계산 및 정렬 (상위 5개)
        List<GradeStatisticsResponse.HighErrorRateUnit> highErrorRateUnits = unitErrorStatsMap.entrySet()
            .stream()
            .map(entry -> {
                Long unitId = entry.getKey();
                UnitErrorStats stats = entry.getValue();
                double errorRate = stats.totalQuestions > 0 ? 
                    (double) stats.wrongAnswers / stats.totalQuestions * 100 : 0.0;
                
                return new GradeStatisticsResponse.HighErrorRateUnit(
                    unitId,
                    stats.unitName,
                    Math.round(errorRate * 10.0) / 10.0, // 소수점 첫째자리까지
                    stats.totalQuestions,
                    stats.wrongAnswers,
                    stats.totalQuestions - stats.wrongAnswers
                );
            })
            .filter(unit -> unit.totalQuestions() > 0) // 문제가 있는 단원만
            .sorted(Comparator.comparing(GradeStatisticsResponse.HighErrorRateUnit::errorRate).reversed())
            .limit(5) // 상위 5개만
            .collect(Collectors.toList());
        
        log.info("오답률 높은 단원 계산 완료: 학년={}, 단원 수={}", grade, highErrorRateUnits.size());
        
        return highErrorRateUnits;
    }
    
    /**
     * 단원별 오답 통계를 위한 내부 클래스
     */
    private static class UnitErrorStats {
        String unitName;
        int totalQuestions;
        int wrongAnswers;
        
        UnitErrorStats(String unitName) {
            this.unitName = unitName;
            this.totalQuestions = 0;
            this.wrongAnswers = 0;
        }
    }
}
