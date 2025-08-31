package com.iroomclass.springbackend.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.user.entity.Admin;

/**
 * 관리자 데이터 접근 계층
 * 
 * <p>관리자 엔티티와 관련된 데이터베이스 작업을 담당합니다.
 * Spring Data JPA를 활용하여 기본적인 CRUD 작업을 자동으로 제공받습니다.</p>
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> { 

    /**
     * 아이디로 관리자 조회
     * 
     * @param username 관리자 아이디
     * @return 관리자 정보 (Optional)
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * 아이디 존재 여부 확인
     * 
     * @param username 관리자 아이디
     * @return 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * ID가 가장 작은(첫 번째) 관리자 조회
     * 
     * @return 첫 번째 관리자 정보 (Optional)
     */
    Optional<Admin> findFirstByOrderByIdAsc();
}