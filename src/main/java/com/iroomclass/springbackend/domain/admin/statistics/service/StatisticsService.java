package com.iroomclass.springbackend.domain.admin.statistics.service;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
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
        
        // 3단계: 오답률 높은 세부 단원 조회 (현재는 빈 리스트 반환)
        // TODO: ExamAnswer 엔티티가 구현되면 실제 오답률 계산 로직 추가
        List<GradeStatisticsResponse.HighErrorRateUnit> highErrorRateUnits = 
            new ArrayList<>();
        
        log.info("학년별 통계 조회 완료: 학년={}, 최근 시험 수={}, 오답률 높은 단원 수={}", 
            grade, recentExams.size(), highErrorRateUnits.size());
        
        return GradeStatisticsResponse.builder()
            .grade(grade)
            .gradeName("중" + grade)
            .recentExamAverages(recentExamAverages)
            .highErrorRateUnits(highErrorRateUnits)
            .build();
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
                averages.add(GradeStatisticsResponse.RecentExamAverage.builder()
                    .examId(exam.getId())
                    .examName(exam.getExamName())
                    .averageScore(0.0)
                    .studentCount(0)
                    .examDate(exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .build());
                continue;
            }
            
            // 유효한 성적이 있는 제출만 필터링
            List<ExamSubmission> validSubmissions = submissions.stream()
                .filter(submission -> submission.getTotalScore() != null)
                .collect(Collectors.toList());
            
            if (validSubmissions.isEmpty()) {
                // 유효한 성적이 없는 경우 0점으로 처리
                averages.add(GradeStatisticsResponse.RecentExamAverage.builder()
                    .examId(exam.getId())
                    .examName(exam.getExamName())
                    .averageScore(0.0)
                    .studentCount(submissions.size())
                    .examDate(exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .build());
                continue;
            }
            
            // 평균 점수 계산
            double averageScore = validSubmissions.stream()
                .mapToInt(submission -> submission.getTotalScore())
                .average()
                .orElse(0.0);
            
            averages.add(GradeStatisticsResponse.RecentExamAverage.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .averageScore(Math.round(averageScore * 10.0) / 10.0) // 소수점 첫째자리까지
                .studentCount(validSubmissions.size())
                .examDate(exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build());
        }
        
        return averages;
    }
}
