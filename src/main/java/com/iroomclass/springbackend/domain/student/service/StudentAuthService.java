package com.iroomclass.springbackend.domain.student.service;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.exception.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학생 인증 서비스
 * 
 * <p>학생의 신원 확인을 위한 3-factor 인증(이름, 전화번호, 생년월일)을 
 * 담당하는 서비스입니다. JWT 토큰 없이 학생의 존재 여부만 확인합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudentAuthService {
    
    private final StudentRepository studentRepository;
    
    /**
     * 학생 인증 정보를 검증하고 학생 엔티티를 반환합니다
     * 
     * <p>이름, 전화번호, 생년월일이 모두 일치하는 학생을 조회합니다.
     * 일치하는 학생이 없으면 StudentNotFoundException을 발생시킵니다.</p>
     * 
     * @param request 학생 인증 요청 정보
     * @return 인증된 학생 엔티티
     * @throws StudentNotFoundException 일치하는 학생이 없을 때
     */
    public Student validateAndGetStudent(StudentAuthRequest request) {
        log.debug("학생 인증 시도: name={}, phone={}, birthDate={}", 
                 request.name(), request.phone(), request.birthDate());
        
        return studentRepository.findByNameAndPhoneAndBirthDate(
                request.name(), 
                request.phone(), 
                request.birthDate())
            .orElseThrow(() -> {
                log.warn("학생 인증 실패: name={}, phone={}, birthDate={}", 
                        request.name(), request.phone(), request.birthDate());
                return new StudentNotFoundException(
                    String.format("학생 정보를 찾을 수 없습니다. (이름: %s, 전화번호: %s, 생년월일: %s)", 
                                request.name(), request.phone(), request.birthDate()));
            });
    }
    
    /**
     * 학생 인증 정보로 학생 존재 여부만 확인합니다
     * 
     * <p>로그인 API에서 사용되며, 단순히 true/false만 반환합니다.
     * 예외를 발생시키지 않고 존재 여부만 확인합니다.</p>
     * 
     * @param request 학생 인증 요청 정보
     * @return 학생 존재 여부 (true: 존재함, false: 존재하지 않음)
     */
    public boolean validateStudentExists(StudentAuthRequest request) {
        log.debug("학생 존재 여부 확인: name={}, phone={}, birthDate={}", 
                 request.name(), request.phone(), request.birthDate());
        
        boolean exists = studentRepository.findByNameAndPhoneAndBirthDate(
                request.name(), 
                request.phone(), 
                request.birthDate())
            .isPresent();
        
        log.debug("학생 존재 여부 결과: {}", exists);
        return exists;
    }
    
    /**
     * Student 엔티티의 matches 메서드를 활용한 추가 검증
     * 
     * <p>데이터베이스 조회 후 엔티티 레벨에서 한 번 더 검증합니다.
     * 더 안전한 인증을 원할 때 사용할 수 있습니다.</p>
     * 
     * @param student Student 엔티티
     * @param request 인증 요청 정보
     * @return 검증 성공 여부
     */
    public boolean verifyStudentMatches(Student student, StudentAuthRequest request) {
        boolean matches = student.matches(request.name(), request.phone(), request.birthDate());
        
        if (!matches) {
            log.warn("엔티티 레벨 검증 실패: studentId={}, request={}", student.getId(), request);
        }
        
        return matches;
    }
}