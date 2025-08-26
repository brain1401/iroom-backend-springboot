package com.iroomclass.springbackend.domain.user.student.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.student.dto.StudentSubmissionHistoryResponse;
import com.iroomclass.springbackend.domain.user.student.dto.ExamResultDetailResponse;
import com.iroomclass.springbackend.domain.user.student.dto.QuestionResultResponse;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학생 결과 조회 서비스
 * 
 * 학생이 시험 결과를 조회할 수 있는 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentService {
    
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    
    /**
     * 학생별 시험 제출 이력 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 시험 제출 이력 목록
     */
    public StudentSubmissionHistoryResponse getSubmissionHistory(String studentName, String studentPhone) {
        log.info("학생 시험 제출 이력 조회: 이름={}, 전화번호={}", studentName, studentPhone);
        
        // 1단계: 학생 존재 확인
        long submissionCount = examSubmissionRepository.countByStudentNameAndStudentPhone(studentName, studentPhone);
        if (submissionCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 학생입니다. 시험 제출 이력이 없습니다.");
        }
        
        // 2단계: 시험 제출 이력 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByStudentNameAndStudentPhoneOrderBySubmittedAtDesc(
            studentName, studentPhone);
        
        // 3단계: 응답 데이터 구성
        List<StudentSubmissionHistoryResponse.SubmissionInfo> submissionInfos = submissions.stream()
            .map(this::convertToSubmissionInfo)
            .collect(Collectors.toList());
        
        log.info("학생 시험 제출 이력 조회 완료: 이름={}, 제출 수={}", studentName, submissionInfos.size());
        
        return StudentSubmissionHistoryResponse.builder()
            .studentName(studentName)
            .studentPhone(studentPhone)
            .submissions(submissionInfos)
            .totalCount(submissionInfos.size())
            .build();
    }
    
    /**
     * 시험별 상세 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 시험 상세 결과
     */
    public ExamResultDetailResponse getExamResult(Long submissionId, String studentName, String studentPhone) {
        log.info("시험 상세 결과 조회: 제출 ID={}, 학생={}, 전화번호={}", submissionId, studentName, studentPhone);
        
        // 1단계: 시험 제출 존재 확인 및 학생 본인 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        if (!submission.getStudentName().equals(studentName) || 
            !submission.getStudentPhone().equals(studentPhone)) {
            throw new IllegalArgumentException("본인의 시험 결과만 조회할 수 있습니다.");
        }
        
        // 2단계: 답안 목록 조회
        List<ExamAnswer> answers = examAnswerRepository.findByExamSubmissionId(submissionId);
        
        // 3단계: 응답 데이터 구성
        List<ExamResultDetailResponse.QuestionResult> questionResults = answers.stream()
            .map(this::convertToQuestionResult)
            .collect(Collectors.toList());
        
        int correctCount = (int) answers.stream().filter(answer -> answer.getIsCorrect()).count();
        int incorrectCount = answers.size() - correctCount;
        
        log.info("시험 상세 결과 조회 완료: 제출 ID={}, 학생={}, 총점={}, 정답 수={}", 
            submissionId, studentName, submission.getTotalScore(), correctCount);
        
        return ExamResultDetailResponse.builder()
            .submissionId(submission.getId())
            .examId(submission.getExam().getId())
            .examName(submission.getExam().getExamName())
            .grade(submission.getExam().getGrade() + "학년")
            .studentName(submission.getStudentName())
            .studentPhone(submission.getStudentPhone())
            .submittedAt(submission.getSubmittedAt())
            .totalScore(submission.getTotalScore())
            .totalQuestions(answers.size())
            .correctCount(correctCount)
            .incorrectCount(incorrectCount)
            .questionResults(questionResults)
            .build();
    }
    
    /**
     * 문제별 정답/오답, 점수, 단원, 난이도 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param questionId 문제 ID
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 문제별 결과 정보
     */
    public QuestionResultResponse getQuestionResult(Long submissionId, Long questionId, String studentName, String studentPhone) {
        log.info("문제별 결과 조회: 제출 ID={}, 문제 ID={}, 학생={}, 전화번호={}", 
            submissionId, questionId, studentName, studentPhone);
        
        // 1단계: 시험 제출 존재 확인 및 학생 본인 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        if (!submission.getStudentName().equals(studentName) || 
            !submission.getStudentPhone().equals(studentPhone)) {
            throw new IllegalArgumentException("본인의 시험 결과만 조회할 수 있습니다.");
        }
        
        // 2단계: 답안 조회
        ExamAnswer answer = examAnswerRepository.findByExamSubmissionIdAndQuestionId(submissionId, questionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답안입니다: 제출 ID=" + submissionId + ", 문제 ID=" + questionId));
        
        // 3단계: 문제 정보 조회
        Question question = answer.getQuestion();
        
        log.info("문제별 결과 조회 완료: 제출 ID={}, 문제 ID={}, 정답 여부={}, 점수={}", 
            submissionId, questionId, answer.getIsCorrect(), answer.getScore());
        
        return QuestionResultResponse.builder()
            .submissionId(submissionId)
            .questionId(questionId)
            .questionNumber(question.getId().intValue()) // TODO: 실제 문제 번호는 exam_draft_question의 seq_no를 사용해야 함
            .questionContent(question.getStem())
            .isCorrect(answer.getIsCorrect())
            .score(answer.getScore())
            .points(5) // TODO: 실제 배점은 exam_draft_question의 points를 사용해야 함
            .unitName(question.getUnit().getUnitName())
            .subcategoryName(question.getUnit().getSubcategory().getSubcategoryName())
            .categoryName(question.getUnit().getSubcategory().getCategory().getCategoryName())
            .difficulty(question.getDifficulty().name())
            .studentAnswer(answer.getAnswerText())
            .correctAnswer(question.getAnswerKey())
            .answerImageUrl(answer.getAnswerImageUrl())
            .build();
    }
    
    /**
     * ExamSubmission을 SubmissionInfo로 변환
     */
    private StudentSubmissionHistoryResponse.SubmissionInfo convertToSubmissionInfo(ExamSubmission submission) {
        List<ExamAnswer> answers = examAnswerRepository.findByExamSubmissionId(submission.getId());
        int correctCount = (int) answers.stream().filter(answer -> answer.getIsCorrect()).count();
        int incorrectCount = answers.size() - correctCount;
        double correctRate = answers.size() > 0 ? (double) correctCount / answers.size() * 100 : 0.0;
        
        return StudentSubmissionHistoryResponse.SubmissionInfo.builder()
            .submissionId(submission.getId())
            .examId(submission.getExam().getId())
            .examName(submission.getExam().getExamName())
            .grade(submission.getExam().getGrade() + "학년")
            .submittedAt(submission.getSubmittedAt())
            .totalScore(submission.getTotalScore())
            .totalQuestions(answers.size())
            .correctCount(correctCount)
            .incorrectCount(incorrectCount)
            .correctRate(Math.round(correctRate * 10.0) / 10.0) // 소수점 첫째자리까지
            .build();
    }
    
    /**
     * ExamAnswer를 QuestionResult로 변환
     */
    private ExamResultDetailResponse.QuestionResult convertToQuestionResult(ExamAnswer answer) {
        Question question = answer.getQuestion();
        
        return ExamResultDetailResponse.QuestionResult.builder()
            .questionId(question.getId())
            .questionNumber(question.getId().intValue()) // TODO: 실제 문제 번호는 exam_draft_question의 seq_no를 사용해야 함
            .isCorrect(answer.getIsCorrect())
            .score(answer.getScore())
            .points(5) // TODO: 실제 배점은 exam_draft_question의 points를 사용해야 함
            .unitName(question.getUnit().getUnitName())
            .difficulty(question.getDifficulty().name())
            .studentAnswer(answer.getAnswerText())
            .correctAnswer(question.getAnswerKey())
            .build();
    }
}
