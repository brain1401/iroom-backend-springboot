package com.iroomclass.springbackend.domain.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.iroomclass.springbackend.domain.exam.dto.SubmitAndGradeRequest;
import com.iroomclass.springbackend.domain.exam.dto.SubmitAndGradeResponse;
import com.iroomclass.springbackend.domain.exam.dto.GradingResultDto;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheetQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.common.exception.EntityNotFoundException;
import com.iroomclass.springbackend.common.exception.InvalidRequestException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 시험 제출 및 AI 채점 서비스
 * 
 * <p>학생의 시험 답안을 제출하고 AI 서버를 통해 채점을 수행하는 서비스입니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamGradingService {
    
    private final WebClient aiServerWebClient;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final StudentRepository studentRepository;
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;
    private final QuestionRepository questionRepository;
    
    @Value("${ai.server.grading.timeout:30}")
    private int gradingTimeout;
    
    /**
     * 시험 답안 제출 및 채점 처리
     * 
     * @param request 제출 및 채점 요청
     * @return 제출 및 채점 결과
     */
    @Transactional
    public SubmitAndGradeResponse submitAndGrade(SubmitAndGradeRequest request) {
        log.info("시험 답안 제출 및 채점 시작: examId={}, studentId={}", 
                request.examId(), request.studentId());
        
        // 1. 기본 검증
        Exam exam = examRepository.findById(request.examId())
            .orElseThrow(() -> new EntityNotFoundException("시험을 찾을 수 없습니다: " + request.examId()));
        
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new EntityNotFoundException("학생을 찾을 수 없습니다: " + request.studentId()));
        
        // 2. 중복 제출 확인
        boolean alreadySubmitted = examSubmissionRepository
            .existsByExamIdAndStudentId(exam.getId(), student.getId());
        
        if (alreadySubmitted) {
            throw new InvalidRequestException("이미 제출한 시험입니다");
        }
        
        // 3. 제출 데이터 저장
        LocalDateTime submittedAt = LocalDateTime.now();
        ExamSubmission submission = saveSubmission(exam, student, submittedAt);
        StudentAnswerSheet answerSheet = saveAnswerSheet(submission, request.answers());
        
        // 4. AI 서버로 채점 요청 (force_grading이 true인 경우)
        GradingResultDto gradingResult = null;
        if (Boolean.TRUE.equals(request.forceGrading())) {
            try {
                gradingResult = requestGrading(request, submission.getId(), exam.getExamSheet().getId());
            } catch (Exception e) {
                log.error("AI 채점 실패, 제출은 성공: {}", e.getMessage());
                // 채점 실패해도 제출은 성공으로 처리
                return SubmitAndGradeResponse.submittedOnly(
                    submission.getId(),
                    exam.getExamSheet().getId(),
                    answerSheet.getId(),
                    submittedAt
                );
            }
        }
        
        // 5. 응답 생성
        if (gradingResult != null) {
            return SubmitAndGradeResponse.success(
                submission.getId(),
                exam.getExamSheet().getId(),
                answerSheet.getId(),
                gradingResult,
                submittedAt
            );
        } else {
            return SubmitAndGradeResponse.submittedOnly(
                submission.getId(),
                exam.getExamSheet().getId(),
                answerSheet.getId(),
                submittedAt
            );
        }
    }
    
    /**
     * ExamSubmission 저장
     */
    private ExamSubmission saveSubmission(Exam exam, Student student, LocalDateTime submittedAt) {
        ExamSubmission submission = ExamSubmission.builder()
            .exam(exam)
            .student(student)
            .submittedAt(submittedAt)
            .build();
        
        return examSubmissionRepository.save(submission);
    }
    
    /**
     * StudentAnswerSheet 및 StudentAnswerSheetQuestion 저장
     */
    private StudentAnswerSheet saveAnswerSheet(ExamSubmission submission, List<SubmitAndGradeRequest.AnswerDto> answers) {
        // StudentAnswerSheet 생성
        StudentAnswerSheet answerSheet = StudentAnswerSheet.builder()
            .examSubmission(submission)
            .studentName("학생") // TODO: 실제 학생 이름으로 변경 필요
            .build();
        
        answerSheet = studentAnswerSheetRepository.save(answerSheet);
        
        // StudentAnswerSheetQuestion 생성
        final StudentAnswerSheet finalAnswerSheet = answerSheet;
        for (SubmitAndGradeRequest.AnswerDto answer : answers) {
            // Question 엔티티 조회
            Question question = questionRepository.findById(answer.questionId())
                .orElseThrow(() -> new EntityNotFoundException("문제를 찾을 수 없습니다: " + answer.questionId()));
            
            // StudentAnswerSheetQuestion 생성
            StudentAnswerSheetQuestion answerQuestion = StudentAnswerSheetQuestion.builder()
                .studentAnswerSheet(finalAnswerSheet)
                .question(question)
                .selectedChoice(answer.selectedChoice())
                .answerText(answer.answerText())
                .build();
            
            // 양방향 관계 설정
            finalAnswerSheet.addProblem(answerQuestion);
        }
        
        return studentAnswerSheetRepository.save(answerSheet);
    }
    
    /**
     * AI 서버로 채점 요청
     */
    private GradingResultDto requestGrading(SubmitAndGradeRequest request, UUID submissionId, UUID examSheetId) {
        log.info("AI 서버로 채점 요청: examId={}, studentId={}", request.examId(), request.studentId());
        
        // AI 서버 요청 데이터 구성
        Map<String, Object> aiRequest = buildAiServerRequest(request);
        
        // AI 서버 호출
        Mono<GradingResultDto> responseMono = aiServerWebClient
            .post()
            .uri("/grading/submit-and-grade")
            .bodyValue(aiRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::parseGradingResponse)
            .timeout(Duration.ofSeconds(gradingTimeout))
            .doOnSuccess(result -> log.info("AI 채점 성공: totalScore={}", result.totalScore()))
            .doOnError(error -> log.error("AI 채점 실패: {}", error.getMessage()));
        
        // 동기 방식으로 결과 대기
        return responseMono.block();
    }
    
    /**
     * AI 서버 요청 데이터 구성
     */
    private Map<String, Object> buildAiServerRequest(SubmitAndGradeRequest request) {
        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("exam_id", request.examId().toString());
        aiRequest.put("student_id", request.studentId());
        aiRequest.put("force_grading", request.forceGrading());
        
        // 답안 변환
        List<Map<String, Object>> answers = request.answers().stream()
            .map(answer -> {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("question_id", answer.questionId().toString());
                if (answer.selectedChoice() != null) {
                    answerMap.put("selected_choice", answer.selectedChoice());
                }
                if (answer.answerText() != null) {
                    answerMap.put("answer_text", answer.answerText());
                }
                return answerMap;
            })
            .collect(Collectors.toList());
        
        aiRequest.put("answers", answers);
        
        if (request.gradingOptions() != null && !request.gradingOptions().isEmpty()) {
            aiRequest.put("grading_options", request.gradingOptions());
        }
        
        return aiRequest;
    }
    
    /**
     * AI 서버 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private GradingResultDto parseGradingResponse(Map<String, Object> response) {
        Map<String, Object> gradingResult = (Map<String, Object>) response.get("grading_result");
        
        if (gradingResult == null) {
            return null;
        }
        
        // 문제별 결과 파싱
        List<Map<String, Object>> questionResultsList = (List<Map<String, Object>>) gradingResult.get("question_results");
        List<GradingResultDto.QuestionResultDto> questionResults = null;
        
        if (questionResultsList != null) {
            questionResults = questionResultsList.stream()
                .map(qr -> new GradingResultDto.QuestionResultDto(
                    UUID.fromString((String) qr.get("question_id")),
                    qr.get("answer_id") != null ? UUID.fromString((String) qr.get("answer_id")) : null,
                    (Boolean) qr.get("is_correct"),
                    (Integer) qr.get("score"),
                    (Integer) qr.get("max_score"),
                    (String) qr.get("grading_method"),
                    (String) qr.get("confidence_score"),
                    (String) qr.get("scoring_comment"),
                    LocalDateTime.parse((String) qr.get("created_at"))
                ))
                .collect(Collectors.toList());
        }
        
        // 메타데이터 파싱
        Map<String, Object> metadataMap = (Map<String, Object>) gradingResult.get("metadata");
        GradingResultDto.GradingMetadataDto metadata = null;
        
        if (metadataMap != null) {
            metadata = new GradingResultDto.GradingMetadataDto(
                (Integer) metadataMap.get("total_questions"),
                (Integer) metadataMap.get("multiple_choice_count"),
                (Integer) metadataMap.get("subjective_count"),
                ((Number) metadataMap.get("processing_time_ms")).longValue(),
                (String) metadataMap.get("ai_model_version")
            );
        }
        
        // GradingResultDto 생성
        return new GradingResultDto(
            UUID.fromString((String) gradingResult.get("result_id")),
            UUID.fromString((String) gradingResult.get("submission_id")),
            UUID.fromString((String) gradingResult.get("exam_sheet_id")),
            (String) gradingResult.get("status"),
            (Integer) gradingResult.get("total_score"),
            (Integer) gradingResult.get("max_total_score"),
            questionResults,
            metadata,
            (String) gradingResult.get("grading_comment"),
            LocalDateTime.parse((String) gradingResult.get("graded_at")),
            (Integer) gradingResult.get("version")
        );
    }
}