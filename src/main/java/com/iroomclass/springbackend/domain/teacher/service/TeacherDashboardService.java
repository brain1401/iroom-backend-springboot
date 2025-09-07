package com.iroomclass.springbackend.domain.teacher.service;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.teacher.dto.ExamAverageScoreDto;
import com.iroomclass.springbackend.domain.teacher.dto.ExamSubmissionDetailDto;
import com.iroomclass.springbackend.domain.teacher.dto.RecentExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.teacher.dto.ScoreDistributionDto;
import com.iroomclass.springbackend.domain.teacher.dto.StudentAnswerDetailDto;
import com.iroomclass.springbackend.domain.teacher.dto.UnitWrongAnswerRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 선생님 대시보드 관련 비즈니스 로직 처리 서비스
 * 
 * <p>
 * 선생님이 필요로 하는 시험 통계, 제출 현황, 성적 분석 등의
 * 복잡한 조회 기능을 제공합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TeacherDashboardService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamResultRepository examResultRepository;
    private final StudentRepository studentRepository;

    /**
     * 학년별 최근 시험들의 제출 현황을 조회합니다
     * 
     * @param grade 학년 (1, 2, 3)
     * @param limit 조회할 최근 시험 개수 (기본값: 10)
     * @return 최근 시험들의 제출 현황
     */
    public RecentExamSubmissionStatusDto getRecentExamsSubmissionStatus(Integer grade, int limit) {
        log.info("학년별 최근 시험 제출 현황 조회 시작: grade={}, limit={}", grade, limit);

        // 1. 해당 학년의 최근 시험 목록 조회
        List<Exam> recentExams = examRepository.findRecentExamsByGrade(grade, limit);

        if (recentExams.isEmpty()) {
            log.info("해당 학년의 시험이 없음: grade={}", grade);
            return RecentExamSubmissionStatusDto.create(grade, List.of());
        }

        log.info("조회된 최근 시험 수: grade={}, examCount={}", grade, recentExams.size());

        // 2. 해당 학년의 전체 학생 수 조회 (시험을 본 학생들 기준)
        Long totalStudentsInGrade = examSubmissionRepository.countStudentsByGrade(grade);
        log.info("해당 학년 전체 학생 수: grade={}, studentCount={}", grade, totalStudentsInGrade);

        // 3. 시험 ID 목록 추출
        List<UUID> examIds = recentExams.stream()
                .map(Exam::getId)
                .collect(Collectors.toList());

        // 4. 시험별 제출 통계 조회
        List<ExamSubmissionRepository.ExamDetailStats> examStats = examSubmissionRepository
                .findExamDetailStats(examIds);

        // 통계를 맵으로 변환 (빠른 조회를 위해)
        Map<UUID, ExamSubmissionRepository.ExamDetailStats> statsMap = examStats.stream()
                .collect(Collectors.toMap(
                        ExamSubmissionRepository.ExamDetailStats::getExamId,
                        stats -> stats));

        // 5. 응답 DTO 생성
        List<RecentExamSubmissionStatusDto.ExamSubmissionInfo> examSubmissionInfos = recentExams.stream()
                .map(exam -> {
                    ExamSubmissionRepository.ExamDetailStats stats = statsMap.get(exam.getId());

                    Long submissionCount = (stats != null) ? stats.getSubmissionCount() : 0L;
                    Long questionCount = (stats != null) ? stats.getQuestionCount() : 0L;

                    return RecentExamSubmissionStatusDto.ExamSubmissionInfo.create(
                            exam.getId(),
                            exam.getExamName(),
                            exam.getCreatedAt(),
                            exam.getMaxStudent(),
                            submissionCount,
                            questionCount.intValue());
                })
                .collect(Collectors.toList());

        log.info("학년별 최근 시험 제출 현황 조회 완료: grade={}, examCount={}, avgSubmissionRate={}",
                grade, examSubmissionInfos.size(),
                examSubmissionInfos.stream()
                        .mapToDouble(info -> info.submissionRate().doubleValue())
                        .average().orElse(0.0));

        return RecentExamSubmissionStatusDto.create(grade, examSubmissionInfos);
    }

    /**
     * 전체 학년의 최근 시험 제출 현황을 조회합니다
     * 
     * @param limit 각 학년별 조회할 시험 개수 (기본값: 5)
     * @return 전체 학년별 최근 시험 제출 현황
     */
    public Map<Integer, RecentExamSubmissionStatusDto> getAllGradesRecentExamsStatus(int limit) {
        log.info("전체 학년 최근 시험 제출 현황 조회 시작: limit={}", limit);

        Map<Integer, RecentExamSubmissionStatusDto> result = Map.of(
                1, getRecentExamsSubmissionStatus(1, limit),
                2, getRecentExamsSubmissionStatus(2, limit),
                3, getRecentExamsSubmissionStatus(3, limit));

        log.info("전체 학년 최근 시험 제출 현황 조회 완료");
        return result;
    }

    /**
     * 학년별 시험 통계 요약 정보를 조회합니다
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 시험 통계 요약
     */
    public ExamStatsSummary getExamStatsSummary(Integer grade) {
        log.info("학년별 시험 통계 요약 조회 시작: grade={}", grade);

        // 전체 시험 수
        long totalExamCount = examRepository.countByGrade(grade);

        // 전체 제출 수
        long totalSubmissionCount = examSubmissionRepository.countByExamGrade(grade);

        // 해당 학년 학생 수
        Long totalStudentCount = examSubmissionRepository.countStudentsByGrade(grade);

        log.info("학년별 시험 통계 요약 조회 완료: grade={}, examCount={}, submissionCount={}, studentCount={}",
                grade, totalExamCount, totalSubmissionCount, totalStudentCount);

        return new ExamStatsSummary(
                grade,
                totalExamCount,
                totalSubmissionCount,
                totalStudentCount != null ? totalStudentCount : 0L);
    }

    /**
     * 시험 통계 요약 정보 DTO
     */
    public record ExamStatsSummary(
            Integer grade,
            Long totalExamCount,
            Long totalSubmissionCount,
            Long totalStudentCount) {
        /**
         * 평균 제출률 계산
         */
        public double getAverageSubmissionRate() {
            if (totalExamCount == 0 || totalStudentCount == 0) {
                return 0.0;
            }

            double expectedTotalSubmissions = totalExamCount * totalStudentCount;
            return (totalSubmissionCount / expectedTotalSubmissions) * 100.0;
        }
    }

    /**
     * 특정 시험의 상세 제출자 현황을 조회합니다
     * 
     * @param examId 시험 ID
     * @return 시험 제출자 상세 현황
     */
    public ExamSubmissionDetailDto getExamSubmissionDetail(UUID examId) {
        log.info("시험 상세 제출자 현황 조회 시작: examId={}", examId);

        // 1. 시험 기본 정보 조회 (시험지 정보 포함)
        Exam exam = examRepository.findByIdWithExamSheet(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        log.info("시험 정보 조회 완료: examId={}, examName={}, grade={}",
                examId, exam.getExamName(), exam.getGrade());

        // 2. 시험지의 문제 개수 계산
        Integer questionCount = 0;
        if (exam.getExamSheet() != null && exam.getExamSheet().getQuestions() != null) {
            questionCount = exam.getExamSheet().getQuestions().size();
        }

        // 3. 시험 기본 정보 DTO 생성
        ExamSubmissionDetailDto.ExamBasicInfo examInfo = ExamSubmissionDetailDto.ExamBasicInfo.create(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getCreatedAt(),
                questionCount,
                exam.getContent());

        // 4. 해당 학년의 모든 학생 목록 조회 (시험을 본 적이 있는 학생들)
        List<Student> allStudentsInGrade = studentRepository.findStudentsByGrade(exam.getGrade());
        log.info("해당 학년 전체 학생 수: grade={}, studentCount={}", exam.getGrade(), allStudentsInGrade.size());

        // 5. 해당 시험에 제출한 학생들의 제출 정보 조회 (학생 정보 포함)
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamIdWithStudent(examId);
        log.info("시험 제출자 수: examId={}, submissionCount={}", examId, submissions.size());

        // 6. 제출한 학생들의 상세 정보 생성 (제출 순서 포함)
        AtomicInteger submissionOrder = new AtomicInteger(1);
        List<ExamSubmissionDetailDto.StudentSubmissionInfo> submittedStudents = submissions.stream()
                .map(submission -> ExamSubmissionDetailDto.StudentSubmissionInfo.create(
                        submission.getStudent().getId(),
                        submission.getStudent().getName(),
                        submission.getStudent().getPhone(),
                        submission.getSubmittedAt(),
                        exam.getCreatedAt(),
                        submissionOrder.getAndIncrement()))
                .collect(Collectors.toList());

        // 7. 제출하지 않은 학생들 조회
        List<Student> notSubmittedStudents = studentRepository.findStudentsNotSubmittedToExam(
                exam.getGrade(), examId);

        List<ExamSubmissionDetailDto.StudentInfo> notSubmittedStudentInfos = notSubmittedStudents.stream()
                .map(student -> ExamSubmissionDetailDto.StudentInfo.create(
                        student.getId(),
                        student.getName(),
                        student.getPhone()))
                .collect(Collectors.toList());

        log.info("시험 미제출자 수: examId={}, notSubmittedCount={}", examId, notSubmittedStudentInfos.size());

        // 8. 전체 응답 DTO 생성
        ExamSubmissionDetailDto result = ExamSubmissionDetailDto.create(
                examInfo,
                (long) allStudentsInGrade.size(),
                submittedStudents,
                notSubmittedStudentInfos);

        log.info("시험 상세 제출자 현황 조회 완료: examId={}, totalStudents={}, submitted={}, notSubmitted={}, submissionRate={}",
                examId, result.statistics().totalStudentCount(), result.statistics().submittedCount(),
                result.statistics().notSubmittedCount(), result.statistics().submissionRate());

        return result;
    }

    /**
     * 특정 학생의 특정 시험에 대한 상세 답안을 조회합니다
     * 
     * @param examId    시험 ID
     * @param studentId 학생 ID
     * @return 학생의 상세 답안 정보
     */
    public StudentAnswerDetailDto getStudentAnswerDetail(UUID examId, Long studentId) {
        log.info("학생별 상세 답안 조회 시작: examId={}, studentId={}", examId, studentId);

        // 1. 시험 기본 정보 조회
        Exam exam = examRepository.findByIdWithExamSheet(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 2. 학생 기본 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다: " + studentId));

        // 3. 학생의 시험 제출 정보 조회
        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new RuntimeException(
                        "해당 학생의 시험 제출 정보를 찾을 수 없습니다: examId=" + examId + ", studentId=" + studentId));

        // 4. 최신 채점 결과 조회 (문제별 상세 정보 포함)
        ExamResult examResult = examResultRepository.findDetailedResultByExamIdAndStudentId(examId, studentId)
                .orElseThrow(
                        () -> new RuntimeException("채점 결과를 찾을 수 없습니다: examId=" + examId + ", studentId=" + studentId));

        log.info("채점 결과 조회 완료: examId={}, studentId={}, totalScore={}, questionCount={}",
                examId, studentId, examResult.getTotalScore(), examResult.getQuestionResults().size());

        // 5. 기본 정보 DTO 생성
        StudentAnswerDetailDto.StudentBasicInfo studentInfo = StudentAnswerDetailDto.StudentBasicInfo.create(
                student.getId(),
                student.getName(),
                student.getPhone());

        // 총 문제 수 계산 (examResult의 questionResults에서 가져옴)
        Integer totalQuestions = examResult.getQuestionResults().size();
        // 총 가능 점수 계산 (각 문제의 점수 합)
        Integer totalPossibleScore = examResult.getQuestionResults().stream()
                .mapToInt(qr -> qr.getQuestion().getPoints())
                .sum();

        StudentAnswerDetailDto.ExamBasicInfo examInfo = StudentAnswerDetailDto.ExamBasicInfo.create(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getCreatedAt(),
                totalQuestions,
                totalPossibleScore);

        StudentAnswerDetailDto.SubmissionInfo submissionInfo = StudentAnswerDetailDto.SubmissionInfo.create(
                submission.getId(),
                submission.getSubmittedAt(),
                exam.getCreatedAt());

        // 6. 채점 결과 정보 생성
        // 정답/오답 개수 계산 (null safety 추가)
        Integer correctAnswers = (int) examResult.getQuestionResults().stream()
                .mapToLong(qr -> {
                    Boolean isCorrect = qr.getIsCorrect();
                    return (isCorrect != null && isCorrect) ? 1 : 0;
                })
                .sum();
        Integer incorrectAnswers = examResult.getQuestionResults().size() - correctAnswers;

        StudentAnswerDetailDto.GradingResult gradingResult = StudentAnswerDetailDto.GradingResult.create(
                examResult.getTotalScore(),
                totalPossibleScore,
                correctAnswers,
                incorrectAnswers,
                examResult.getGradedAt(),
                null,  // version 컬럼이 제거됨
                examResult.getScoringComment());

        // 7. 문제별 답안 정보 생성
        java.util.concurrent.atomic.AtomicInteger orderCounter = new java.util.concurrent.atomic.AtomicInteger(1);
        List<StudentAnswerDetailDto.QuestionAnswer> questionAnswers = examResult.getQuestionResults().stream()
                .map(questionResult -> {
                    // Unit 정보 생성
                    StudentAnswerDetailDto.QuestionAnswer.UnitInfo unitInfo = null;
                    if (questionResult.getQuestion().getUnit() != null) {
                        var unit = questionResult.getQuestion().getUnit();
                        var subcategory = unit.getSubcategory();
                        var category = subcategory != null ? subcategory.getCategory() : null;

                        unitInfo = StudentAnswerDetailDto.QuestionAnswer.UnitInfo.create(
                                unit.getId(),
                                unit.getUnitName(),
                                subcategory != null ? subcategory.getSubcategoryName() : null,
                                category != null ? category.getCategoryName() : null);
                    }

                    // 학생 답안 정보 (임시로 빈 문자열 처리 - 향후 StudentAnswerSheet 연관관계 구현 필요)
                    String studentAnswer = ""; // TODO: StudentAnswerSheet -> StudentAnswerSheetQuestion을 통해 답안 조회

                    return StudentAnswerDetailDto.QuestionAnswer.create(
                            questionResult.getQuestion().getId(),
                            orderCounter.getAndIncrement(), // 순서번호
                            questionResult.getQuestion().getQuestionText(), // 문제 요약으로 사용
                            questionResult.getQuestion().getQuestionType().name(), // 문제 유형
                            questionResult.getQuestion().getDifficulty().name(), // 난이도
                            questionResult.getQuestion().getPoints(), // 배점
                            studentAnswer != null ? studentAnswer : "", // 학생 답안
                            questionResult.getQuestion().getAnswerText(), // 정답
                            questionResult.getScore(), // 획득 점수
                            questionResult.getIsCorrect(), // 정답 여부
                            questionResult.getFeedback(), // 피드백
                            unitInfo // 단원 정보
                    );
                })
                .collect(Collectors.toList());

        log.info("학생별 상세 답안 조회 완료: examId={}, studentId={}, questionCount={}, totalScore={}/{}",
                examId, studentId, questionAnswers.size(), examResult.getTotalScore(),
                questionAnswers.stream().mapToInt(qa -> qa.maxPoints()).sum());

        // 8. 전체 응답 DTO 생성
        return StudentAnswerDetailDto.create(
                studentInfo,
                examInfo,
                submissionInfo,
                gradingResult,
                questionAnswers);
    }

    /**
     * 특정 시험의 모든 학생 상세 답안을 조회합니다 (페이징 지원)
     * 
     * @param examId   시험 ID
     * @param pageable 페이징 정보
     * @return 학생들의 상세 답안 목록
     */
    public List<StudentAnswerDetailDto> getAllStudentsAnswerDetails(UUID examId,
            org.springframework.data.domain.Pageable pageable) {
        log.info("시험의 전체 학생 상세 답안 조회 시작: examId={}, page={}, size={}",
                examId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 시험에 제출한 모든 학생 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamIdWithStudent(examId);

        // 2. 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), submissions.size());

        List<ExamSubmission> pagedSubmissions = submissions.subList(start, end);

        // 3. 각 학생별 상세 답안 조회
        List<StudentAnswerDetailDto> results = pagedSubmissions.stream()
                .map(submission -> getStudentAnswerDetail(examId, submission.getStudent().getId()))
                .collect(Collectors.toList());

        log.info("시험의 전체 학생 상세 답안 조회 완료: examId={}, resultCount={}", examId, results.size());

        return results;
    }

    /**
     * 학년별 성적 분포도를 조회합니다
     * 전체 학생들의 평균 성적을 구간별로 나누어 분포를 계산합니다
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 성적 분포도 정보
     */
    public ScoreDistributionDto getScoreDistribution(Integer grade) {
        log.info("학년별 성적 분포도 조회 시작: grade={}", grade);

        // 1. 학년별 학생 평균 점수 목록 조회
        List<Double> rawStudentAverageScores = examResultRepository.findStudentAverageScoresByGrade(grade);

        // Null 값 필터링 - totalScore가 null인 경우 평균값도 null일 수 있음
        List<Double> studentAverageScores = rawStudentAverageScores.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("학년별 성적 분포도 원본 데이터: grade={}, rawCount={}, validCount={}",
                grade, rawStudentAverageScores.size(), studentAverageScores.size());

        if (studentAverageScores.isEmpty()) {
            log.info("해당 학년의 유효한 채점 결과가 없음: grade={}", grade);
            return createEmptyScoreDistribution(grade);
        }

        log.info("학년별 성적 분포도 계산: grade={}, studentCount={}", grade, studentAverageScores.size());

        // 2. 전체 통계 계산
        long totalStudentCount = studentAverageScores.size();

        // 평균 계산
        Double avgValue = studentAverageScores.stream()
                .collect(Collectors.averagingDouble(Double::doubleValue));
        BigDecimal averageScore = BigDecimal.valueOf(avgValue).setScale(2, RoundingMode.HALF_UP);

        // 최대값, 최소값 계산
        BigDecimal maxScore = studentAverageScores.stream()
                .max(Double::compareTo)
                .map(max -> BigDecimal.valueOf(max).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);

        BigDecimal minScore = studentAverageScores.stream()
                .min(Double::compareTo)
                .map(min -> BigDecimal.valueOf(min).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);

        // 중앙값 계산
        BigDecimal medianScore = calculateMedian(studentAverageScores);

        // 표준편차 계산
        BigDecimal standardDeviation = calculateStandardDeviation(studentAverageScores, averageScore.doubleValue());

        // 3. 점수 구간별 분포 조회
        List<ExamResultRepository.ScoreRangeDistribution> rawDistributions = examResultRepository
                .findScoreDistributionByGrade(grade);

        // 4. 분포 데이터를 DTO로 변환
        List<ScoreDistributionDto.ScoreDistribution> distributions = convertToScoreDistributions(rawDistributions,
                totalStudentCount);

        // 5. 응답 DTO 생성
        ScoreDistributionDto result = ScoreDistributionDto.create(
                grade,
                totalStudentCount,
                averageScore,
                standardDeviation,
                medianScore,
                distributions,
                maxScore,
                minScore);

        log.info("학년별 성적 분포도 조회 완료: grade={}, studentCount={}, avgScore={}, stdDev={}",
                grade, totalStudentCount, averageScore, standardDeviation);

        return result;
    }

    /**
     * 빈 성적 분포도 생성 (데이터가 없는 경우)
     */
    private ScoreDistributionDto createEmptyScoreDistribution(Integer grade) {
        List<ScoreDistributionDto.ScoreDistribution> emptyDistributions = ScoreDistributionDto.getStandardScoreRanges()
                .stream()
                .map(range -> ScoreDistributionDto.ScoreDistribution.create(
                        range.min(), range.max(), 0L, 0L))
                .collect(Collectors.toList());

        return ScoreDistributionDto.create(
                grade, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                emptyDistributions, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * 중앙값 계산
     */
    private BigDecimal calculateMedian(List<Double> scores) {
        List<Double> sortedScores = new ArrayList<>(scores);
        Collections.sort(sortedScores);

        int size = sortedScores.size();
        if (size == 0) {
            return BigDecimal.ZERO;
        }

        if (size % 2 == 0) {
            // 짝수 개수인 경우 중간 두 값의 평균
            double median = (sortedScores.get(size / 2 - 1) + sortedScores.get(size / 2)) / 2.0;
            return BigDecimal.valueOf(median).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 홀수 개수인 경우 중간값
            return BigDecimal.valueOf(sortedScores.get(size / 2)).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 표준편차 계산
     */
    private BigDecimal calculateStandardDeviation(List<Double> scores, double mean) {
        if (scores.size() <= 1) {
            return BigDecimal.ZERO;
        }

        // 분산 계산 (편차 제곱의 평균)
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - mean, 2))
                .average()
                .orElse(0.0);

        // 표준편차 = 분산의 제곱근
        double stdDev = Math.sqrt(variance);

        return BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Repository 결과를 분포 DTO로 변환
     */
    private List<ScoreDistributionDto.ScoreDistribution> convertToScoreDistributions(
            List<ExamResultRepository.ScoreRangeDistribution> rawDistributions,
            long totalStudentCount) {

        // 표준 점수 구간 정의
        Map<String, ScoreDistributionDto.ScoreRange> rangeMap = Map.of(
                "0-39", new ScoreDistributionDto.ScoreRange(0, 39, "낙제"),
                "40-59", new ScoreDistributionDto.ScoreRange(40, 59, "미달"),
                "60-69", new ScoreDistributionDto.ScoreRange(60, 69, "보통"),
                "70-79", new ScoreDistributionDto.ScoreRange(70, 79, "양호"),
                "80-89", new ScoreDistributionDto.ScoreRange(80, 89, "우수"),
                "90-100", new ScoreDistributionDto.ScoreRange(90, 100, "최우수"));

        // Repository 결과를 맵으로 변환
        Map<String, Long> distributionMap = rawDistributions.stream()
                .collect(Collectors.toMap(
                        ExamResultRepository.ScoreRangeDistribution::getScoreRange,
                        ExamResultRepository.ScoreRangeDistribution::getStudentCount));

        // 모든 구간에 대해 분포 데이터 생성 (0인 구간도 포함)
        return rangeMap.entrySet().stream()
                .map(entry -> {
                    String rangeKey = entry.getKey();
                    ScoreDistributionDto.ScoreRange range = entry.getValue();
                    Long studentCount = distributionMap.getOrDefault(rangeKey, 0L);

                    return ScoreDistributionDto.ScoreDistribution.create(
                            range.min(),
                            range.max(),
                            studentCount,
                            totalStudentCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 학년별 시험별 평균 점수를 조회합니다
     * 최근 4개 시험의 평균 점수와 통계를 제공합니다
     * 
     * @param grade 학년 (1, 2, 3)
     * @param limit 조회할 최근 시험 개수 (기본값: 4)
     * @return 시험별 평균 점수 정보
     */
    public ExamAverageScoreDto getExamAverageScores(Integer grade, int limit) {
        log.info("학년별 시험별 평균 점수 조회 시작: grade={}, limit={}", grade, limit);

        // 1. 해당 학년의 전체 시험 개수 조회
        Long totalExamCount = examResultRepository.countExamsByGrade(grade);

        // 2. 학년별 최근 시험들의 평균 점수 통계 조회
        List<ExamResultRepository.ExamAverageStatistics> rawStatistics = examResultRepository
                .findExamAverageStatisticsByGrade(grade);

        if (rawStatistics.isEmpty()) {
            log.info("해당 학년의 시험 결과가 없음: grade={}", grade);
            return createEmptyExamAverageScore(grade, totalExamCount);
        }

        // 3. limit만큼 최근 시험들만 선택
        List<ExamResultRepository.ExamAverageStatistics> limitedStatistics = rawStatistics.stream()
                .limit(limit)
                .collect(Collectors.toList());

        log.info("학년별 시험별 평균 점수 계산: grade={}, totalExamCount={}, recentExamCount={}",
                grade, totalExamCount, limitedStatistics.size());

        // 4. 해당 학년의 전체 학생 수 조회 (제출률 계산용)
        Long totalStudentCount = examSubmissionRepository.countStudentsByGrade(grade);

        // 5. 각 시험별 상세 통계 생성
        List<ExamAverageScoreDto.ExamAverageScore> examAverageScores = limitedStatistics.stream()
                .map(stats -> {
                    // 해당 시험의 문제 수 조회
                    Long questionCount = examResultRepository.countQuestionsByExamId(stats.getExamId());

                    // 표준편차 계산 (Repository에서 복잡한 계산 대신 Java에서 계산)
                    Double standardDeviation = calculateExamStandardDeviation(
                            stats.getExamId(), stats.getAverageScore());

                    return ExamAverageScoreDto.ExamAverageScore.create(
                            stats.getExamId(),
                            stats.getExamName(),
                            stats.getCreatedAt(),
                            questionCount.intValue(),
                            stats.getParticipantCount(),
                            stats.getAverageScore(),
                            stats.getMaxScore(),
                            stats.getMinScore(),
                            standardDeviation,
                            totalStudentCount);
                })
                .collect(Collectors.toList());

        // 6. 응답 DTO 생성
        ExamAverageScoreDto result = ExamAverageScoreDto.create(
                grade,
                totalExamCount,
                examAverageScores);

        log.info("학년별 시험별 평균 점수 조회 완료: grade={}, examCount={}, overallAvg={}",
                grade, examAverageScores.size(), result.overallAverageScore());

        return result;
    }

    /**
     * 빈 시험별 평균 점수 응답 생성 (데이터가 없는 경우)
     */
    private ExamAverageScoreDto createEmptyExamAverageScore(Integer grade, Long totalExamCount) {
        return ExamAverageScoreDto.create(grade, totalExamCount, List.of());
    }

    /**
     * 특정 시험의 표준편차 계산
     */
    private Double calculateExamStandardDeviation(UUID examId, Double meanScore) {
        if (meanScore == null) {
            return 0.0;
        }

        try {
            // 해당 시험의 모든 점수 조회
            List<Double> scores = examResultRepository.findLatestResultsByExamId(examId,
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .map(result -> result.getTotalScore().doubleValue())
                    .collect(Collectors.toList());

            if (scores.size() <= 1) {
                return 0.0;
            }

            // 분산 계산
            double variance = scores.stream()
                    .mapToDouble(score -> Math.pow(score - meanScore, 2))
                    .average()
                    .orElse(0.0);

            // 표준편차 = 분산의 제곱근
            return Math.sqrt(variance);

        } catch (Exception e) {
            log.warn("표준편차 계산 실패: examId={}, error={}", examId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * 학년별 단원별 오답률 조회 (API 12)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 단원별 오답률 통계
     */
    public UnitWrongAnswerRateDto getUnitWrongAnswerRates(Integer grade) {
        log.info("단원별 오답률 조회 시작: grade={}", grade);

        try {
            // 단원별 오답률 통계 조회
            List<ExamResultRepository.UnitWrongAnswerStatistics> statistics = examResultRepository
                    .findUnitWrongAnswerStatisticsByGrade(grade);

            // 전체 통계 조회
            Long totalQuestionCount = examResultRepository.countTotalQuestionsByGrade(grade);
            Long totalSubmissionCount = examResultRepository.countTotalSubmissionsByGrade(grade);
            Long totalWrongAnswerCount = examResultRepository.countTotalWrongAnswersByGrade(grade);

            log.debug("단원별 오답률 기본 통계: grade={}, totalQuestions={}, totalSubmissions={}, totalWrong={}",
                    grade, totalQuestionCount, totalSubmissionCount, totalWrongAnswerCount);

            // 데이터가 없는 경우 빈 결과 반환
            if (statistics.isEmpty() || totalQuestionCount == 0 || totalSubmissionCount == 0) {
                log.info("단원별 오답률 데이터 없음: grade={}", grade);
                return UnitWrongAnswerRateDto.empty(grade);
            }

            // 단원별 통계를 DTO로 변환하고 순위 부여
            List<UnitWrongAnswerRateDto.UnitStatistic> unitStatistics = statistics.stream()
                    .filter(stat -> stat.getSubmissionCount() > 0) // 제출이 있는 단원만
                    .map(stat -> UnitWrongAnswerRateDto.UnitStatistic.create(
                            stat.getUnitId(),
                            stat.getUnitName(),
                            stat.getCategoryName(),
                            stat.getSubcategoryName(),
                            stat.getQuestionCount(),
                            stat.getSubmissionCount(),
                            stat.getWrongAnswerCount(),
                            0 // 순위는 아래에서 설정
                    ))
                    .sorted((a, b) -> b.wrongAnswerRate().compareTo(a.wrongAnswerRate())) // 오답률 높은 순으로 정렬
                    .collect(Collectors.toList());

            // 순위 부여 (오답률 기준 내림차순)
            List<UnitWrongAnswerRateDto.UnitStatistic> rankedStatistics = new ArrayList<>();
            for (int i = 0; i < unitStatistics.size(); i++) {
                UnitWrongAnswerRateDto.UnitStatistic original = unitStatistics.get(i);
                rankedStatistics.add(UnitWrongAnswerRateDto.UnitStatistic.create(
                        original.unitId(),
                        original.unitName(),
                        original.categoryName(),
                        original.subcategoryName(),
                        original.questionCount(),
                        original.submissionCount(),
                        original.wrongAnswerCount(),
                        i + 1 // 순위 (1부터 시작)
                ));
            }

            // 최종 DTO 생성
            UnitWrongAnswerRateDto result = UnitWrongAnswerRateDto.create(
                    grade,
                    totalQuestionCount,
                    totalSubmissionCount,
                    totalWrongAnswerCount,
                    rankedStatistics);

            log.info("단원별 오답률 조회 완료: grade={}, unitCount={}, overallWrongRate={}%",
                    grade, rankedStatistics.size(), result.overallWrongAnswerRate());

            return result;

        } catch (Exception e) {
            log.error("단원별 오답률 조회 실패: grade={}, error={}", grade, e.getMessage(), e);
            return UnitWrongAnswerRateDto.empty(grade);
        }
    }

    /**
     * 빈 단원별 오답률 응답 생성
     * 
     * @param grade 학년
     * @return 빈 단원별 오답률 DTO
     */
    private UnitWrongAnswerRateDto createEmptyUnitWrongAnswerRate(Integer grade) {
        return UnitWrongAnswerRateDto.empty(grade);
    }
}