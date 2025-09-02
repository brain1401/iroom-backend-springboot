package com.iroomclass.springbackend.domain.analysis.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroomclass.springbackend.domain.analysis.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.analysis.dto.OverallStatisticsResponse;
import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;

/**
 * DashboardService 테스트
 * 
 * 대시보드 엔드포인트의 0 반환 문제를 재현하고 해결하는 테스트
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ExamRepository examRepository;
    
    @Mock
    private ExamSubmissionRepository examSubmissionRepository;
    
    @Mock
    private ExamResultRepository examResultRepository;
    
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("학년별 성적 분포도 조회 - 데이터가 있을 때")
    void getGradeScoreDistribution_WithData_ShouldReturnCorrectDistribution() {
        // Given
        Integer grade = 1;
        
        // 학생 데이터 생성
        Student student1 = Student.builder()
            .id(1L)
            .name("김철수")
            .phone("010-1111-1111")
            .build();
        
        Student student2 = Student.builder()
            .id(2L)
            .name("이영희")
            .phone("010-2222-2222")
            .build();
        
        // 시험 데이터 생성
        ExamSheet examSheet = ExamSheet.builder()
            .id(UUID.randomUUID())
            .examName("중1 중간고사")
            .grade(1)
            .build();
        
        Exam exam = Exam.builder()
            .id(UUID.randomUUID())
            .examName("중1 중간고사")
            .grade(1)
            .studentCount(2)
            .examSheet(examSheet)
            .createdAt(LocalDateTime.now())
            .build();
        
        // 시험 제출 데이터 생성
        ExamSubmission submission1 = ExamSubmission.builder()
            .id(UUID.randomUUID())
            .exam(exam)
            .student(student1)
            .submittedAt(LocalDateTime.now())
            .build();
        
        ExamSubmission submission2 = ExamSubmission.builder()
            .id(UUID.randomUUID())
            .exam(exam)
            .student(student2)
            .submittedAt(LocalDateTime.now())
            .build();
        
        List<ExamSubmission> submissions = Arrays.asList(submission1, submission2);
        
        // 시험 결과 데이터 생성
        ExamResult result1 = ExamResult.builder()
            .id(UUID.randomUUID())
            .examSubmission(submission1)
            .totalScore(85)
            .version(1)
            .build();
        
        ExamResult result2 = ExamResult.builder()
            .id(UUID.randomUUID())
            .examSubmission(submission2)
            .totalScore(75)
            .version(1)
            .build();
        
        // Mock 설정
        when(examSubmissionRepository.findByExamGrade(grade)).thenReturn(submissions);
        when(examResultRepository.findLatestBySubmissionId(submission1.getId())).thenReturn(Optional.of(result1));
        when(examResultRepository.findLatestBySubmissionId(submission2.getId())).thenReturn(Optional.of(result2));
        
        // When
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(grade);
        
        // Then
        assertNotNull(response);
        assertEquals(grade, response.grade());
        assertEquals(2, response.totalStudentCount()); // 학생 수
        assertEquals(2, response.studentWithScoreCount()); // 유효 점수 수
        assertEquals(80.0, response.overallAverageScore(), 0.1); // 평균 점수 (85+75)/2 = 80
        
        System.out.println("테스트 결과:");
        System.out.println("학년: " + response.grade());
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("유효 점수 수: " + response.studentWithScoreCount());
        System.out.println("평균 점수: " + response.overallAverageScore());
    }

    @Test
    @DisplayName("학년별 성적 분포도 조회 - 데이터가 없을 때")
    void getGradeScoreDistribution_WithNoData_ShouldReturnZero() {
        // Given
        Integer grade = 1;
        when(examSubmissionRepository.findByExamGrade(grade)).thenReturn(Collections.emptyList());
        
        // When
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(grade);
        
        // Then
        assertNotNull(response);
        assertEquals(grade, response.grade());
        assertEquals(0, response.totalStudentCount());
        assertEquals(0, response.studentWithScoreCount());
        assertEquals(0.0, response.overallAverageScore());
        
        System.out.println("데이터 없는 경우 테스트 결과:");
        System.out.println("학년: " + response.grade());
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("유효 점수 수: " + response.studentWithScoreCount());
        System.out.println("평균 점수: " + response.overallAverageScore());
    }

    @Test
    @DisplayName("학년별 시험 제출 현황 조회 - 데이터가 있을 때")
    void getGradeSubmissionStatus_WithData_ShouldReturnCorrectStatus() {
        // Given
        Integer grade = 1;
        
        ExamSheet examSheet = ExamSheet.builder()
            .id(UUID.randomUUID())
            .examName("중1 중간고사")
            .grade(1)
            .build();
        
        Exam exam = Exam.builder()
            .id(UUID.randomUUID())
            .examName("중1 중간고사")
            .grade(1)
            .studentCount(10) // 전체 학생 10명
            .examSheet(examSheet)
            .createdAt(LocalDateTime.now())
            .build();
        
        List<Exam> exams = Arrays.asList(exam);
        
        // Mock 설정
        when(examRepository.findByGrade(grade)).thenReturn(exams);
        when(examSubmissionRepository.countByExamId(exam.getId())).thenReturn(7L); // 7명 제출
        
        // When
        GradeSubmissionStatusResponse response = dashboardService.getGradeSubmissionStatus(grade);
        
        // Then
        assertNotNull(response);
        assertEquals(grade, response.grade());
        assertEquals(1, response.totalExamCount());
        assertEquals(7, response.totalSubmissionCount());
        
        System.out.println("제출 현황 테스트 결과:");
        System.out.println("학년: " + response.grade());
        System.out.println("시험 수: " + response.totalExamCount());
        System.out.println("총 제출 수: " + response.totalSubmissionCount());
    }

    @Test
    @DisplayName("전체 학년 통합 통계 조회 - 데이터가 있을 때")
    void getOverallStatistics_WithData_ShouldReturnCorrectStatistics() {
        // Given
        // 학생들 생성
        Student student1 = Student.builder().id(1L).name("김철수").phone("010-1111-1111").build();
        Student student2 = Student.builder().id(2L).name("이영희").phone("010-2222-2222").build();
        
        // 시험 생성
        ExamSheet examSheet = ExamSheet.builder().id(UUID.randomUUID()).examName("통합 시험").grade(1).build();
        Exam exam1 = Exam.builder()
            .id(UUID.randomUUID())
            .examName("1학년 시험")
            .grade(1)
            .studentCount(2)
            .examSheet(examSheet)
            .build();
        
        Exam exam2 = Exam.builder()
            .id(UUID.randomUUID())
            .examName("2학년 시험") 
            .grade(2)
            .studentCount(2)
            .examSheet(examSheet)
            .build();
        
        // 제출 데이터 생성
        ExamSubmission submission1 = ExamSubmission.builder()
            .id(UUID.randomUUID())
            .exam(exam1)
            .student(student1)
            .build();
        
        ExamSubmission submission2 = ExamSubmission.builder()
            .id(UUID.randomUUID())
            .exam(exam2)
            .student(student2)
            .build();
        
        List<ExamSubmission> allSubmissions = Arrays.asList(submission1, submission2);
        
        // 결과 데이터 생성
        ExamResult result1 = ExamResult.builder()
            .id(UUID.randomUUID())
            .examSubmission(submission1)
            .totalScore(85)
            .version(1)
            .build();
        
        ExamResult result2 = ExamResult.builder()
            .id(UUID.randomUUID())
            .examSubmission(submission2)
            .totalScore(75)
            .version(1)
            .build();
        
        // Mock 설정
        when(examSubmissionRepository.findAll()).thenReturn(allSubmissions);
        when(examResultRepository.findLatestBySubmissionId(submission1.getId())).thenReturn(Optional.of(result1));
        when(examResultRepository.findLatestBySubmissionId(submission2.getId())).thenReturn(Optional.of(result2));
        
        // When
        OverallStatisticsResponse response = dashboardService.getOverallStatistics();
        
        // Then
        assertNotNull(response);
        assertEquals(2, response.totalStudentCount()); // 전체 학생 2명
        assertEquals(80.0, response.overallAverageScore(), 0.1); // 평균 80점
        
        System.out.println("전체 통계 테스트 결과:");
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("전체 평균 점수: " + response.overallAverageScore());
    }
    
    @Test
    @DisplayName("실제 Repository 메소드 호출 확인")
    void verifyRepositoryMethodCalls() {
        // Given
        Integer grade = 1;
        when(examSubmissionRepository.findByExamGrade(grade)).thenReturn(Collections.emptyList());
        
        // When
        dashboardService.getGradeScoreDistribution(grade);
        
        // Then
        verify(examSubmissionRepository, times(1)).findByExamGrade(grade);
        
        System.out.println("Repository 메소드 호출이 확인되었습니다: findByExamGrade(" + grade + ")");
    }
}