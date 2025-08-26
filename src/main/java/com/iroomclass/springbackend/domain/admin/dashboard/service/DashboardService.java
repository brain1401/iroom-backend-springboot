package com.iroomclass.springbackend.domain.admin.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.dashboard.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.admin.dashboard.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;

/**
 * 대시보드 서비스
 * 
 * <p>관리자 대시보드 관련 비즈니스 로직을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

    /**
     * 학년별 시험 제출 현황 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험별 제출 현황
     */
    public GradeSubmissionStatusResponse getGradeSubmissionStatus(Integer grade) {
        log.info("학년별 시험 제출 현황 조회: 학년={}", grade);
        
        // 1단계: 해당 학년의 시험 목록 조회
        List<Exam> exams = examRepository.findByGrade(grade);
        
        if (exams.isEmpty()) {
            log.info("해당 학년의 시험이 없습니다: 학년={}", grade);
            return GradeSubmissionStatusResponse.builder()
                .grade(grade)
                .gradeName("중" + grade)
                .examSubmissions(new ArrayList<>())
                .totalExamCount(0)
                .totalSubmissionCount(0)
                .build();
        }
        
        // 2단계: 각 시험별 제출 현황 계산
        List<GradeSubmissionStatusResponse.ExamSubmissionStatus> examSubmissions = new ArrayList<>();
        int totalSubmissionCount = 0;
        
        for (Exam exam : exams) {
            // 해당 시험의 제출 수 조회
            long submittedCount = examSubmissionRepository.countByExamId(exam.getId());
            
            // 전체 학생 수 (시험 등록 시 입력한 학생 수)
            int totalStudentCount = exam.getStudentCount();
            int notSubmittedCount = totalStudentCount - (int) submittedCount;
            double submissionRate = totalStudentCount > 0 ? 
                (double) submittedCount / totalStudentCount * 100 : 0.0;
            
            examSubmissions.add(GradeSubmissionStatusResponse.ExamSubmissionStatus.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .totalStudentCount(totalStudentCount)
                .submittedStudentCount((int) submittedCount)
                .notSubmittedStudentCount(notSubmittedCount)
                .submissionRate(Math.round(submissionRate * 10.0) / 10.0) // 소수점 첫째자리까지
                .build());
            
            totalSubmissionCount += submittedCount;
        }
        
        log.info("학년별 시험 제출 현황 조회 완료: 학년={}, 시험 수={}, 총 제출 수={}", 
            grade, exams.size(), totalSubmissionCount);
        
        return GradeSubmissionStatusResponse.builder()
            .grade(grade)
            .gradeName("중" + grade)
            .examSubmissions(examSubmissions)
            .totalExamCount(exams.size())
            .totalSubmissionCount(totalSubmissionCount)
            .build();
    }

    /**
     * 학년별 성적 분포도 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 성적 분포도
     */
    public GradeScoreDistributionResponse getGradeScoreDistribution(Integer grade) {
        log.info("학년별 성적 분포도 조회: 학년={}", grade);
        
        // 1단계: 해당 학년의 모든 시험 제출 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamGrade(grade);
        
        if (submissions.isEmpty()) {
            log.info("해당 학년의 시험 제출 데이터가 없습니다: 학년={}", grade);
            return createEmptyScoreDistribution(grade);
        }
        
        // 2단계: 학생별 평균 성적 계산
        Map<String, List<ExamSubmission>> studentSubmissions = submissions.stream()
            .collect(Collectors.groupingBy(submission -> 
                submission.getStudentName() + "_" + submission.getStudentPhone()));
        
        List<Double> studentAverageScores = new ArrayList<>();
        for (List<ExamSubmission> studentSubmissionList : studentSubmissions.values()) {
            double averageScore = studentSubmissionList.stream()
                .filter(submission -> submission.getTotalScore() != null)
                .mapToInt(submission -> submission.getTotalScore())
                .average()
                .orElse(0.0);
            
            if (averageScore > 0) {
                studentAverageScores.add(averageScore);
            }
        }
        
        if (studentAverageScores.isEmpty()) {
            log.info("해당 학년의 유효한 성적 데이터가 없습니다: 학년={}", grade);
            return createEmptyScoreDistribution(grade);
        }
        
        // 3단계: 성적 구간별 분포 계산
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> scoreRanges = calculateScoreRanges(studentAverageScores);
        
        // 4단계: 상위권/중위권/하위권 통계 계산
        GradeScoreDistributionResponse.RankDistribution rankDistribution = calculateRankDistribution(studentAverageScores);
        
        // 5단계: 전체 평균 성적 계산
        double overallAverageScore = studentAverageScores.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        log.info("학년별 성적 분포도 조회 완료: 학년={}, 전체 학생 수={}, 평균 성적={}", 
            grade, studentSubmissions.size(), overallAverageScore);
        
        return GradeScoreDistributionResponse.builder()
            .grade(grade)
            .gradeName("중" + grade)
            .totalStudentCount(studentSubmissions.size())
            .studentWithScoreCount(studentAverageScores.size())
            .overallAverageScore(Math.round(overallAverageScore * 10.0) / 10.0)
            .scoreRanges(scoreRanges)
            .rankDistribution(rankDistribution)
            .build();
    }

    /**
     * 성적 구간별 분포 계산
     */
    private List<GradeScoreDistributionResponse.ScoreRangeDistribution> calculateScoreRanges(List<Double> scores) {
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> ranges = new ArrayList<>();
        
        int[][] scoreRanges = {
            {0, 39}, {40, 49}, {50, 59}, {60, 69}, {70, 79}, {80, 89}, {90, 100}
        };
        
        for (int[] range : scoreRanges) {
            int startScore = range[0];
            int endScore = range[1];
            
            long count = scores.stream()
                .filter(score -> score >= startScore && score <= endScore)
                .count();
            
            double percentage = scores.size() > 0 ? (double) count / scores.size() * 100 : 0.0;
            
            ranges.add(GradeScoreDistributionResponse.ScoreRangeDistribution.builder()
                .scoreRange(startScore + "-" + endScore)
                .studentCount((int) count)
                .percentage(Math.round(percentage * 10.0) / 10.0)
                .startScore(startScore)
                .endScore(endScore)
                .build());
        }
        
        return ranges;
    }

    /**
     * 상위권/중위권/하위권 통계 계산
     */
    private GradeScoreDistributionResponse.RankDistribution calculateRankDistribution(List<Double> scores) {
        long topRankCount = scores.stream().filter(score -> score >= 80).count();
        long middleRankCount = scores.stream().filter(score -> score >= 60 && score < 80).count();
        long bottomRankCount = scores.stream().filter(score -> score < 60).count();
        
        double totalCount = scores.size();
        double topRankPercentage = totalCount > 0 ? (double) topRankCount / totalCount * 100 : 0.0;
        double middleRankPercentage = totalCount > 0 ? (double) middleRankCount / totalCount * 100 : 0.0;
        double bottomRankPercentage = totalCount > 0 ? (double) bottomRankCount / totalCount * 100 : 0.0;
        
        return GradeScoreDistributionResponse.RankDistribution.builder()
            .topRankCount((int) topRankCount)
            .topRankPercentage(Math.round(topRankPercentage * 10.0) / 10.0)
            .middleRankCount((int) middleRankCount)
            .middleRankPercentage(Math.round(middleRankPercentage * 10.0) / 10.0)
            .bottomRankCount((int) bottomRankCount)
            .bottomRankPercentage(Math.round(bottomRankPercentage * 10.0) / 10.0)
            .build();
    }

    /**
     * 빈 성적 분포도 생성 (데이터가 없을 때)
     */
    private GradeScoreDistributionResponse createEmptyScoreDistribution(Integer grade) {
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> emptyRanges = new ArrayList<>();
        int[][] scoreRanges = {
            {0, 39}, {40, 49}, {50, 59}, {60, 69}, {70, 79}, {80, 89}, {90, 100}
        };
        
        for (int[] range : scoreRanges) {
            emptyRanges.add(GradeScoreDistributionResponse.ScoreRangeDistribution.builder()
                .scoreRange(range[0] + "-" + range[1])
                .studentCount(0)
                .percentage(0.0)
                .startScore(range[0])
                .endScore(range[1])
                .build());
        }
        
        GradeScoreDistributionResponse.RankDistribution emptyRankDistribution = 
            GradeScoreDistributionResponse.RankDistribution.builder()
                .topRankCount(0)
                .topRankPercentage(0.0)
                .middleRankCount(0)
                .middleRankPercentage(0.0)
                .bottomRankCount(0)
                .bottomRankPercentage(0.0)
                .build();
        
        return GradeScoreDistributionResponse.builder()
            .grade(grade)
            .gradeName("중" + grade)
            .totalStudentCount(0)
            .studentWithScoreCount(0)
            .overallAverageScore(0.0)
            .scoreRanges(emptyRanges)
            .rankDistribution(emptyRankDistribution)
            .build();
    }
}