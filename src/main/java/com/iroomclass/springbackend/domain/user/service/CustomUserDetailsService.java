package com.iroomclass.springbackend.domain.user.service;

import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 세부 정보 서비스
 * 
 * <p>Spring Security에서 사용자 인증 시 사용되는 UserDetailsService 구현체입니다.
 * 데이터베이스에서 사용자 정보를 로드하여 UserDetails 객체를 반환합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * 사용자명으로 사용자 상세 정보 로드
     * 
     * @param username 사용자명
     * @return UserDetails 구현체 (User 엔티티)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 로드 시도: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: {}", username);
                    return new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + username
                    );
                });
        
        // 관리자가 아닌 경우 로그인 방지
        if (!user.canLogin()) {
            log.warn("로그인 방지된 사용자: {} (역할: {})", username, user.getRole());
            throw new UsernameNotFoundException(
                "로그인 권한이 없는 사용자입니다: " + username
            );
        }
        
        log.debug("사용자 로드 성공: {} (역할: {})", username, user.getRole());
        return user;
    }
}