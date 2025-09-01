package com.iroomclass.springbackend.domain.analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.analysis.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.analysis.dto.OverallStatisticsResponse;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;

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
     * 전체 학년 통합 통계 조회
     * 모든 학년의 통합 통계 정보를 제공합니다.
     * 
     * @return 전체 통합 통계 정보
     */
    public OverallStatisticsResponse getOverallStatistics() {
        log.info("전체 학년 통합 통계 조회 시작");
        
        // 1단계: 전체 학년의 시험 제출 데이터 수집
        List<ExamSubmission> allSubmissions = examSubmissionRepository.findAll();
        
        if (allSubmissions.isEmpty()) {
            log.info("시험 제출 데이터가 없습니다");
            return createEmptyOverallStatistics();
        }
        
        // 2단계: 전체 학생 수 및 평균 성적 계산
        Map<String, List<ExamSubmission>> allStudentSubmissions = allSubmissions.stream()
            .collect(Collectors.groupingBy(submission -> 
                submission.getStudent().getName() + "_" + submission.getStudent().getPhone()));
        
        List<Double> allStudentAverageScores = new ArrayList<>();
        for (List<ExamSubmission> studentSubmissionList : allStudentSubmissions.values()) {
            double averageScore = studentSubmissionList.stream()
                .filter(submission -> submission.getTotalScore() != null)
                .mapToInt(submission -> submission.getTotalScore())
                .average()
                .orElse(0.0);
            
            if (averageScore > 0) {
                allStudentAverageScores.add(averageScore);
            }
        }
        
        // 3단계: 전체 평균 성적 계산
        double overallAverageScore = allStudentAverageScores.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        // 4단계: 전체 상/중/하위권 분포 계산
        OverallStatisticsResponse.OverallRankDistribution rankDistribution = calculateOverallRankDistribution(allStudentAverageScores);
        
        // 5단계: 학년별 세부 통계 계산
        List<OverallStatisticsResponse.GradeStatistics> gradeStatistics = calculateGradeStatistics(allSubmissions);
        
        log.info("전체 학년 통합 통계 조회 완료: 전체 학생 수={}, 평균 성적={}", 
                allStudentSubmissions.size(), overallAverageScore);
        
        return new OverallStatisticsResponse(
            allStudentSubmissions.size(),
            Math.round(overallAverageScore * 10.0) / 10.0,
            rankDistribution,
            gradeStatistics
        );
    }
    
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
            return new GradeSubmissionStatusResponse(
                grade,
                "중" + grade,
                new ArrayList<>(),
                0,
                0
            );
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
            
            examSubmissions.add(new GradeSubmissionStatusResponse.ExamSubmissionStatus(
                exam.getId(),
                exam.getExamName(),
                totalStudentCount,
                (int) submittedCount,
                Math.round(submissionRate * 10.0) / 10.0, // 소수점 첫째자리까지
                notSubmittedCount
            ));
            
            totalSubmissionCount += submittedCount;
        }
        
        log.info("학년별 시험 제출 현황 조회 완료: 학년={}, 시험 수={}, 총 제출 수={}", 
            grade, exams.size(), totalSubmissionCount);
        
        return new GradeSubmissionStatusResponse(
            grade,
            "중" + grade,
            examSubmissions,
            exams.size(),
            totalSubmissionCount
        );
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
                submission.getStudent().getName() + "_" + submission.getStudent().getPhone()));
        
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
        
        return new GradeScoreDistributionResponse(
            grade,
            "중" + grade,
            studentSubmissions.size(),
            studentAverageScores.size(),
            Math.round(overallAverageScore * 10.0) / 10.0,
            scoreRanges,
            rankDistribution
        );
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
            
            ranges.add(new GradeScoreDistributionResponse.ScoreRangeDistribution(
                startScore + "-" + endScore,
                (int) count,
                Math.round(percentage * 10.0) / 10.0,
                startScore,
                endScore
            ));
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
        
        return new GradeScoreDistributionResponse.RankDistribution(
            (int) topRankCount,
            Math.round(topRankPercentage * 10.0) / 10.0,
            (int) middleRankCount,
            Math.round(middleRankPercentage * 10.0) / 10.0,
            (int) bottomRankCount,
            Math.round(bottomRankPercentage * 10.0) / 10.0
        );
    }

    /**
     * 전체 학년 상/중/하위권 분포 계산
     */
    private OverallStatisticsResponse.OverallRankDistribution calculateOverallRankDistribution(List<Double> scores) {
        long highRankCount = scores.stream().filter(score -> score >= 80).count();
        long middleRankCount = scores.stream().filter(score -> score >= 60 && score < 80).count();
        long lowRankCount = scores.stream().filter(score -> score < 60).count();
        
        double totalCount = scores.size();
        double highRankPercentage = totalCount > 0 ? (double) highRankCount / totalCount * 100 : 0.0;
        double middleRankPercentage = totalCount > 0 ? (double) middleRankCount / totalCount * 100 : 0.0;
        double lowRankPercentage = totalCount > 0 ? (double) lowRankCount / totalCount * 100 : 0.0;
        
        return new OverallStatisticsResponse.OverallRankDistribution(
            (int) highRankCount,
            (int) middleRankCount,
            (int) lowRankCount,
            Math.round(highRankPercentage * 10.0) / 10.0,
            Math.round(middleRankPercentage * 10.0) / 10.0,
            Math.round(lowRankPercentage * 10.0) / 10.0
        );
    }
    
    /**
     * 학년별 세부 통계 계산
     */
    private List<OverallStatisticsResponse.GradeStatistics> calculateGradeStatistics(List<ExamSubmission> allSubmissions) {
        List<OverallStatisticsResponse.GradeStatistics> gradeStatistics = new ArrayList<>();
        
        // 학년별로 그룹핑
        Map<Integer, List<ExamSubmission>> submissionsByGrade = allSubmissions.stream()
            .collect(Collectors.groupingBy(submission -> submission.getExam().getGrade()));
        
        for (Map.Entry<Integer, List<ExamSubmission>> entry : submissionsByGrade.entrySet()) {
            Integer grade = entry.getKey();
            List<ExamSubmission> gradeSubmissions = entry.getValue();
            
            // 해당 학년의 학생별 평균 성적 계산
            Map<String, List<ExamSubmission>> studentSubmissions = gradeSubmissions.stream()
                .collect(Collectors.groupingBy(submission -> 
                    submission.getStudent().getName() + "_" + submission.getStudent().getPhone()));
            
            List<Double> gradeAverageScores = new ArrayList<>();
            for (List<ExamSubmission> studentSubmissionList : studentSubmissions.values()) {
                double averageScore = studentSubmissionList.stream()
                    .filter(submission -> submission.getTotalScore() != null)
                    .mapToInt(submission -> submission.getTotalScore())
                    .average()
                    .orElse(0.0);
                
                if (averageScore > 0) {
                    gradeAverageScores.add(averageScore);
                }
            }
            
            if (!gradeAverageScores.isEmpty()) {
                // 해당 학년의 통계 계산
                double averageScore = gradeAverageScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
                
                int maxScore = (int) gradeAverageScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);
                
                int minScore = (int) gradeAverageScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);
                
                gradeStatistics.add(new OverallStatisticsResponse.GradeStatistics(
                    grade,
                    studentSubmissions.size(),
                    Math.round(averageScore * 10.0) / 10.0,
                    maxScore,
                    minScore
                ));
            }
        }
        
        // 학년순으로 정렬
        gradeStatistics.sort((a, b) -> Integer.compare(a.grade(), b.grade()));
        
        return gradeStatistics;
    }
    
    /**
     * 빈 전체 통계 생성 (데이터가 없을 때)
     */
    private OverallStatisticsResponse createEmptyOverallStatistics() {
        OverallStatisticsResponse.OverallRankDistribution emptyRankDistribution = 
            new OverallStatisticsResponse.OverallRankDistribution(
                0, 0, 0, 0.0, 0.0, 0.0
            );
        
        return new OverallStatisticsResponse(
            0,
            0.0,
            emptyRankDistribution,
            new ArrayList<>()
        );
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
            emptyRanges.add(new GradeScoreDistributionResponse.ScoreRangeDistribution(
                range[0] + "-" + range[1],
                0,
                0.0,
                range[0],
                range[1]
            ));
        }
        
        GradeScoreDistributionResponse.RankDistribution emptyRankDistribution = 
            new GradeScoreDistributionResponse.RankDistribution(
                0,
                0.0,
                0,
                0.0,
                0,
                0.0
            );
        
        return new GradeScoreDistributionResponse(
            grade,
            "중" + grade,
            0,
            0,
            0.0,
            emptyRanges,
            emptyRankDistribution
        );
    }
}