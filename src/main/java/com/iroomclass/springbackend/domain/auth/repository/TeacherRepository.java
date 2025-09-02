package com.iroomclass.springbackend.domain.auth.repository;

import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 선생님 레포지토리
 * 
 * <p>
 * 선생님 엔티티에 대한 데이터 접근을 담당하는 레포지토리입니다.
 * 선생님 인증을 위한 사용자명/비밀번호 조회 메서드를 제공합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * 사용자명으로 선생님 조회
     * 
     * @param username 사용자명
     * @return 조건에 일치하는 선생님 (Optional)
     */
    Optional<Teacher> findByUsername(String username);
    
    /**
     * 사용자명과 비밀번호로 선생님 조회
     * 
     * <p>
     * 선생님 인증을 위한 사용자명/비밀번호 검증에 사용됩니다.
     * 두 조건이 모두 일치하는 선생님을 조회합니다.
     * </p>
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return 조건에 일치하는 선생님 (Optional)
     */
    Optional<Teacher> findByUsernameAndPassword(String username, String password);
}