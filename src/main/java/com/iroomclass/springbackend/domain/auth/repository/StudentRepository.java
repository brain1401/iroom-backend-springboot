package com.iroomclass.springbackend.domain.auth.repository;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 학생 레포지토리
 * 
 * <p>
 * 학생 엔티티에 대한 데이터 접근을 담당하는 레포지토리입니다.
 * 학생 인증을 위한 3-factor 조회 메서드를 제공합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * 이름과 전화번호로 학생 조회
     * 
     * @param name 학생 이름
     * @param phone 학생 전화번호
     * @return 조건에 일치하는 학생 (Optional)
     */
    Optional<Student> findByNameAndPhone(String name, String phone);
    
    /**
     * 이름, 전화번호, 생년월일로 학생 조회
     * 
     * <p>
     * 학생 인증을 위한 3-factor 검증에 사용됩니다.
     * 세 개의 조건이 모두 일치하는 학생을 조회합니다.
     * </p>
     * 
     * @param name 학생 이름
     * @param phone 학생 전화번호
     * @param birthDate 학생 생년월일
     * @return 조건에 일치하는 학생 (Optional)
     */
    Optional<Student> findByNameAndPhoneAndBirthDate(String name, String phone, LocalDate birthDate);
}