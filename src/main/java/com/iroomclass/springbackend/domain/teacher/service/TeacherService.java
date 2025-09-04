package com.iroomclass.springbackend.domain.teacher.service;

import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import com.iroomclass.springbackend.domain.auth.repository.TeacherRepository;
import com.iroomclass.springbackend.domain.teacher.dto.LoginRequest;
import com.iroomclass.springbackend.domain.teacher.dto.LoginResponse;
import com.iroomclass.springbackend.domain.teacher.exception.TeacherNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 선생님 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;

    /**
     * 선생님 로그인 처리
     *
     * @param request 로그인 요청 정보
     * @return 로그인 응답 DTO
     * @throws TeacherNotFoundException 선생님을 찾을 수 없거나 인증에 실패한 경우
     */
    public LoginResponse login(LoginRequest request) {
        log.info("선생님 로그인 시도: username={}", request.username());
        
        Teacher teacher = teacherRepository.findByUsernameAndPassword(
                request.username(), 
                request.password()
        ).orElseThrow(() -> {
            log.warn("선생님 로그인 실패: 사용자명 또는 비밀번호가 일치하지 않습니다. username={}", request.username());
            return new TeacherNotFoundException("사용자명 또는 비밀번호가 일치하지 않습니다");
        });
        
        log.info("선생님 로그인 성공: id={}, username={}", teacher.getId(), teacher.getUsername());
        return LoginResponse.from(teacher);
    }
}