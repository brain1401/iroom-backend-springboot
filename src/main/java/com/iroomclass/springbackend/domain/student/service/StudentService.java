package com.iroomclass.springbackend.domain.student.service;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.auth.dto.StudentUpsertRequest;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.dto.response.*;
import com.iroomclass.springbackend.domain.student.exception.StudentNotFoundException;
import com.iroomclass.springbackend.domain.student.repository.StudentExamResultRepository;
import com.iroomclass.springbackend.domain.student.repository.StudentExamSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheetQuestion;

/**
 * 학생 관련 비즈니스 로직 서비스
 * 
 * <p>학생 인증, 시험 제출 내역 조회, 시험 결과 조회 등의 기능을 제공합니다.
 * 모든 메서드는 3요소 인증(이름, 생년월일, 전화번호)을 기반으로 동작합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudentService {

    private final StudentAuthService studentAuthService;
    private final StudentRepository studentRepository;
    private final StudentExamSubmissionRepository studentExamSubmissionRepository;
    private final StudentExamResultRepository studentExamResultRepository;

    /**
     * 학생 로그인 인증 및 기본 정보 조회
     * 
     * @param request 학생 인증 요청 (이름, 생년월일, 전화번호)
     * @return 학생 로그인 응답 (ID, 이름)
     * @throws StudentNotFoundException 인증 정보가 일치하지 않을 때
     */
    public StudentLoginResponse login(StudentAuthRequest request) {
        log.info("학생 로그인 시도 (upsert): name={}, phone={}", request.name(), request.phone());
        
        // 기존 방식과 다르게 예외를 발생시키지 않고 upsert 수행
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        log.info("학생 로그인 성공 (upsert): studentId={}, name={}", student.getId(), student.getName());
        
        return StudentLoginResponse.from(student);
    }
    
    /**
     * 학생 로그인 (StudentUpsertRequest 버전)
     * 
     * @param request 학생 Upsert 요청 (이름, 생년월일, 전화번호)
     * @return 학생 로그인 응답 (ID, 이름)
     */
    public StudentLoginResponse loginWithUpsert(StudentUpsertRequest request) {
        log.info("학생 로그인 시도 (upsert): name={}, phone={}", request.name(), request.phone());
        
        Student student = studentAuthService.upsertStudent(request);
        
        log.info("학생 로그인 성공 (upsert): studentId={}, name={}", student.getId(), student.getName());
        
        return StudentLoginResponse.from(student);
    }

    /**
     * 최근 시험 제출 내역 조회 (페이징)
     * 
     * @param request 학생 인증 요청
     * @param pageable 페이징 정보
     * @return 최근 제출 내역 페이지
     * @throws StudentNotFoundException 인증 정보가 일치하지 않을 때
     */
    public Page<RecentSubmissionDto> getRecentSubmissions(StudentAuthRequest request, Pageable pageable) {
        log.info("최근 시험 제출 내역 조회 (upsert): name={}, page={}, size={}", 
                request.name(), pageable.getPageNumber(), pageable.getPageSize());
        
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        Page<RecentSubmissionDto> submissions = studentExamSubmissionRepository
                .findRecentSubmissionsByStudentId(student.getId(), pageable);
        
        log.info("최근 시험 제출 내역 조회 완료: studentId={}, totalElements={}", 
                student.getId(), submissions.getTotalElements());
        
        return submissions;
    }
    
    /**
     * 최근 시험 제출 내역 조회 (StudentUpsertRequest 버전)
     */
    @Transactional(readOnly = false)  // 명시적으로 readOnly=false 설정 (학생 upsert 포함)
    public Page<RecentSubmissionDto> getRecentSubmissionsWithUpsert(StudentUpsertRequest request, Pageable pageable) {
        log.info("최근 시험 제출 내역 조회 (upsert): name={}, page={}, size={}", 
                request.name(), pageable.getPageNumber(), pageable.getPageSize());
        
        Student student = studentAuthService.upsertStudent(request);
        
        Page<RecentSubmissionDto> submissions = studentExamSubmissionRepository
                .findRecentSubmissionsByStudentId(student.getId(), pageable);
        
        log.info("최근 시험 제출 내역 조회 완료: studentId={}, totalElements={}", 
                student.getId(), submissions.getTotalElements());
        
        return submissions;
    }

    /**
     * 시험 결과 요약 목록 조회 (페이징)
     * 
     * @param request 학생 인증 요청
     * @param pageable 페이징 정보
     * @return 시험 결과 요약 페이지
     * @throws StudentNotFoundException 인증 정보가 일치하지 않을 때
     */
    public Page<ExamResultSummaryDto> getExamResultsSummary(StudentAuthRequest request, Pageable pageable) {
        log.info("시험 결과 요약 조회 (upsert): name={}, page={}, size={}", 
                request.name(), pageable.getPageNumber(), pageable.getPageSize());
        
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        Page<ExamResultSummaryDto> results = studentExamResultRepository
                .findExamResultsSummaryByStudentId(student.getId(), pageable);
        
        log.info("시험 결과 요약 조회 완료: studentId={}, totalElements={}", 
                student.getId(), results.getTotalElements());
        
        return results;
    }
    
    /**
     * 시험 결과 요약 목록 조회 (StudentUpsertRequest 버전)
     */
    public Page<ExamResultSummaryDto> getExamResultsSummaryWithUpsert(StudentUpsertRequest request, Pageable pageable) {
        log.info("시험 결과 요약 조회 (upsert): name={}, page={}, size={}", 
                request.name(), pageable.getPageNumber(), pageable.getPageSize());
        
        Student student = studentAuthService.upsertStudent(request);
        
        Page<ExamResultSummaryDto> results = studentExamResultRepository
                .findExamResultsSummaryByStudentId(student.getId(), pageable);
        
        log.info("시험 결과 요약 조회 완료: studentId={}, totalElements={}", 
                student.getId(), results.getTotalElements());
        
        return results;
    }

    /**
     * 특정 시험의 상세 결과 조회
     * 
     * @param request 학생 인증 요청
     * @param examId 시험 ID
     * @return 시험 상세 결과
     * @throws StudentNotFoundException 인증 정보가 일치하지 않거나 시험 결과가 없을 때
     */
    public ExamDetailResultDto getExamDetailResult(StudentAuthRequest request, UUID examId) {
        log.info("시험 상세 결과 조회 (upsert): name={}, examId={}", request.name(), examId);
        
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        ExamResult examResult = studentExamResultRepository
                .findDetailedExamResultByStudentIdAndExamId(student.getId(), examId)
                .orElseThrow(() -> {
                    log.warn("시험 결과를 찾을 수 없음: studentId={}, examId={}", student.getId(), examId);
                    return new StudentNotFoundException("해당 시험의 결과를 찾을 수 없습니다");
                });

        // ExamResult를 ExamDetailResultDto로 변환
        ExamDetailResultDto result = convertToExamDetailResultDto(examResult);
        
        log.info("시험 상세 결과 조회 완료: studentId={}, examId={}, totalScore={}", 
                student.getId(), examId, result.totalScore());
        
        return result;
    }
    
    /**
     * 특정 시험의 상세 결과 조회 (StudentUpsertRequest 버전)
     */
    public ExamDetailResultDto getExamDetailResultWithUpsert(StudentUpsertRequest request, UUID examId) {
        log.info("시험 상세 결과 조회 (upsert): name={}, examId={}", request.name(), examId);
        
        Student student = studentAuthService.upsertStudent(request);
        
        ExamResult examResult = studentExamResultRepository
                .findDetailedExamResultByStudentIdAndExamId(student.getId(), examId)
                .orElseThrow(() -> {
                    log.warn("시험 결과를 찾을 수 없음: studentId={}, examId={}", student.getId(), examId);
                    return new StudentNotFoundException("해당 시험의 결과를 찾을 수 없습니다");
                });

        ExamDetailResultDto result = convertToExamDetailResultDto(examResult);
        
        log.info("시험 상세 결과 조회 완료: studentId={}, examId={}, totalScore={}", 
                student.getId(), examId, result.totalScore());
        
        return result;
    }

    /**
     * 학생 정보 조회 (최신 학년 정보 포함)
     * 
     * @param request 학생 인증 요청
     * @return 학생 정보
     * @throws StudentNotFoundException 인증 정보가 일치하지 않을 때
     */
    public StudentInfoDto getStudentInfo(StudentAuthRequest request) {
        log.info("학생 정보 조회 (upsert): name={}, phone={}", request.name(), request.phone());
        
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        // 최신 응시한 시험의 학년 정보 조회
        Integer latestGrade = studentExamSubmissionRepository
                .findLatestGradeByStudentId(student.getId());
        
        StudentInfoDto studentInfo = StudentInfoDto.of(
                student.getName(), 
                student.getPhone(), 
                student.getBirthDate(), 
                latestGrade);
        
        log.info("학생 정보 조회 완료: studentId={}, grade={}", student.getId(), latestGrade);
        
        return studentInfo;
    }
    
    /**
     * 학생 정보 조회 (StudentUpsertRequest 버전)
     */
    public StudentInfoDto getStudentInfoWithUpsert(StudentUpsertRequest request) {
        log.info("학생 정보 조회 (upsert): name={}, phone={}", request.name(), request.phone());
        
        Student student = studentAuthService.upsertStudent(request);
        
        // 최신 응시한 시험의 학년 정보 조회
        Integer latestGrade = studentExamSubmissionRepository
                .findLatestGradeByStudentId(student.getId());
        
        StudentInfoDto studentInfo = StudentInfoDto.of(
                student.getName(), 
                student.getPhone(), 
                student.getBirthDate(), 
                latestGrade);
        
        log.info("학생 정보 조회 완료: studentId={}, grade={}", student.getId(), latestGrade);
        
        return studentInfo;
    }

    /**
     * 학생 로그아웃
     * 
     * <p>현재는 상태를 저장하지 않으므로 단순히 성공 응답만 반환합니다.
     * 향후 세션 관리나 토큰 무효화 로직이 추가될 수 있습니다.</p>
     * 
     * @param request 학생 인증 요청
     * @throws StudentNotFoundException 인증 정보가 일치하지 않을 때
     */
    public void logout(StudentAuthRequest request) {
        log.info("학생 로그아웃 (upsert): name={}, phone={}", request.name(), request.phone());
        
        // 인증 확인만 수행 (현재는 별도 로그아웃 처리 없음)
        // upsert 패턴 사용으로 학생이 없어도 에러가 발생하지 않음
        Student student = studentAuthService.upsertStudentFromAuth(request);
        
        log.info("학생 로그아웃 완료: studentId={}", student.getId());
    }
    
    /**
     * 학생 로그아웃 (StudentUpsertRequest 버전)
     */
    public void logoutWithUpsert(StudentUpsertRequest request) {
        log.info("학생 로그아웃 (upsert): name={}, phone={}", request.name(), request.phone());
        
        Student student = studentAuthService.upsertStudent(request);
        
        log.info("학생 로그아웃 완료: studentId={}", student.getId());
    }

    /**
     * 학생의 전체 시험 응시 횟수 조회 (통계 목적)
     * 
     * @param studentId 학생 ID
     * @return 전체 응시 횟수
     */
    public long getTotalSubmissionCount(Long studentId) {
        long count = studentExamSubmissionRepository.countSubmissionsByStudentId(studentId);
        log.debug("학생 전체 응시 횟수: studentId={}, count={}", studentId, count);
        return count;
    }

    /**
     * 학생의 평균 점수 조회 (통계 목적)
     * 
     * @param studentId 학생 ID
     * @return 평균 점수 (응시한 시험이 없으면 null)
     */
    public Double getAverageScore(Long studentId) {
        Double average = studentExamResultRepository.findAverageScoreByStudentId(studentId);
        log.debug("학생 평균 점수: studentId={}, average={}", studentId, average);
        return average;
    }

    /**
     * ExamResult를 ExamDetailResultDto로 변환
     * 
     * @param examResult 시험 결과 엔티티
     * @return 상세 시험 결과 DTO
     */
    private ExamDetailResultDto convertToExamDetailResultDto(ExamResult examResult) {
        // 기본 정보 추출
        String examName = examResult.getExamSubmission().getExam().getExamName();
        LocalDateTime gradedAt = examResult.getGradedAt();
        Integer totalScore = examResult.getTotalScore();
        
        // 문제 결과에서 통계 계산
        int totalQuestions = examResult.getQuestionResults().size();
        int objectiveCount = 0;
        int subjectiveCount = 0;
        
        // 단원 정보 및 문제별 답안 수집
        Set<ExamDetailResultDto.UnitInfo> unitSet = new HashSet<>();
        List<ExamDetailResultDto.QuestionAnswer> questionAnswers = new ArrayList<>();
        
        int questionNumber = 1;
        for (ExamResultQuestion questionResult : examResult.getQuestionResults()) {
            Question question = questionResult.getQuestion();
            
            // 문제 유형별 카운트
            if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE) {
                objectiveCount++;
            } else {
                subjectiveCount++;
            }
            
            // 단원 정보 수집
            unitSet.add(new ExamDetailResultDto.UnitInfo(
                    question.getUnit().getId(),
                    question.getUnit().getUnitName()
            ));
            
            // 문제별 답안 정보 
            String studentAnswer = "";
            StudentAnswerSheetQuestion answerSheetQuestion = questionResult.getStudentAnswerSheet().getProblemByQuestionId(question.getId());
            if (answerSheetQuestion != null) {
                studentAnswer = answerSheetQuestion.getAnswerContent();
            }
            
            questionAnswers.add(new ExamDetailResultDto.QuestionAnswer(
                    questionNumber++,
                    question.getQuestionText(),
                    studentAnswer != null ? studentAnswer : "",
                    question.getQuestionType().name()
            ));
        }
        
        List<ExamDetailResultDto.UnitInfo> units = new ArrayList<>(unitSet);
        
        return ExamDetailResultDto.of(
                examName,
                gradedAt,
                totalQuestions,
                objectiveCount,
                subjectiveCount,
                totalScore,
                units,
                questionAnswers
        );
    }
}