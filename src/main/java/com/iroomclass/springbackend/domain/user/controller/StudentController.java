package com.iroomclass.springbackend.domain.user.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import com.iroomclass.springbackend.domain.exam.dto.exam.ExamResultDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.RecentExamSubmissionsResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.StudentSubmissionHistoryResponse;
import com.iroomclass.springbackend.domain.exam.dto.question.QuestionResultResponse;
import com.iroomclass.springbackend.domain.user.dto.StudentProfileResponse;
import com.iroomclass.springbackend.domain.user.service.StudentService;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학생 결과 조회 컨트롤러
 * 
 * 학생이 시험 결과를 조회할 수 있는 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/user/student")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 API", description = "학생 마이페이지, 시험 이력 조회, 결과 상세 조회 API")
public class StudentController {

    private final StudentService studentService;

    /**
     * 학생 마이페이지 조회
     * 
     * @param name      학생 이름
     * @param phone     학생 전화번호
     * @param birthDate 학생 생년월일
     * @return 학생 기본 정보
     */
    @GetMapping("/profile")
    @Operation(summary = "학생 마이페이지 조회", description = "학생의 기본 정보를 조회합니다. 3-factor 인증 사용.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 학생")
    })
    public ApiResponse<StudentProfileResponse> getProfile(
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String name,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String phone,
            @Parameter(description = "학생 생년월일", example = "2008-03-15") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate) {
        log.info("학생 마이페이지 조회 요청: 이름={}, 전화번호={}, 생년월일={}", name, phone, birthDate);

        StudentProfileResponse response = studentService.getProfile(name, phone, birthDate);

        log.info("학생 마이페이지 조회 성공: 이름={}, 학년={}", response.name(), response.grade());

        return ApiResponse.success("프로필 조회 성공", response);
    }

    /**
     * 학생 메인화면 - 최근 시험 3건 조회
     * 
     * @param studentName  학생 이름
     * @param studentPhone 학생 전화번호
     * @return 최근 시험 3건 목록
     */
    @GetMapping("/recent-submissions")
    @Operation(summary = "학생 메인화면 - 최근 시험 3건 조회", description = "학생 메인화면에서 최근 제출한 시험 3건을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 학생")
    })
    public ApiResponse<RecentExamSubmissionsResponse> getRecentExamSubmissions(
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String studentPhone) {
        log.info("학생 최근 시험 3건 조회 요청: 이름={}, 전화번호={}", studentName, studentPhone);

        RecentExamSubmissionsResponse response = studentService.getRecentExamSubmissions(studentName, studentPhone);

        log.info("학생 최근 시험 3건 조회 성공: 이름={}, 조회된 건수={}",
                response.studentName(), response.recentExams().size());

        return ApiResponse.success("최근 시험 이력 조회 성공", response);
    }

    /**
     * 학생별 시험 제출 이력 조회
     * 
     * @param studentName  학생 이름
     * @param studentPhone 학생 전화번호
     * @return 시험 제출 이력 목록
     */
    @GetMapping("/submissions")
    @Operation(summary = "학생별 시험 제출 이력 조회", description = "학생의 모든 시험 제출 이력을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 학생")
    })
    public ApiResponse<StudentSubmissionHistoryResponse> getSubmissionHistory(
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String studentPhone) {
        log.info("학생 시험 제출 이력 조회 요청: 이름={}, 전화번호={}", studentName, studentPhone);

        StudentSubmissionHistoryResponse response = studentService.getSubmissionHistory(studentName, studentPhone);

        log.info("학생 시험 제출 이력 조회 성공: 이름={}, 제출 수={}",
                response.studentName(), response.submissions().size());

        return ApiResponse.success("성공", response);
    }

    /**
     * 시험별 상세 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param studentName  학생 이름
     * @param studentPhone 학생 전화번호
     * @return 시험 상세 결과
     */
    @GetMapping("/submission/{submissionId}/result")
    @Operation(summary = "시험별 상세 결과 조회", description = "특정 시험의 상세 결과를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 제출 또는 학생")
    })
    public ApiResponse<ExamResultDetailResponse> getExamResult(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID submissionId,
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String studentPhone) {
        log.info("시험 상세 결과 조회 요청: 제출 ID={}, 학생={}, 전화번호={}",
                submissionId, studentName, studentPhone);

        ExamResultDetailResponse response = studentService.getExamResult(submissionId, studentName, studentPhone);

        log.info("시험 상세 결과 조회 성공: 제출 ID={}, 학생={}, 총점={}",
                submissionId, response.studentName(), response.totalScore());

        return ApiResponse.success("성공", response);
    }

    /**
     * 문제별 정답/오답, 점수, 단원, 난이도 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param questionId   문제 ID
     * @param studentName  학생 이름
     * @param studentPhone 학생 전화번호
     * @return 문제별 결과 정보
     */
    @GetMapping("/submission/{submissionId}/question/{questionId}")
    @Operation(summary = "문제별 정답/오답, 점수, 단원, 난이도 조회", description = "특정 문제의 정답/오답, 점수, 단원, 난이도 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 제출, 문제 또는 학생")
    })
    public ApiResponse<QuestionResultResponse> getQuestionResult(
            @Parameter(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID submissionId,
            @Parameter(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID questionId,
            @Parameter(description = "학생 이름", example = "김철수") @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") @RequestParam String studentPhone) {
        log.info("문제별 결과 조회 요청: 제출 ID={}, 문제 ID={}, 학생={}, 전화번호={}",
                submissionId, questionId, studentName, studentPhone);

        QuestionResultResponse response = studentService.getQuestionResult(submissionId, questionId, studentName,
                studentPhone);

        log.info("문제별 결과 조회 성공: 제출 ID={}, 문제 ID={}, 정답 여부={}, 점수={}",
                submissionId, questionId, response.isCorrect(), response.score());

        return ApiResponse.success("성공", response);
    }
}
