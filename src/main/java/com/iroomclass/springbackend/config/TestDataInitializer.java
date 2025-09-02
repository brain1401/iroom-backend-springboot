package com.iroomclass.springbackend.config;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.auth.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 테스트 환경에서 초기 데이터를 생성하는 컴포넌트
 * 
 * <p>H2 인메모리 데이터베이스 환경에서 테스트를 위한 
 * 학생 및 선생님 계정을 자동으로 생성합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@Profile("test-local") // test-local 프로파일에서만 실행
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements CommandLineRunner {
    
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeTestData();
    }
    
    /**
     * 테스트용 학생 및 선생님 데이터 생성
     * 
     * <p>인증 테스트를 위한 기본 계정들을 생성합니다.</p>
     */
    private void initializeTestData() {
        initializeTestStudents();
        initializeTestTeachers();
    }
    
    /**
     * 테스트용 학생 계정 생성
     */
    private void initializeTestStudents() {
        // 테스트용 학생 1
        String studentName1 = "김학생";
        String studentPhone1 = "010-1234-5678";
        LocalDate birthDate1 = LocalDate.of(2010, 5, 15);
        
        if (studentRepository.findByNameAndPhone(studentName1, studentPhone1).isEmpty()) {
            Student testStudent1 = Student.builder()
                .name(studentName1)
                .phone(studentPhone1)
                .birthDate(birthDate1)
                .build();
            
            studentRepository.save(testStudent1);
            log.info("테스트 학생 계정 생성 완료: 이름={}, 전화번호={}, 생년월일={}", 
                    studentName1, studentPhone1, birthDate1);
        } else {
            log.info("테스트 학생 계정이 이미 존재합니다: {}", studentName1);
        }
        
        // 테스트용 학생 2
        String studentName2 = "이학생";
        String studentPhone2 = "010-2345-6789";
        LocalDate birthDate2 = LocalDate.of(2011, 8, 20);
        
        if (studentRepository.findByNameAndPhone(studentName2, studentPhone2).isEmpty()) {
            Student testStudent2 = Student.builder()
                .name(studentName2)
                .phone(studentPhone2)
                .birthDate(birthDate2)
                .build();
            
            studentRepository.save(testStudent2);
            log.info("테스트 학생 계정 생성 완료: 이름={}, 전화번호={}, 생년월일={}", 
                    studentName2, studentPhone2, birthDate2);
        } else {
            log.info("테스트 학생 계정이 이미 존재합니다: {}", studentName2);
        }
    }
    
    /**
     * 테스트용 선생님 계정 생성
     */
    private void initializeTestTeachers() {
        String testUsername = "teacher";
        
        if (teacherRepository.findByUsername(testUsername).isEmpty()) {
            Teacher testTeacher = Teacher.builder()
                .username(testUsername)
                .password("password123") // 테스트용 평문 비밀번호
                .build();
            
            teacherRepository.save(testTeacher);
            log.info("테스트 선생님 계정 생성 완료: 사용자명={}", testUsername);
            log.info("테스트 로그인 정보 - 사용자명: teacher, 비밀번호: password123");
        } else {
            log.info("테스트 선생님 계정이 이미 존재합니다: {}", testUsername);
        }
    }
}