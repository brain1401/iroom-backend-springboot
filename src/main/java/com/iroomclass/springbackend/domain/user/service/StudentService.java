package com.iroomclass.springbackend.domain.user.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.student.dto.StudentSubmissionHistoryResponse;
import com.iroomclass.springbackend.domain.user.student.dto.ExamResultDetailResponse;
import com.iroomclass.springbackend.domain.user.student.dto.QuestionResultResponse;
import com.iroomclass.springbackend.domain.user.student.dto.StudentProfileResponse;
import com.iroomclass.springbackend.domain.user.student.dto.RecentExamSubmissionsResponse;
import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.repository.UserRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetQuestionRepository;

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
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;
    private final QuestionRepository questionRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final UserRepository userRepository;
    
    /**
     * 학생 프로필 조회 (3-factor 인증)
     * 이름, 전화번호, 생년월일로 본인 확인 후 기본 정보 반환
     * 
     * @param name 학생 이름
     * @param phone 학생 전화번호  
     * @param birthDate 학생 생년월일
     * @return 학생 기본 정보
     */
    public StudentProfileResponse getProfile(String name, String phone, LocalDate birthDate) {
        log.info("학생 프로필 조회 요청: 이름={}, 전화번호={}, 생년월일={}", name, phone, birthDate);
        
        // 3-factor 인증: 이름 + 전화번호 + 생년월일
        User user = userRepository.findByNameAndPhoneAndBirthDate(name, phone, birthDate)
            .orElseThrow(() -> new IllegalArgumentException(
                "이름, 전화번호, 생년월일이 일치하지 않습니다."));
        
        log.info("학생 프로필 조회 성공: ID={}, 이름={}, 학년={}", user.getId(), user.getName(), user.getGrade());
        
        return new StudentProfileResponse(
            user.getName(),
            user.getPhone(),
            user.getBirthDate(),
            user.getGrade()
        );
    }
    
    /**
     * 학생 최근 시험 3건 조회 (메인화면)
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호  
     * @return 최근 시험 3건 목록
     */
    public RecentExamSubmissionsResponse getRecentExamSubmissions(String studentName, String studentPhone) {
        log.info("학생 최근 시험 3건 조회: 이름={}, 전화번호={}", studentName, studentPhone);
        
        // 1단계: 학생 존재 확인
        long submissionCount = examSubmissionRepository.countByUserNameAndUserPhone(studentName, studentPhone);
        if (submissionCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 학생입니다. 시험 제출 이력이 없습니다.");
        }
        
        // 2단계: 최근 3건 제출 이력 조회
        List<ExamSubmission> recentSubmissions = examSubmissionRepository
            .findTop3ByUserNameAndUserPhoneOrderBySubmittedAtDesc(studentName, studentPhone);
        
        // 3단계: 응답 데이터 구성
        List<RecentExamSubmissionsResponse.RecentExamInfo> recentExamInfos = recentSubmissions.stream()
            .map(this::convertToRecentExamInfo)
            .collect(Collectors.toList());
        
        log.info("학생 최근 시험 3건 조회 완료: 이름={}, 조회된 건수={}", studentName, recentExamInfos.size());
        
        return new RecentExamSubmissionsResponse(
            studentName,
            studentPhone,
            recentExamInfos
        );
    }
    
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
        long submissionCount = examSubmissionRepository.countByUserNameAndUserPhone(studentName, studentPhone);
        if (submissionCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 학생입니다. 시험 제출 이력이 없습니다.");
        }
        
        // 2단계: 시험 제출 이력 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByUserNameAndUserPhoneOrderBySubmittedAtDesc(
            studentName, studentPhone);
        
        // 3단계: 응답 데이터 구성
        List<StudentSubmissionHistoryResponse.SubmissionInfo> submissionInfos = submissions.stream()
            .map(this::convertToSubmissionInfo)
            .collect(Collectors.toList());
        
        log.info("학생 시험 제출 이력 조회 완료: 이름={}, 제출 수={}", studentName, submissionInfos.size());
        
        return new StudentSubmissionHistoryResponse(
            studentName,
            studentPhone,
            submissionInfos,
            submissionInfos.size()
        );
    }
    
    /**
     * 시험별 상세 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 시험 상세 결과
     */
    public ExamResultDetailResponse getExamResult(UUID submissionId, String studentName, String studentPhone) {
        log.info("시험 상세 결과 조회: 제출 ID={}, 학생={}, 전화번호={}", submissionId, studentName, studentPhone);
        
        // 1단계: 시험 제출 존재 확인 및 학생 본인 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        if (!submission.getUser().getName().equals(studentName) || 
            !submission.getUser().getPhone().equals(studentPhone)) {
            throw new IllegalArgumentException("본인의 시험 결과만 조회할 수 있습니다.");
        }
        
        // 2단계: 답안 목록 조회
        List<StudentAnswerSheet> answers = studentAnswerSheetRepository.findByExamSubmissionId(submissionId);
        
        // 3단계: 응답 데이터 구성
        List<ExamResultDetailResponse.QuestionResult> questionResults = answers.stream()
            .map(this::convertToQuestionResult)
            .collect(Collectors.toList());
        
        // TODO: 정답 개수는 QuestionResultService를 통해 조회해야 함
        int correctCount = 0; // QuestionResult에서 조회 필요
        int incorrectCount = answers.size() - correctCount;
        
        // 문제 타입별 개수 계산
        int multipleChoiceCount = (int) answers.stream()
            .filter(answer -> answer.getQuestion().isMultipleChoice())
            .count();
        int subjectiveCount = answers.size() - multipleChoiceCount;
        
        // 단원명 목록 생성 (중복 제거)
        String unitNames = answers.stream()
            .map(answer -> answer.getQuestion().getUnit().getUnitName())
            .distinct()
            .collect(Collectors.joining(", "));
        
        log.info("시험 상세 결과 조회 완료: 제출 ID={}, 학생={}, 총점={}, 정답 수={}", 
            submissionId, studentName, submission.getTotalScore(), correctCount);
        
        return new ExamResultDetailResponse(
            submission.getId(),
            submission.getExam().getId(),
            submission.getExam().getExamName(),
            submission.getExam().getGrade() + "학년",
            submission.getUser().getName(),
            submission.getUser().getPhone(),
            submission.getSubmittedAt(),
            submission.getTotalScore(),
            answers.size(),
            multipleChoiceCount,
            subjectiveCount,
            unitNames,
            correctCount,
            incorrectCount,
            questionResults
        );
    }
    
    /**
     * ExamSheetQuestion 조회 (문제 번호와 배점 정보)
     */
    private ExamSheetQuestion getExamSheetQuestion(UUID examSheetId, UUID questionId) {
        return examSheetQuestionRepository.findByExamSheetIdAndQuestionId(examSheetId, questionId)
            .orElse(null);
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
    public QuestionResultResponse getQuestionResult(UUID submissionId, UUID questionId, String studentName, String studentPhone) {
        log.info("문제별 결과 조회: 제출 ID={}, 문제 ID={}, 학생={}, 전화번호={}", 
            submissionId, questionId, studentName, studentPhone);
        
        // 1단계: 시험 제출 존재 확인 및 학생 본인 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        if (!submission.getUser().getName().equals(studentName) || 
            !submission.getUser().getPhone().equals(studentPhone)) {
            throw new IllegalArgumentException("본인의 시험 결과만 조회할 수 있습니다.");
        }
        
        // 2단계: 답안 조회
        StudentAnswerSheet answer = studentAnswerSheetRepository.findByExamSubmissionIdAndQuestionId(submissionId, questionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답안입니다: 제출 ID=" + submissionId + ", 문제 ID=" + questionId));
        
        // 3단계: 문제 정보 조회
        Question question = answer.getQuestion();
        
        // 4단계: ExamSheetQuestion 조회 (문제 번호와 배점)
        ExamSheetQuestion examSheetQuestion = getExamSheetQuestion(
            submission.getExam().getExamSheet().getId(), questionId);
        
        log.info("문제별 결과 조회 완료: 제출 ID={}, 문제 ID={}, 정답 여부={}, 점수={}", 
            submissionId, questionId, null, 0); // TODO: QuestionResult에서 조회
        
        return new QuestionResultResponse(
            submissionId,
            questionId,
            examSheetQuestion != null ? examSheetQuestion.getSeqNo() : 0,
            question.getQuestionTextAsHtml(),
            null, // TODO: QuestionResult에서 조회
            0, // TODO: QuestionResult에서 조회
            examSheetQuestion != null ? examSheetQuestion.getPoints() : 0,
            question.getUnit().getUnitName(),
            question.getUnit().getSubcategory().getSubcategoryName(),
            question.getUnit().getSubcategory().getCategory().getCategoryName(),
            question.getDifficulty().name(),
            answer.getAnswerText(),
            question.getAnswerText(),
            answer.getAnswerImageUrl()
        );
    }
    
    /**
     * ExamSubmission을 RecentExamInfo로 변환 (최근 시험 목록용)
     */
    private RecentExamSubmissionsResponse.RecentExamInfo convertToRecentExamInfo(ExamSubmission submission) {
        List<StudentAnswerSheet> answers = studentAnswerSheetRepository.findByExamSubmissionId(submission.getId());
        // TODO: 정답 개수는 QuestionResultService를 통해 조회해야 함
        int correctCount = 0; // QuestionResult에서 조회 필요
        double correctRate = answers.size() > 0 ? (double) correctCount / answers.size() * 100 : 0.0;
        
        // 단원명 목록 가져오기 (중복 제거)
        String unitNames = answers.stream()
            .map(answer -> answer.getQuestion().getUnit().getUnitName())
            .distinct()
            .collect(Collectors.joining(", "));
        
        return new RecentExamSubmissionsResponse.RecentExamInfo(
            submission.getId(),
            submission.getExam().getId(),
            submission.getExam().getExamName(),
            answers.size(),
            unitNames,
            submission.getSubmittedAt(),
            submission.getTotalScore(),
            Math.round(correctRate * 10.0) / 10.0 // 소수점 첫째자리까지
        );
    }
    
    /**
     * ExamSubmission을 SubmissionInfo로 변환
     */
    private StudentSubmissionHistoryResponse.SubmissionInfo convertToSubmissionInfo(ExamSubmission submission) {
        List<StudentAnswerSheet> answers = studentAnswerSheetRepository.findByExamSubmissionId(submission.getId());
        // TODO: 정답 개수는 QuestionResultService를 통해 조회해야 함
        int correctCount = 0; // QuestionResult에서 조회 필요
        int incorrectCount = answers.size() - correctCount;
        double correctRate = answers.size() > 0 ? (double) correctCount / answers.size() * 100 : 0.0;
        
        return new StudentSubmissionHistoryResponse.SubmissionInfo(
            submission.getId(),
            submission.getExam().getId(),
            submission.getExam().getExamName(),
            submission.getExam().getGrade() + "학년",
            submission.getSubmittedAt(),
            submission.getTotalScore(),
            answers.size(),
            correctCount,
            incorrectCount,
            Math.round(correctRate * 10.0) / 10.0 // 소수점 첫째자리까지
        );
    }
    
    /**
     * StudentAnswerSheet를 QuestionResult로 변환
     */
    private ExamResultDetailResponse.QuestionResult convertToQuestionResult(StudentAnswerSheet answer) {
        Question question = answer.getQuestion();
        
        // ExamSheetQuestion 조회 (문제 번호와 배점)
        ExamSheetQuestion examSheetQuestion = getExamSheetQuestion(
            answer.getExamSubmission().getExam().getExamSheet().getId(), question.getId());
        
        return new ExamResultDetailResponse.QuestionResult(
            question.getId(),
            examSheetQuestion != null ? examSheetQuestion.getSeqNo() : 0,
            null, // TODO: QuestionResult에서 조회
            0, // TODO: QuestionResult에서 조회
            examSheetQuestion != null ? examSheetQuestion.getPoints() : 0,
            question.getUnit().getUnitName(),
            question.getDifficulty().name(),
            answer.getAnswerText(),
            question.getAnswerText()
        );
    }
}
