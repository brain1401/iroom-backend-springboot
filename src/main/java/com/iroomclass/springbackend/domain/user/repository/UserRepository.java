package com.iroomclass.springbackend.domain.user.repository;

import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 리포지토리
 * 
 * <p>사용자 엔티티에 대한 데이터베이스 접근 기능을 제공합니다.
 * 로그인, 사용자 조회, 역할별 검색 등을 지원합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * 사용자명으로 사용자 조회
     * 로그인 시 인증에 사용
     * 
     * @param username 사용자명
     * @return 사용자 Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 사용자명이 존재하는지 확인
     * 중복 검사에 사용
     * 
     * @param username 사용자명
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByUsername(String username);
    
    /**
     * 역할별 사용자 목록 조회
     * 
     * @param role 사용자 역할
     * @return 해당 역할의 사용자 목록
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 활성된 관리자 목록 조회
     * 로그인 가능한 관리자만 반환 (username, password가 있는 경우)
     * 
     * @return 로그인 가능한 관리자 목록
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.username IS NOT NULL AND u.password IS NOT NULL")
    List<User> findActiveAdmins();
    
    /**
     * 이름으로 사용자 검색
     * 차트 등에서 사용자 이름으로 검색
     * 
     * @param name 사용자 이름 (부분 매칭)
     * @return 매칭되는 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    /**
     * 학생 사용자 수 조회
     * 
     * @return 학생 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT'")
    Long countStudents();
    
    /**
     * 관리자 사용자 수 조회
     * 
     * @return 관리자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    Long countAdmins();
    
    /**
     * 이름과 전화번호로 사용자 조회
     * 학생 로그인 및 제출 이력 조회 시 사용
     * 
     * @param name 사용자 이름
     * @param phone 전화번호
     * @return 사용자 Optional
     */
    Optional<User> findByNameAndPhone(String name, String phone);
    
    /**
     * 이름, 전화번호, 생년월일로 사용자 조회
     * 학생 3-factor 인증 시 사용
     * 
     * @param name 사용자 이름
     * @param phone 전화번호
     * @param birthDate 생년월일
     * @return 사용자 Optional
     */
    Optional<User> findByNameAndPhoneAndBirthDate(String name, String phone, LocalDate birthDate);
    
    /**
     * 이름, 전화번호, 생년월일, 역할로 사용자 조회
     * 학생 3-factor 인증 시 역할까지 확인하여 정확한 매칭
     * 
     * @param name 사용자 이름
     * @param phone 전화번호
     * @param birthDate 생년월일
     * @param role 사용자 역할
     * @return 사용자 Optional
     */
    Optional<User> findByNameAndPhoneAndBirthDateAndRole(String name, String phone, LocalDate birthDate, UserRole role);
    
    /**
     * 역할별 사용자 수 조회
     * 
     * @param role 사용자 역할
     * @return 해당 역할의 사용자 수
     */
    long countByRole(UserRole role);
    
    /**
     * Refresh Token으로 사용자 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return 사용자 Optional
     */
    Optional<User> findByRefreshToken(String refreshToken);
}