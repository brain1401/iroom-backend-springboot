package com.iroomclass.springbackend.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.dto.StudentLoginRequest;
import com.iroomclass.springbackend.domain.user.dto.StudentLoginResponse;
import com.iroomclass.springbackend.domain.user.entity.Student;
import com.iroomclass.springbackend.domain.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 로그인 서비스
 * 
 * 학생이 로그인할 수 있는 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LoginService {

    private final StudentRepository userRepository;

    /**
     * 학생 로그인 (3-factor 인증)
     * 이름, 전화번호, 생년월일로 본인 확인
     * 
     * @param request 학생 로그인 요청 (이름 + 전화번호 + 생년월일)
     * @return 로그인 성공 정보
     */
    public StudentLoginResponse login(StudentLoginRequest request) {
        log.info("3-factor 로그인 시도: 이름={}, 전화번호={}, 생년월일={}",
                request.name(), request.phone(), request.birthDate());

        // 3-factor 인증: 이름 + 전화번호 + 생년월일
        Student user = userRepository.findByNameAndPhoneAndBirthDate(
                request.name(), request.phone(), request.birthDate()).orElseThrow(
                        () -> new IllegalArgumentException(
                                "이름, 전화번호, 생년월일이 일치하지 않습니다."));

        log.info("학생 로그인 성공: ID={}, 이름={}, 학년={}",
                user.getId(), user.getName(), user.getGrade());

        return new StudentLoginResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getGrade(),
                user.getBirthDate(),
                "로그인에 성공했습니다.");
    }
}
