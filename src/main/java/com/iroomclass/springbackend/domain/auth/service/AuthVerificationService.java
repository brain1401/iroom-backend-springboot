package com.iroomclass.springbackend.domain.auth.service;

import com.iroomclass.springbackend.domain.auth.dto.StudentDto;
import com.iroomclass.springbackend.domain.auth.dto.StudentUpsertRequest;
import com.iroomclass.springbackend.domain.auth.dto.VerifyStudentRequest;
import com.iroomclass.springbackend.domain.auth.dto.VerifyTeacherRequest;
import com.iroomclass.springbackend.domain.auth.dto.VerificationResponse;
import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.auth.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증 검증 서비스
 * 
 * <p>
 * 학생과 선생님의 DB 매칭 검증을 처리하는 서비스입니다.
 * JWT나 세션 없이 단순한 DB 매칭을 통한 true/false 결과를 반환합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthVerificationService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /**
     * 학생 검증
     * 
     * <p>
     * 이름, 전화번호, 생년월일을 통한 3-factor 학생 검증을 수행합니다.
     * 데이터베이스에 일치하는 학생이 있으면 true, 없으면 false를 반환합니다.
     * </p>
     * 
     * @param request 학생 검증 요청 데이터
     * @return 검증 성공 시 true, 실패 시 false
     */
    public VerificationResponse verifyStudent(VerifyStudentRequest request) {
        log.info("학생 검증 시작: 이름={}, 전화번호={}, 생년월일={}",
                request.name(), request.phone(), request.birthDate());

        try {
            // 학생 엔티티에서 3-factor 매칭 검색
            Optional<Student> studentOptional = studentRepository
                    .findByNameAndPhoneAndBirthDate(
                            request.name(),
                            request.phone(),
                            request.birthDate());

            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                log.info("학생 검증 성공: 이름={}, 학생ID={}", request.name(), student.getId());
                return VerificationResponse.success();
            } else {
                log.warn("학생 검증 실패: 이름={}, 전화번호={}, 생년월일={} - 일치하는 학생 없음",
                        request.name(), request.phone(), request.birthDate());
                return VerificationResponse.failure();
            }

        } catch (Exception e) {
            log.error("학생 검증 중 오류 발생: 이름={}, 오류={}", request.name(), e.getMessage(), e);
            return VerificationResponse.failure();
        }
    }

    /**
     * 선생님 검증
     * 
     * <p>
     * 사용자명과 비밀번호를 통한 선생님 검증을 수행합니다.
     * 데이터베이스에 일치하는 선생님이 있으면 true, 없으면 false를 반환합니다.
     * </p>
     * 
     * @param request 선생님 검증 요청 데이터
     * @return 검증 성공 시 true, 실패 시 false
     */
    public VerificationResponse verifyTeacher(VerifyTeacherRequest request) {
        log.info("선생님 검증 시작: 사용자명={}", request.username());

        try {
            // 선생님 엔티티에서 사용자명/비밀번호 매칭 검색
            Optional<Teacher> teacherOptional = teacherRepository
                    .findByUsernameAndPassword(
                            request.username(),
                            request.password());

            if (teacherOptional.isPresent()) {
                Teacher teacher = teacherOptional.get();
                log.info("선생님 검증 성공: 사용자명={}, 선생님ID={}", request.username(), teacher.getId());
                return VerificationResponse.success();
            } else {
                log.warn("선생님 검증 실패: 사용자명={} - 일치하는 선생님 없음", request.username());
                return VerificationResponse.failure();
            }

        } catch (Exception e) {
            log.error("선생님 검증 중 오류 발생: 사용자명={}, 오류={}", request.username(), e.getMessage(), e);
            return VerificationResponse.failure();
        }
    }

    /**
     * 학생 정보 Upsert (등록 또는 조회)
     * 
     * <p>
     * 학생 정보가 데이터베이스에 없으면 새로 등록하고,
     * 이미 있으면 기존 정보를 반환합니다.
     * 3-factor 인증 (이름, 전화번호, 생년월일)을 통해 학생을 식별합니다.
     * </p>
     * 
     * @param request 학생 등록/조회 요청 데이터
     * @return 등록되거나 조회된 학생 정보 DTO
     */
    @Transactional
    public StudentDto upsertStudent(StudentUpsertRequest request) {
        log.info("학생 정보 Upsert 시작: 이름={}, 전화번호={}, 생년월일={}",
                request.name(), request.phone(), request.birthDate());

        try {
            // 기존 학생 존재 여부 확인
            Optional<Student> existingStudent = studentRepository
                    .findByNameAndPhoneAndBirthDate(
                            request.name(),
                            request.phone(),
                            request.birthDate());

            if (existingStudent.isPresent()) {
                // 기존 학생 정보 반환
                Student student = existingStudent.get();
                log.info("기존 학생 정보 조회: 이름={}, 학생ID={}", request.name(), student.getId());
                return StudentDto.from(student);
            } else {
                // 새 학생 등록
                Student newStudent = Student.builder()
                        .name(request.name())
                        .phone(request.phone())
                        .birthDate(request.birthDate())
                        .build();
                
                Student savedStudent = studentRepository.saveAndFlush(newStudent);
                // JPA 엔티티를 다시 조회하여 auditing 필드(createdAt, updatedAt) 확실히 가져오기
                Student refreshedStudent = studentRepository.findById(savedStudent.getId())
                        .orElse(savedStudent); // fallback to savedStudent if somehow not found
                log.info("새 학생 등록 완료: 이름={}, 학생ID={}", request.name(), refreshedStudent.getId());
                return StudentDto.from(refreshedStudent);
            }

        } catch (Exception e) {
            log.error("학생 정보 Upsert 중 오류 발생: 이름={}, 오류={}", request.name(), e.getMessage(), e);
            throw new RuntimeException("학생 정보 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}