package com.iroomclass.springbackend.domain.user.info.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * 사용자 Entity
 * 
 * 시험을 치르는 학생들의 기본 정보를 관리합니다.
 * 이름, 전화번호, 학년, 생년월일을 저장합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {
    
    /**
     * 사용자 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자 이름
     * 학생의 이름을 저장
     * 최대 50자, 필수 입력
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * 사용자 전화번호
     * 학생의 연락처 정보
     * 최대 20자, 선택 입력
     */
    @Column(length = 20)
    private String phone;
    
    /**
     * 학년
     * 1, 2, 3학년 중 하나
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer grade;
    
    /**
     * 생년월일
     * 학생의 생년월일
     * 필수 입력, 3-factor 인증에 사용
     */
    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;
}