package com.iroomclass.springbackend.domain.user.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.user.info.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

/**
 * 사용자 Repository
 * 
 * 사용자 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * 이름과 전화번호로 사용자 조회
     * 
     * 사용처: 학생 로그인 시 본인 확인
     * 예시: "김철수" + "010-1234-5678" → 해당 학생 정보 조회
     * 
     * @param name 사용자 이름
     * @param phone 사용자 전화번호
     * @return 해당 사용자 정보 (Optional)
     */
    Optional<User> findByNameAndPhone(String name, String phone);

    /**
     * 전화번호로 사용자 조회
     * 
     * 사용처: 전화번호 중복 확인
     * 예시: "010-1234-5678" → 해당 전화번호로 가입된 학생 조회
     * 
     * @param phone 사용자 전화번호
     * @return 해당 전화번호의 사용자 정보 (Optional)
     */
    Optional<User> findByPhone(String phone);

    /**
     * 이름으로 사용자 목록 조회
     * 
     * 사용처: 동명이인 학생들 조회
     * 예시: "김철수" → "김철수"라는 이름의 모든 학생들 조회
     * 
     * @param name 사용자 이름
     * @return 해당 이름의 사용자 목록
     */
    List<User> findByName(String name);

    /**
     * 전화번호 존재 여부 확인
     * 
     * 사용처: 학생 등록 시 전화번호 중복 체크
     * 예시: "010-1234-5678" → 이미 등록된 전화번호인지 확인
     * 
     * @param phone 사용자 전화번호
     * @return 존재 여부 (true: 존재함, false: 존재하지 않음)
     */
    boolean existsByPhone(String phone);

    /**
     * 이름, 전화번호, 생년월일로 사용자 조회 (3-factor 인증)
     * 
     * 사용처: 학생 로그인 시 3단계 본인 확인
     * 예시: "김철수" + "010-1234-5678" + "2008-03-15" → 해당 학생 정보 조회
     * 
     * @param name 사용자 이름
     * @param phone 사용자 전화번호
     * @param birthDate 사용자 생년월일
     * @return 해당 사용자 정보 (Optional)
     */
    Optional<User> findByNameAndPhoneAndBirthDate(String name, String phone, LocalDate birthDate);
}
