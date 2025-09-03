package com.iroomclass.springbackend.domain.exam.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheetQuestion;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion.ScoringMethod;
import com.iroomclass.springbackend.domain.exam.repository.QuestionResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 문제별 채점 결과 서비스
 * 
 * 개별 문제에 대한 채점 결과 관련 비즈니스 로직을 처리합니다.
 * 자동 채점, 수동 채점, AI 보조 채점 등의 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuestionResultService {

    private final QuestionResultRepository questionResultRepository;
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;

    /**
     * 시험 제출에 대한 문제별 자동 채점 시작
     * 
     * @param examResult 시험 결과
     * @param submission 시험 제출
     */
    @Transactional
    public void startAutoGradingForSubmission(ExamResult examResult, ExamSubmission submission) {
        log.info("문제별 자동 채점 시작: examResultId={}, submissionId={}",
                examResult.getId(), submission.getId());

        List<StudentAnswerSheet> answers = studentAnswerSheetRepository
                .findBySubmissionIdOrderByQuestionOrder(submission.getId());

        for (StudentAnswerSheet answer : answers) {
            // 각 답안지의 문제별 답안에 대해 자동 채점 수행
            for (StudentAnswerSheetQuestion problem : answer.getStudentAnswerSheetQuestions()) {
                createAndProcessAutoGrading(examResult, answer, problem);
            }
        }

        log.info("문제별 자동 채점 완료: examResultId={}, 처리된 문제 수={}",
                examResult.getId(), answers.size());
    }

    /**
     * 수동 채점 준비
     * 
     * @param examResult 시험 결과
     * @param submission 시험 제출
     */
    @Transactional
    public void prepareManualGrading(ExamResult examResult, ExamSubmission submission) {
        log.info("수동 채점 준비: examResultId={}, submissionId={}",
                examResult.getId(), submission.getId());

        List<StudentAnswerSheet> answers = studentAnswerSheetRepository
                .findBySubmissionIdOrderByQuestionOrder(submission.getId());

        for (StudentAnswerSheet answer : answers) {
            // 각 답안지의 문제별 답안에 대해 ExamResultQuestion 생성
            for (StudentAnswerSheetQuestion problem : answer.getStudentAnswerSheetQuestions()) {
                ExamResultQuestion questionResult = ExamResultQuestion.builder()
                        .examResult(examResult)
                        .studentAnswerSheet(answer)
                        .question(problem.getQuestion()) // 문제별 Question 설정
                        .scoringMethod(ScoringMethod.MANUAL)
                        .build();

                questionResultRepository.save(questionResult);
                examResult.addQuestionResult(questionResult);
            }
        }

        log.info("수동 채점 준비 완료: examResultId={}, 준비된 문제 수={}",
                examResult.getId(), answers.size());
    }

    /**
     * 재채점 준비
     * 
     * @param newResult      새로운 시험 결과
     * @param originalResult 기존 시험 결과
     */
    @Transactional
    public void prepareRegrading(ExamResult newResult, ExamResult originalResult) {
        log.info("재채점 준비: newResultId={}, originalResultId={}",
                newResult.getId(), originalResult.getId());

        List<ExamResultQuestion> originalQuestionResults = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(originalResult.getId());

        for (ExamResultQuestion original : originalQuestionResults) {
            ExamResultQuestion newQuestionResult = ExamResultQuestion.builder()
                    .examResult(newResult)
                    .studentAnswerSheet(original.getStudentAnswerSheet())
                    .question(original.getQuestion()) // Question 명시적 설정
                    .scoringMethod(ScoringMethod.MANUAL) // 재채점은 기본적으로 수동

                    .build();

            questionResultRepository.save(newQuestionResult);
            newResult.addQuestionResult(newQuestionResult);
        }

        log.info("재채점 준비 완료: newResultId={}, 준비된 문제 수={}",
                newResult.getId(), originalQuestionResults.size());
    }

    /**
     * 재채점용 자동 채점 실행
     * 
     * @param examResult 시험 결과
     */
    @Transactional
    public void executeAutoGradingForRegrading(ExamResult examResult) {
        log.info("재채점 자동 채점 시작: examResultId={}", examResult.getId());

        List<ExamResultQuestion> questionResults = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(examResult.getId());

        for (ExamResultQuestion questionResult : questionResults) {
            StudentAnswerSheetQuestion problem = questionResult.getStudentAnswerSheet()
                    .getProblemByQuestionId(questionResult.getQuestion().getId());
            if (questionResult.getQuestion().isMultipleChoice()
                    && problem != null && problem.getSelectedChoice() != null) {
                // 객관식 문제에 대해 자동 채점 실행
                boolean success = questionResult.processAutoScoring();
                if (success) {
                    questionResultRepository.save(questionResult);
                    log.debug("재채점 자동 채점 완료: questionId={}, score={}",
                            questionResult.getQuestion().getId(), questionResult.getScore());
                }
            }
        }

        log.info("재채점 자동 채점 완료: examResultId={}, 처리된 문제 수={}",
                examResult.getId(), questionResults.size());
    }

    /**
     * 자동 채점 생성 및 처리
     * 
     * @param examResult 시험 결과
     * @param answer     학생 답안지
     */
    @Transactional
    protected void createAndProcessAutoGrading(ExamResult examResult, StudentAnswerSheet answer,
            StudentAnswerSheetQuestion problem) {
        ExamResultQuestion questionResult = ExamResultQuestion.builder()
                .examResult(examResult)
                .studentAnswerSheet(answer)
                .question(problem.getQuestion()) // 문제별 Question 설정
                .scoringMethod(ScoringMethod.AUTO)
                .confidenceScore(BigDecimal.ONE) // 자동 채점은 신뢰도 100%
                .build();

        // 자동 채점 로직 실행
        questionResult.processAutoScoring();

        questionResultRepository.save(questionResult);
        examResult.addQuestionResult(questionResult);

        log.debug("자동 채점 완료: questionId={}, score={}, isCorrect={}",
                problem.getQuestion().getId(), questionResult.getScore(), questionResult.getIsCorrect());
    }

    /**
     * 수동 채점 처리
     * 
     * @param resultId  문제별 결과 ID
     * @param score     채점 점수
     * @param isCorrect 정답 여부
     * @param feedback  피드백
     * @throws IllegalArgumentException 문제별 결과가 존재하지 않을 때
     */
    @Transactional
    public void processManualGrading(UUID resultId, Integer score, Boolean isCorrect, String feedback) {
        log.info("수동 채점 처리: resultId={}, score={}, isCorrect={}", resultId, score, isCorrect);

        ExamResultQuestion questionResult = questionResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("문제별 결과를 찾을 수 없습니다: " + resultId));

        if (questionResult.getScoringMethod() != ScoringMethod.MANUAL) {
            throw new IllegalStateException("수동 채점 대상이 아닙니다: " + resultId);
        }

        // 수동 채점 처리
        questionResult.processManualScoring(score, isCorrect, feedback);
        questionResultRepository.save(questionResult);

        log.info("수동 채점 완료: resultId={}, score={}", resultId, score);
    }

    /**
     * AI 보조 채점 처리
     * 
     * @param resultId   문제별 결과 ID
     * @param score      채점 점수
     * @param isCorrect  정답 여부
     * @param confidence 신뢰도
     * @param aiAnalysis AI 분석 결과
     * @throws IllegalArgumentException 문제별 결과가 존재하지 않을 때
     */
    @Transactional
    public void processAIAssistedGrading(UUID resultId, Integer score, Boolean isCorrect,
            BigDecimal confidence, String aiAnalysis) {
        log.info("AI 보조 채점 처리: resultId={}, score={}, confidence={}", resultId, score, confidence);

        ExamResultQuestion questionResult = questionResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("문제별 결과를 찾을 수 없습니다: " + resultId));

        // AI 보조 채점 처리
        questionResult.processAIAssistedScoring(score, isCorrect, confidence, aiAnalysis);
        questionResultRepository.save(questionResult);

        log.info("AI 보조 채점 완료: resultId={}, score={}, confidence={}", resultId, score, confidence);
    }

    /**
     * 시험 결과의 모든 문제 채점 완료 여부 확인
     * 
     * @param examResultId 시험 결과 ID
     * @return 모든 문제가 채점되었으면 true
     */
    public boolean isAllQuestionsGraded(UUID examResultId) {
        return questionResultRepository.isAllQuestionsGraded(examResultId);
    }

    /**
     * 시험 결과의 채점 진행률 계산
     * 
     * @param examResultId 시험 결과 ID
     * @return 채점 진행률 (0.0 ~ 1.0)
     */
    public Double calculateGradingProgress(UUID examResultId) {
        return questionResultRepository.calculateGradingProgress(examResultId);
    }

    /**
     * 시험 결과 ID로 문제별 결과 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 문제별 결과 목록
     */
    public List<ExamResultQuestion> findByExamResultId(UUID examResultId) {
        return questionResultRepository.findByExamResultIdOrderByQuestionOrder(examResultId);
    }

    /**
     * ID로 문제별 결과 조회
     * 
     * @param resultId 문제별 결과 ID
     * @return 문제별 결과
     */
    public ExamResultQuestion findById(UUID resultId) {
        return questionResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("문제별 결과를 찾을 수 없습니다: " + resultId));
    }

    /**
     * 특정 문제의 모든 채점 결과 조회
     * 
     * @param questionId 문제 ID
     * @param pageable   페이징 정보
     * @return 해당 문제의 채점 결과 페이지
     */
    public Page<ExamResultQuestion> findByQuestionId(UUID questionId, Pageable pageable) {
        return questionResultRepository.findByQuestionId(questionId, pageable);
    }

    /**
     * 채점 방법별 결과 조회
     * 
     * @param scoringMethod 채점 방법
     * @param pageable      페이징 정보
     * @return 해당 방법으로 채점된 결과 페이지
     */
    public Page<ExamResultQuestion> findByScoringMethod(ScoringMethod scoringMethod, Pageable pageable) {
        return questionResultRepository.findByScoringMethodOrderByCreatedAtDesc(scoringMethod, pageable);
    }

    /**
     * 수동 채점이 필요한 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return 수동 채점 대상 결과 페이지
     */
    public Page<ExamResultQuestion> findPendingManualGrading(Pageable pageable) {
        return questionResultRepository.findPendingManualGrading(pageable);
    }

    /**
     * 낮은 신뢰도의 AI 채점 결과 조회
     * 
     * @param confidenceThreshold 신뢰도 임계값
     * @param pageable            페이징 정보
     * @return 낮은 신뢰도 결과 페이지
     */
    public Page<ExamResultQuestion> findLowConfidenceAIResults(BigDecimal confidenceThreshold, Pageable pageable) {
        return questionResultRepository.findLowConfidenceAIResults(confidenceThreshold, pageable);
    }

    /**
     * 특정 문제의 정답률 계산
     * 
     * @param questionId 문제 ID
     * @return 정답률 (0.0 ~ 1.0)
     */
    public Double calculateCorrectRate(UUID questionId) {
        return questionResultRepository.calculateCorrectRateByQuestionId(questionId);
    }

    /**
     * 특정 문제의 평균 점수 계산
     * 
     * @param questionId 문제 ID
     * @return 평균 점수
     */
    public Double calculateAverageScore(UUID questionId) {
        return questionResultRepository.calculateAverageScoreByQuestionId(questionId);
    }

    /**
     * 시험 결과의 총점 계산
     * 
     * @param examResultId 시험 결과 ID
     * @return 총점
     */
    public Integer calculateTotalScore(UUID examResultId) {
        return questionResultRepository.calculateTotalScoreByExamResultId(examResultId);
    }

    /**
     * 채점 방법별 통계 조회
     * 
     * @param scoringMethod 채점 방법
     * @return 통계 정보 배열 [총 개수, 정답 개수, 평균 점수, 평균 신뢰도]
     */
    public Object[] getStatisticsByScoringMethod(ScoringMethod scoringMethod) {
        return questionResultRepository.getStatisticsByScoringMethod(scoringMethod);
    }

    /**
     * 문제별 결과 삭제
     * 
     * @param resultId 문제별 결과 ID
     * @throws IllegalArgumentException 문제별 결과가 존재하지 않을 때
     */
    @Transactional
    public void deleteResult(UUID resultId) {
        log.info("문제별 결과 삭제: resultId={}", resultId);

        if (!questionResultRepository.existsById(resultId)) {
            throw new IllegalArgumentException("문제별 결과를 찾을 수 없습니다: " + resultId);
        }

        questionResultRepository.deleteById(resultId);
        log.info("문제별 결과 삭제 완료: resultId={}", resultId);
    }

    /**
     * 시험 결과별 문제 결과 목록 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 문제 결과 응답 목록
     */
    public List<com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse> findQuestionResultResponsesByExamResultId(
            UUID examResultId) {
        log.info("시험 결과별 문제 결과 목록 조회: examResultId={}", examResultId);

        List<ExamResultQuestion> questionResults = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(examResultId);

        return questionResults.stream()
                .map(com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse::from)
                .toList();
    }

    /**
     * 문제 결과 단건 조회
     * 
     * @param questionResultId 문제 결과 ID
     * @return 문제 결과 응답
     * @throws IllegalArgumentException 문제 결과가 존재하지 않을 때
     */
    public com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse findResponseById(
            UUID questionResultId) {
        log.info("문제 결과 단건 조회: questionResultId={}", questionResultId);

        ExamResultQuestion questionResult = questionResultRepository.findById(questionResultId)
                .orElseThrow(() -> new IllegalArgumentException("문제 결과를 찾을 수 없습니다: " + questionResultId));

        return com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse.from(questionResult);
    }

    /**
     * 문제 결과 생성
     * 
     * @param examResultId 시험 결과 ID
     * @param request      문제 결과 생성 요청
     * @return 생성된 문제 결과 응답
     */
    @Transactional
    public com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse createQuestionResult(
            UUID examResultId,
            com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultCreateRequest request) {
        log.info("문제 결과 생성: examResultId={}, questionId={}", examResultId, request.questionId());

        // 기본적인 QuestionResult 생성 (실제로는 ExamResult와 StudentAnswerSheet 조회가 필요)
        ExamResultQuestion questionResult = ExamResultQuestion.builder()
                .isCorrect(request.isCorrect())
                .score(request.score())

                .scoringMethod(request.scoringMethod())
                .confidenceScore(request.confidenceScore())
                .scoringComment(request.feedback())

                .build();

        ExamResultQuestion savedResult = questionResultRepository.save(questionResult);

        return com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse.from(savedResult);
    }

    /**
     * 문제 결과 수정
     * 
     * @param questionResultId 문제 결과 ID
     * @param request          문제 결과 수정 요청
     * @return 수정된 문제 결과 응답
     */
    @Transactional
    public com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse updateQuestionResult(
            UUID questionResultId,
            com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultUpdateRequest request) {
        log.info("문제 결과 수정: questionResultId={}", questionResultId);

        ExamResultQuestion questionResult = questionResultRepository.findById(questionResultId)
                .orElseThrow(() -> new IllegalArgumentException("문제 결과를 찾을 수 없습니다: " + questionResultId));

        // 실제로는 Entity에 update 메서드가 필요하지만 임시로 저장
        ExamResultQuestion updatedResult = questionResultRepository.save(questionResult);

        return com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse.from(updatedResult);
    }

    /**
     * 문제 결과 삭제
     * 
     * @param questionResultId 문제 결과 ID
     */
    @Transactional
    public void deleteQuestionResult(UUID questionResultId) {
        deleteResult(questionResultId);
    }

    /**
     * 시험 결과와 문제 ID로 문제 결과 조회
     * 
     * @param examResultId 시험 결과 ID
     * @param questionId   문제 ID
     * @return 문제 결과 응답
     */
    public com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse findByExamResultIdAndQuestionId(
            UUID examResultId, UUID questionId) {
        log.info("시험 결과와 문제 ID로 문제 결과 조회: examResultId={}, questionId={}", examResultId, questionId);

        // 임시 구현 - 실제로는 적절한 Repository 메서드가 필요
        List<ExamResultQuestion> results = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(examResultId);
        ExamResultQuestion questionResult = results.stream()
                .filter(r -> r.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 문제의 결과를 찾을 수 없습니다"));

        return com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse.from(questionResult);
    }

    /**
     * 점수 통계 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 점수 통계
     */
    public ScoreStatistics getScoreStatistics(UUID examResultId) {
        log.info("점수 통계 조회: examResultId={}", examResultId);

        List<ExamResultQuestion> results = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(examResultId);

        if (results.isEmpty()) {
            return new ScoreStatistics(0, 0, 0.0, 0, 0);
        }

        int totalScore = results.stream().mapToInt(ExamResultQuestion::getScore).sum();
        int maxPossibleScore = results.stream().mapToInt(r -> r.getQuestion().getPoints()).sum();
        double averageScore = results.stream().mapToInt(ExamResultQuestion::getScore).average().orElse(0.0);
        int correctCount = (int) results.stream().filter(ExamResultQuestion::getIsCorrect).count();
        int totalCount = results.size();

        return new ScoreStatistics(totalScore, maxPossibleScore, averageScore, correctCount, totalCount);
    }

    /**
     * 오답 문제 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 오답 문제 응답 목록
     */
    public List<com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse> findIncorrectQuestionsByExamResult(
            UUID examResultId) {
        log.info("오답 문제 조회: examResultId={}", examResultId);

        List<ExamResultQuestion> results = questionResultRepository
                .findByExamResultIdOrderByQuestionOrder(examResultId);
        List<ExamResultQuestion> incorrectResults = results.stream()
                .filter(r -> !r.getIsCorrect())
                .toList();

        return incorrectResults.stream()
                .map(com.iroomclass.springbackend.domain.exam.dto.result.QuestionResultResponse::from)
                .toList();
    }

    /**
     * 점수 통계 클래스
     */
    public record ScoreStatistics(
            int totalScore,
            int maxPossibleScore,
            double averageScore,
            int correctCount,
            int totalCount) {
    }
}