package com.iroomclass.springbackend.domain.user.info.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.info.dto.UserLoginRequest;
import com.iroomclass.springbackend.domain.user.info.dto.UserLoginResponse;
import com.iroomclass.springbackend.domain.user.info.entity.User;
import com.iroomclass.springbackend.domain.user.info.repository.UserRepository;

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
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 학생 로그인
     * 
     * @param request 학생 로그인 요청
     * @return 로그인 성공 정보
     */
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("학생 로그인 요청: 이름={}, 전화번호={}", request.name(), request.phone());
        
        // 1단계: 학생 존재 확인
        User user = userRepository.findByNameAndPhone(request.name(), request.phone())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다. 이름과 전화번호를 확인해주세요."));
        
        log.info("학생 로그인 성공: ID={}, 이름={}", user.getId(), user.getName());
        
        return new UserLoginResponse(
            user.getId(),
            user.getName(),
            user.getPhone(),
            "로그인에 성공했습니다."
        );
    }
}
