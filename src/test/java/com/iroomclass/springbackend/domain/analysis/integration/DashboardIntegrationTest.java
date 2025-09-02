package com.iroomclass.springbackend.domain.analysis.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.analysis.dto.GradeScoreDistributionResponse;
import com.iroomclass.springbackend.domain.analysis.dto.GradeSubmissionStatusResponse;
import com.iroomclass.springbackend.domain.analysis.dto.OverallStatisticsResponse;
import com.iroomclass.springbackend.domain.analysis.service.DashboardServiceOptimized;
import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;

/**
 * 대시보드 통합 테스트
 * 
 * 실제 H2 데이터베이스를 사용해서 Repository와 Service가 함께 동작하는지 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class DashboardIntegrationTest {

    @Autowired
    private DashboardServiceOptimized dashboardService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ExamSheetRepository examSheetRepository;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    private Student student1;
    private Student student2;
    private Student student3;
    private ExamSheet examSheet1;
    private ExamSheet examSheet2;
    private Exam exam1; // 1학년
    private Exam exam2; // 2학년
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        setupTestData();
    }
    
    void setupTestData() {
        // 학생 생성
        student1 = studentRepository.save(Student.builder()
            .name("김철수")
            .phone("010-1111-1111")
            .birthDate(LocalDate.of(2010, 1, 1))
            .build());
            
        student2 = studentRepository.save(Student.builder()
            .name("이영희")
            .phone("010-2222-2222")
            .birthDate(LocalDate.of(2010, 2, 2))
            .build());
            
        student3 = studentRepository.save(Student.builder()
            .name("박민수")
            .phone("010-3333-3333")
            .birthDate(LocalDate.of(2009, 3, 3))
            .build());
        
        // 시험지 생성
        examSheet1 = examSheetRepository.save(ExamSheet.builder()
            .examName("1학년 중간고사")
            .grade(1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build());
            
        examSheet2 = examSheetRepository.save(ExamSheet.builder()
            .examName("2학년 중간고사")
            .grade(2)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build());
        
        // 시험 생성
        exam1 = examRepository.save(Exam.builder()
            .examName("1학년 중간고사")
            .grade(1)
            .studentCount(10)
            .examSheet(examSheet1)
            .createdAt(LocalDateTime.now())
            .build());
            
        exam2 = examRepository.save(Exam.builder()
            .examName("2학년 중간고사")
            .grade(2)
            .studentCount(10)
            .examSheet(examSheet2)
            .createdAt(LocalDateTime.now())
            .build());
        
        // 1학년 시험 제출 생성 (학생 1, 2)
        ExamSubmission submission1 = examSubmissionRepository.save(ExamSubmission.builder()
            .exam(exam1)
            .student(student1)
            .submittedAt(LocalDateTime.now())
            .build());
            
        ExamSubmission submission2 = examSubmissionRepository.save(ExamSubmission.builder()
            .exam(exam1)
            .student(student2)
            .submittedAt(LocalDateTime.now())
            .build());
        
        // 2학년 시험 제출 생성 (학생 3)
        ExamSubmission submission3 = examSubmissionRepository.save(ExamSubmission.builder()
            .exam(exam2)
            .student(student3)
            .submittedAt(LocalDateTime.now())
            .build());
        
        // 시험 결과 생성
        examResultRepository.save(ExamResult.builder()
            .examSubmission(submission1)
            .examSheet(submission1.getExam().getExamSheet()) // examSheet 필드 추가
            .totalScore(85) // 1학년 학생1: 85점
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build());
            
        examResultRepository.save(ExamResult.builder()
            .examSubmission(submission2)
            .examSheet(submission2.getExam().getExamSheet()) // examSheet 필드 추가
            .totalScore(75) // 1학년 학생2: 75점
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build());
            
        examResultRepository.save(ExamResult.builder()
            .examSubmission(submission3)
            .examSheet(submission3.getExam().getExamSheet()) // examSheet 필드 추가
            .totalScore(90) // 2학년 학생3: 90점
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build());
    }

    @Test
    @DisplayName("학년별 성적 분포도 조회 - 1학년 (실제 데이터)")
    void getGradeScoreDistribution_Grade1_WithRealData() {
        // When
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(1);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.grade());
        assertEquals("중1", response.gradeName());
        assertEquals(2, response.totalStudentCount()); // 김철수, 이영희
        assertEquals(2, response.studentWithScoreCount()); // 둘 다 점수 있음
        assertEquals(80.0, response.overallAverageScore(), 0.1); // (85+75)/2 = 80.0
        
        // 점수 범위별 확인
        assertNotNull(response.scoreRanges());
        assertTrue(response.scoreRanges().size() > 0);
        
        // 상위권/중위권/하위권 분포 확인
        assertNotNull(response.rankDistribution());
        assertEquals(1, response.rankDistribution().topRankCount()); // 85점 (80점 이상)
        assertEquals(1, response.rankDistribution().middleRankCount()); // 75점 (60-79점)
        assertEquals(0, response.rankDistribution().bottomRankCount()); // 60점 미만 없음
        
        System.out.println("=== 1학년 성적 분포도 결과 ===");
        System.out.println("학년: " + response.grade());
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("점수 있는 학생 수: " + response.studentWithScoreCount());
        System.out.println("평균 점수: " + response.overallAverageScore());
        System.out.println("상위권: " + response.rankDistribution().topRankCount() + "명");
        System.out.println("중위권: " + response.rankDistribution().middleRankCount() + "명");
        System.out.println("하위권: " + response.rankDistribution().bottomRankCount() + "명");
    }

    @Test
    @DisplayName("학년별 성적 분포도 조회 - 2학년 (실제 데이터)")
    void getGradeScoreDistribution_Grade2_WithRealData() {
        // When
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(2);
        
        // Then
        assertNotNull(response);
        assertEquals(2, response.grade());
        assertEquals("중2", response.gradeName());
        assertEquals(1, response.totalStudentCount()); // 박민수만
        assertEquals(1, response.studentWithScoreCount()); // 박민수 점수 있음
        assertEquals(90.0, response.overallAverageScore(), 0.1); // 90점
        
        // 상위권/중위권/하위권 분포 확인
        assertEquals(1, response.rankDistribution().topRankCount()); // 90점 (80점 이상)
        assertEquals(0, response.rankDistribution().middleRankCount());
        assertEquals(0, response.rankDistribution().bottomRankCount());
        
        System.out.println("=== 2학년 성적 분포도 결과 ===");
        System.out.println("학년: " + response.grade());
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("점수 있는 학생 수: " + response.studentWithScoreCount());
        System.out.println("평균 점수: " + response.overallAverageScore());
    }

    @Test
    @DisplayName("학년별 시험 제출 현황 조회 - 1학년 (실제 데이터)")
    void getGradeSubmissionStatus_Grade1_WithRealData() {
        // When
        GradeSubmissionStatusResponse response = dashboardService.getGradeSubmissionStatus(1);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.grade());
        assertEquals("중1", response.gradeName());
        assertEquals(1, response.totalExamCount()); // 1학년 시험 1개
        assertEquals(2, response.totalSubmissionCount()); // 제출 2건
        
        // 시험별 제출 현황 확인
        assertNotNull(response.examSubmissions());
        assertEquals(1, response.examSubmissions().size());
        
        GradeSubmissionStatusResponse.ExamSubmissionStatus examStatus = response.examSubmissions().get(0);
        assertEquals("1학년 중간고사", examStatus.examName());
        assertEquals(10, examStatus.totalStudentCount()); // 전체 학생 10명으로 설정
        assertEquals(2, examStatus.submittedStudentCount()); // 실제 제출 2명
        assertEquals(8, examStatus.notSubmittedStudentCount()); // 미제출 8명
        assertEquals(20.0, examStatus.submissionRate(), 0.1); // 20% 제출률
        
        System.out.println("=== 1학년 제출 현황 결과 ===");
        System.out.println("학년: " + response.grade());
        System.out.println("시험 수: " + response.totalExamCount());
        System.out.println("총 제출 수: " + response.totalSubmissionCount());
        System.out.println("시험명: " + examStatus.examName());
        System.out.println("제출률: " + examStatus.submissionRate() + "%");
    }

    @Test
    @DisplayName("전체 학년 통합 통계 조회 (실제 데이터)")
    void getOverallStatistics_WithRealData() {
        // When
        OverallStatisticsResponse response = dashboardService.getOverallStatistics();
        
        // Then
        assertNotNull(response);
        assertEquals(3, response.totalStudentCount()); // 김철수, 이영희, 박민수
        assertEquals(83.3, response.overallAverageScore(), 0.2); // (85+75+90)/3 = 83.33...
        
        // 전체 상위권/중위권/하위권 분포 확인
        assertNotNull(response.rankDistribution());
        assertEquals(2, response.rankDistribution().highRankCount()); // 85, 90점 (80점 이상)
        assertEquals(1, response.rankDistribution().middleRankCount()); // 75점 (60-79점)
        assertEquals(0, response.rankDistribution().lowRankCount()); // 60점 미만 없음
        
        // 학년별 세부 통계 확인
        assertNotNull(response.gradeStatistics());
        assertEquals(2, response.gradeStatistics().size()); // 1학년, 2학년
        
        System.out.println("=== 전체 통합 통계 결과 ===");
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("전체 평균 점수: " + response.overallAverageScore());
        System.out.println("상위권: " + response.rankDistribution().highRankCount() + "명");
        System.out.println("중위권: " + response.rankDistribution().middleRankCount() + "명");
        System.out.println("하위권: " + response.rankDistribution().lowRankCount() + "명");
        System.out.println("학년별 통계: " + response.gradeStatistics().size() + "개 학년");
    }

    @Test
    @DisplayName("데이터가 없는 학년 조회 - 3학년")
    void getGradeScoreDistribution_Grade3_WithNoData() {
        // When
        GradeScoreDistributionResponse response = dashboardService.getGradeScoreDistribution(3);
        
        // Then
        assertNotNull(response);
        assertEquals(3, response.grade());
        assertEquals("중3", response.gradeName());
        assertEquals(0, response.totalStudentCount()); // 3학년 데이터 없음
        assertEquals(0, response.studentWithScoreCount());
        assertEquals(0.0, response.overallAverageScore());
        
        System.out.println("=== 3학년 (데이터 없음) 결과 ===");
        System.out.println("학년: " + response.grade());
        System.out.println("전체 학생 수: " + response.totalStudentCount());
        System.out.println("평균 점수: " + response.overallAverageScore());
    }
    
    @Test
    @DisplayName("Repository findByExamGrade 메소드 직접 테스트")
    void testFindByExamGrade_DirectRepositoryCall() {
        // When
        var grade1Submissions = examSubmissionRepository.findByExamGrade(1);
        var grade2Submissions = examSubmissionRepository.findByExamGrade(2);
        var grade3Submissions = examSubmissionRepository.findByExamGrade(3);
        
        // Then
        assertEquals(2, grade1Submissions.size(), "1학년 시험 제출은 2개여야 합니다");
        assertEquals(1, grade2Submissions.size(), "2학년 시험 제출은 1개여야 합니다");
        assertEquals(0, grade3Submissions.size(), "3학년 시험 제출은 0개여야 합니다");
        
        System.out.println("=== Repository 직접 테스트 결과 ===");
        System.out.println("1학년 제출 수: " + grade1Submissions.size());
        System.out.println("2학년 제출 수: " + grade2Submissions.size());
        System.out.println("3학년 제출 수: " + grade3Submissions.size());
        
        // 1학년 제출 데이터 상세 확인
        for (ExamSubmission submission : grade1Submissions) {
            System.out.println("1학년 제출 - 시험명: " + submission.getExam().getExamName() + 
                             ", 학생명: " + submission.getStudent().getName() +
                             ", 학년: " + submission.getExam().getGrade());
        }
        
        // 2학년 제출 데이터 상세 확인
        for (ExamSubmission submission : grade2Submissions) {
            System.out.println("2학년 제출 - 시험명: " + submission.getExam().getExamName() + 
                             ", 학생명: " + submission.getStudent().getName() +
                             ", 학년: " + submission.getExam().getGrade());
        }
    }
}