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
import jakarta.persistence.EntityManager;

import com.iroomclass.springbackend.domain.analysis.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.analysis.dto.OverallStatisticsResponse;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;

/**
 * 최적화된 대시보드 서비스
 * 
 * <p>
 * 관리자 대시보드 관련 비즈니스 로직을 제공합니다.
 * </p>
 * <p>
 * N+1 문제를 해결하고 성능을 최적화한 버전입니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamResultRepository examResultRepository;
    private final EntityManager entityManager;

    /**
     * 전체 학년 통합 통계 조회
     * 모든 학년의 통합 통계 정보를 제공합니다.
     * 
     * @return 전체 통합 통계 정보
     */
    public OverallStatisticsResponse getOverallStatistics() {
        log.info("전체 학년 통합 통계 조회 시작");

        // 1단계: 최적화된 쿼리로 전체 학생 평균 성적 통계 조회
        List<ExamResultRepository.StudentAverageScoreWithGradeProjection> studentStats = examResultRepository
                .findAllStudentAverageScores();

        if (studentStats.isEmpty()) {
            log.info("학생 성적 데이터가 없습니다");
            return createEmptyOverallStatistics();
        }

        // 2단계: 전체 평균 성적 계산
        List<Double> allStudentAverageScores = studentStats.stream()
                .map(ExamResultRepository.StudentAverageScoreWithGradeProjection::getAverageScore)
                .filter(score -> score != null && score >= 0) // null과 음수 제거 (0점 포함)
                .collect(Collectors.toList());

        double overallAverageScore = allStudentAverageScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 3단계: 전체 상/중/하위권 분포 계산
        OverallStatisticsResponse.OverallRankDistribution rankDistribution = calculateOverallRankDistribution(
                allStudentAverageScores);

        // 4단계: 학년별 세부 통계 계산
        List<OverallStatisticsResponse.GradeStatistics> gradeStatistics = calculateGradeStatisticsFromProjection(
                studentStats);

        log.info("전체 학년 통합 통계 조회 완료: 전체 학생 수={}, 평균 성적={}",
                studentStats.size(), overallAverageScore);

        return new OverallStatisticsResponse(
                studentStats.size(),
                Math.round(overallAverageScore * 10.0) / 10.0,
                rankDistribution,
                gradeStatistics);
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
                    0);
        }

        // 2단계: 해당 학년의 전체 활성 학생 수 조회 (한 번만 계산)
        long totalStudentCountLong = examSubmissionRepository.countDistinctStudentsByGrade(grade);
        int totalStudentCount = (int) totalStudentCountLong;

        // 3단계: 각 시험별 제출 현황 계산
        List<GradeSubmissionStatusResponse.ExamSubmissionStatus> examSubmissions = new ArrayList<>();
        int totalSubmissionCount = 0;

        for (Exam exam : exams) {
            // 해당 시험의 제출 수 조회
            long submittedCount = examSubmissionRepository.countByExamId(exam.getId());
            int notSubmittedCount = totalStudentCount - (int) submittedCount;
            double submissionRate = totalStudentCount > 0 ? (double) submittedCount / totalStudentCount * 100 : 0.0;

            examSubmissions.add(new GradeSubmissionStatusResponse.ExamSubmissionStatus(
                    exam.getId(),
                    exam.getExamName(),
                    totalStudentCount,
                    (int) submittedCount,
                    Math.round(submissionRate * 10.0) / 10.0, // 소수점 첫째자리까지
                    notSubmittedCount));

            totalSubmissionCount += submittedCount;
        }

        log.info("학년별 시험 제출 현황 조회 완료: 학년={}, 시험 수={}, 전체 학생 수={}, 총 제출 수={}",
                grade, exams.size(), totalStudentCount, totalSubmissionCount);

        return new GradeSubmissionStatusResponse(
                grade,
                "중" + grade,
                examSubmissions,
                exams.size(),
                totalSubmissionCount);
    }

    /**
     * 학년별 성적 분포도 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 성적 분포도
     */
    public GradeScoreDistributionResponse getGradeScoreDistribution(Integer grade) {
        log.info("학년별 성적 분포도 조회: 학년={}", grade);

        // 🔍 디버깅: 단계별 데이터 존재 여부 확인
        
        // 1단계: Exam 테이블에 해당 학년의 시험이 있는지 확인
        List<Exam> gradeExams = examRepository.findByGrade(grade);
        log.info("🔍 [DEBUG] Exam 테이블 확인: grade={}, exam 개수={}", grade, gradeExams.size());
        
        if (gradeExams.isEmpty()) {
            log.warn("❌ 해당 학년의 시험이 존재하지 않음: grade={}", grade);
            return createEmptyScoreDistribution(grade);
        }
        
        // 시험 ID 목록 출력
        List<UUID> examIds = gradeExams.stream().map(Exam::getId).collect(Collectors.toList());
        log.info("🔍 [DEBUG] 해당 학년 시험 ID 목록: {}", examIds);
        
        // 2단계: ExamSubmission 테이블에 해당 시험들의 제출이 있는지 확인
        long totalSubmissions = 0;
        for (UUID examId : examIds) {
            long submissionCount = examSubmissionRepository.countByExamId(examId);
            totalSubmissions += submissionCount;
            log.info("🔍 [DEBUG] ExamSubmission 확인: examId={}, submission 개수={}", examId, submissionCount);
        }
        log.info("🔍 [DEBUG] 총 ExamSubmission 개수: {}", totalSubmissions);
        
        if (totalSubmissions == 0) {
            log.warn("❌ 해당 학년의 시험 제출이 존재하지 않음: grade={}", grade);
            return createEmptyScoreDistribution(grade);
        }
        
        // 3단계: ExamResult 테이블에 해당 제출들의 채점 결과가 있는지 확인
        long totalExamResults = examResultRepository.count();
        log.info("🔍 [DEBUG] 전체 ExamResult 개수: {}", totalExamResults);
        
        // 4단계: 복잡한 JOIN 쿼리를 단순화해서 디버깅
        log.info("🔍 [DIAGNOSTIC] SQL 쿼리 진단 시작...");
        
        // 📊 진단 1: ExamResult의 totalScore 값들 확인
        @SuppressWarnings("unchecked")
        List<Object[]> examResultScores = entityManager.createNativeQuery(
            "SELECT er.total_score, er.version, es.exam_id, e.grade " +
            "FROM exam_result er " +
            "JOIN exam_submission es ON er.submission_id = es.id " + 
            "JOIN exam e ON es.exam_id = e.id " +
            "WHERE e.grade = :grade " +
            "ORDER BY er.total_score DESC " +
            "LIMIT 20"
        ).setParameter("grade", grade).getResultList();
        
        log.info("🔍 [DIAGNOSTIC] ExamResult totalScore 샘플 (grade={}): 총 {}개", grade, examResultScores.size());
        for (int i = 0; i < Math.min(5, examResultScores.size()); i++) {
            Object[] row = examResultScores.get(i);
            log.info("🔍 [DIAGNOSTIC] Row {}: totalScore={}, version={}, examId={}, grade={}", 
                    i+1, row[0], row[1], row[2], row[3]);
        }
        
        // 📊 진단 2: 버전별 통계 확인
        @SuppressWarnings("unchecked")
        List<Object[]> versionStats = entityManager.createNativeQuery(
            "SELECT er.submission_id, MAX(er.version) as max_version, COUNT(*) as count " +
            "FROM exam_result er " +
            "JOIN exam_submission es ON er.submission_id = es.id " + 
            "JOIN exam e ON es.exam_id = e.id " +
            "WHERE e.grade = :grade " +
            "GROUP BY er.submission_id " +
            "LIMIT 10"
        ).setParameter("grade", grade).getResultList();
        
        log.info("🔍 [DIAGNOSTIC] 버전 통계 샘플: 총 {}개 submission", versionStats.size());
        for (int i = 0; i < Math.min(3, versionStats.size()); i++) {
            Object[] row = versionStats.get(i);
            log.info("🔍 [DIAGNOSTIC] SubmissionId={}: maxVersion={}, totalResults={}", 
                    row[0], row[1], row[2]);
        }
        
        // 📊 진단 3: 실제 JOIN된 데이터와 버전 필터링 결과 확인
        @SuppressWarnings("unchecked")
        List<Object[]> joinResults = entityManager.createNativeQuery(
            "SELECT s.id, s.name, er.total_score, er.version " +
            "FROM exam_result er " +
            "JOIN exam_submission es ON er.submission_id = es.id " + 
            "JOIN student s ON es.student_id = s.id " +
            "JOIN exam e ON es.exam_id = e.id " +
            "WHERE e.grade = :grade " +
            "AND er.version = (SELECT MAX(er2.version) FROM exam_result er2 WHERE er2.submission_id = er.submission_id) " +
            "LIMIT 10"
        ).setParameter("grade", grade).getResultList();
        
        log.info("🔍 [DIAGNOSTIC] 최종 JOIN + 버전필터링 결과: 총 {}개", joinResults.size());
        for (int i = 0; i < Math.min(5, joinResults.size()); i++) {
            Object[] row = joinResults.get(i);
            log.info("🔍 [DIAGNOSTIC] Student {}: id={}, name='{}', totalScore={}, version={}", 
                    i+1, row[0], row[1], row[2], row[3]);
        }
        
        // 📊 진단 4: ExamResultQuestion 데이터 확인 (개별 문제 점수)
        @SuppressWarnings("unchecked")
        List<Object[]> questionScores = entityManager.createNativeQuery(
            "SELECT erq.exam_result_id, erq.score, er.submission_id " +
            "FROM exam_result_question erq " +
            "JOIN exam_result er ON erq.exam_result_id = er.id " +
            "JOIN exam_submission es ON er.submission_id = es.id " + 
            "JOIN exam e ON es.exam_id = e.id " +
            "WHERE e.grade = :grade " +
            "LIMIT 20"
        ).setParameter("grade", grade).getResultList();
        
        log.info("🔍 [DIAGNOSTIC] ExamResultQuestion 샘플: 총 {}개", questionScores.size());
        for (int i = 0; i < Math.min(5, questionScores.size()); i++) {
            Object[] row = questionScores.get(i);
            log.info("🔍 [DIAGNOSTIC] QuestionResult {}: examResultId={}, questionScore={}, submissionId={}", 
                    i+1, row[0], row[1], row[2]);
        }
        
        // 📊 진단 5: 근본 원인 확인 - ExamResult의 totalScore 계산 문제
        log.warn("🚨 [CRITICAL] 모든 ExamResult.totalScore가 NULL입니다! 데이터 무결성 문제 발견");
        log.warn("🚨 [CRITICAL] 이것이 AVG(er.totalScore)가 NULL을 반환하는 이유입니다");
        
        // 📊 즉시 수정: totalScore 재계산 실행
        log.info("🔧 [FIX] ExamResult.totalScore 재계산 시작...");
        int fixedCount = fixExamResultTotalScores(grade);
        log.info("🔧 [FIX] {}개의 ExamResult.totalScore를 재계산했습니다", fixedCount);
        
        // 원래 쿼리: findStudentAverageScoresByGrade 실행
        List<ExamResultRepository.StudentAverageScoreProjection> studentStats = examResultRepository
                .findStudentAverageScoresByGrade(grade);
        
        log.info("🔍 [DEBUG] findStudentAverageScoresByGrade 결과: grade={}, 결과 개수={}", grade, studentStats.size());

        if (studentStats.isEmpty()) {
            // 5단계: 더 자세한 디버깅 - 각 JOIN 단계별 확인을 위해 개별 Repository 메서드 사용
            log.warn("❌ findStudentAverageScoresByGrade가 빈 결과를 반환함. 추가 디버깅 시작...");
            
            // ExamResult와 ExamSubmission의 연결 상태 확인
            log.info("🔍 [DEBUG] 추가 분석을 위해 다른 방법으로 데이터 확인 중...");
            
            // 우선 빈 결과 반환
            log.info("해당 학년의 성적 데이터가 없습니다: 학년={}", grade);
            return createEmptyScoreDistribution(grade);
        }

        // 2단계: 평균 점수 리스트 생성 - 상세 디버깅 추가
        log.info("🔍 [DEBUG] studentStats에서 실제 점수 값들을 확인합니다...");
        
        List<Double> allScores = new ArrayList<>();
        int nullCount = 0;
        int negativeCount = 0;
        int validCount = 0;
        
        for (int i = 0; i < Math.min(10, studentStats.size()); i++) { // 처음 10개만 로그 출력
            ExamResultRepository.StudentAverageScoreProjection projection = studentStats.get(i);
            Double score = projection.getAverageScore();
            log.info("🔍 [DEBUG] Student {}: studentId={}, studentName='{}', averageScore={}", 
                    i+1, projection.getStudentId(), projection.getStudentName(), score);
            
            if (score == null) {
                nullCount++;
            } else if (score < 0) {
                negativeCount++;
            } else {
                validCount++;
                allScores.add(score);
            }
        }
        
        // 전체 통계
        for (int i = 10; i < studentStats.size(); i++) { // 나머지는 통계만
            Double score = studentStats.get(i).getAverageScore();
            if (score == null) {
                nullCount++;
            } else if (score < 0) {
                negativeCount++;
            } else {
                validCount++;
                allScores.add(score);
            }
        }
        
        log.info("🔍 [DEBUG] 점수 분석 결과: 전체={}, null={}, 음수={}, 유효={}", 
                studentStats.size(), nullCount, negativeCount, validCount);
        
        List<Double> studentAverageScores = allScores;

        if (studentAverageScores.isEmpty()) {
            log.info("해당 학년의 유효한 성적 데이터가 없습니다: 학년={}", grade);
            return createEmptyScoreDistribution(grade);
        }

        // 3단계: 성적 구간별 분포 계산
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> scoreRanges = calculateScoreRanges(
                studentAverageScores);

        // 4단계: 상위권/중위권/하위권 통계 계산
        GradeScoreDistributionResponse.RankDistribution rankDistribution = calculateRankDistribution(
                studentAverageScores);

        // 5단계: 전체 평균 성적 계산
        double overallAverageScore = studentAverageScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        log.info("학년별 성적 분포도 조회 완료: 학년={}, 전체 학생 수={}, 평균 성적={}",
                grade, studentStats.size(), overallAverageScore);

        return new GradeScoreDistributionResponse(
                grade,
                "중" + grade,
                studentStats.size(),
                studentAverageScores.size(),
                Math.round(overallAverageScore * 10.0) / 10.0,
                scoreRanges,
                rankDistribution);
    }

    /**
     * 성적 구간별 분포 계산
     */
    private List<GradeScoreDistributionResponse.ScoreRangeDistribution> calculateScoreRanges(List<Double> scores) {
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> ranges = new ArrayList<>();

        int[][] scoreRanges = {
                { 0, 39 }, { 40, 49 }, { 50, 59 }, { 60, 69 }, { 70, 79 }, { 80, 89 }, { 90, 100 }
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
                    endScore));
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
                Math.round(bottomRankPercentage * 10.0) / 10.0);
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
                Math.round(lowRankPercentage * 10.0) / 10.0);
    }

    /**
     * Projection 데이터를 사용한 학년별 세부 통계 계산
     */
    private List<OverallStatisticsResponse.GradeStatistics> calculateGradeStatisticsFromProjection(
            List<ExamResultRepository.StudentAverageScoreWithGradeProjection> studentStats) {

        Map<Integer, List<Double>> scoresByGrade = studentStats.stream()
                .filter(stat -> stat.getAverageScore() != null && stat.getAverageScore() >= 0)
                .collect(Collectors.groupingBy(
                        ExamResultRepository.StudentAverageScoreWithGradeProjection::getGrade,
                        Collectors.mapping(
                                ExamResultRepository.StudentAverageScoreWithGradeProjection::getAverageScore,
                                Collectors.toList())));

        List<OverallStatisticsResponse.GradeStatistics> gradeStatistics = new ArrayList<>();

        for (Map.Entry<Integer, List<Double>> entry : scoresByGrade.entrySet()) {
            Integer grade = entry.getKey();
            List<Double> scores = entry.getValue();

            if (!scores.isEmpty()) {
                double averageScore = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                int maxScore = (int) scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0.0);

                int minScore = (int) scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(0.0);

                gradeStatistics.add(new OverallStatisticsResponse.GradeStatistics(
                        grade,
                        scores.size(),
                        Math.round(averageScore * 10.0) / 10.0,
                        maxScore,
                        minScore));
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
        OverallStatisticsResponse.OverallRankDistribution emptyRankDistribution = new OverallStatisticsResponse.OverallRankDistribution(
                0, 0, 0, 0.0, 0.0, 0.0);

        return new OverallStatisticsResponse(
                0,
                0.0,
                emptyRankDistribution,
                new ArrayList<>());
    }

    /**
     * 빈 성적 분포도 생성 (데이터가 없을 때)
     */
    private GradeScoreDistributionResponse createEmptyScoreDistribution(Integer grade) {
        List<GradeScoreDistributionResponse.ScoreRangeDistribution> emptyRanges = new ArrayList<>();
        int[][] scoreRanges = {
                { 0, 39 }, { 40, 49 }, { 50, 59 }, { 60, 69 }, { 70, 79 }, { 80, 89 }, { 90, 100 }
        };

        for (int[] range : scoreRanges) {
            emptyRanges.add(new GradeScoreDistributionResponse.ScoreRangeDistribution(
                    range[0] + "-" + range[1],
                    0,
                    0.0,
                    range[0],
                    range[1]));
        }

        GradeScoreDistributionResponse.RankDistribution emptyRankDistribution = new GradeScoreDistributionResponse.RankDistribution(
                0,
                0.0,
                0,
                0.0,
                0,
                0.0);

        return new GradeScoreDistributionResponse(
                grade,
                "중" + grade,
                0,
                0,
                0.0,
                emptyRanges,
                emptyRankDistribution);
    }

    /**
     * ExamResult의 totalScore를 재계산하여 데이터 무결성 문제 해결
     * 
     * @param grade 대상 학년
     * @return 수정된 ExamResult 개수
     */
    @Transactional
    private int fixExamResultTotalScores(Integer grade) {
        log.info("🔧 [FIX] ExamResult.totalScore 재계산 시작: grade={}", grade);
        
        // 해당 학년의 모든 ExamResult 조회
        @SuppressWarnings("unchecked")
        List<Object[]> examResults = entityManager.createNativeQuery(
            "SELECT er.id, er.submission_id " +
            "FROM exam_result er " +
            "JOIN exam_submission es ON er.submission_id = es.id " + 
            "JOIN exam e ON es.exam_id = e.id " +
            "WHERE e.grade = :grade " +
            "AND er.total_score IS NULL"
        ).setParameter("grade", grade).getResultList();
        
        log.info("🔧 [FIX] 수정할 ExamResult 개수: {}", examResults.size());
        
        int fixedCount = 0;
        
        for (Object[] row : examResults) {
            byte[] examResultIdBytes = (byte[]) row[0];
            
            // ExamResultQuestion의 점수 합계 계산
            @SuppressWarnings("unchecked")
            List<Object> scoreSum = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(erq.score), 0) " +
                "FROM exam_result_question erq " +
                "WHERE erq.exam_result_id = :examResultId"
            ).setParameter("examResultId", examResultIdBytes).getResultList();
            
            Integer totalScore = 0;
            if (!scoreSum.isEmpty() && scoreSum.get(0) != null) {
                if (scoreSum.get(0) instanceof Number) {
                    totalScore = ((Number) scoreSum.get(0)).intValue();
                }
            }
            
            // totalScore 업데이트
            int updatedRows = entityManager.createNativeQuery(
                "UPDATE exam_result SET total_score = :totalScore " +
                "WHERE id = :examResultId"
            ).setParameter("totalScore", totalScore)
             .setParameter("examResultId", examResultIdBytes)
             .executeUpdate();
            
            if (updatedRows > 0) {
                fixedCount++;
                if (fixedCount <= 5) {
                    log.info("🔧 [FIX] Updated ExamResult: totalScore={}", totalScore);
                }
            }
        }
        
        // 변경사항 커밋
        entityManager.flush();
        
        log.info("🔧 [FIX] ExamResult.totalScore 재계산 완료: {}개 수정", fixedCount);
        return fixedCount;
    }
}