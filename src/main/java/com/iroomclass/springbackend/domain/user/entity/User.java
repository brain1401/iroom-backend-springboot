package com.iroomclass.springbackend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 Entity
 * 
 * 시험을 치르는 학생들의 기본 정보를 관리합니다.
 * 이름과 전화번호만 저장하는 간단한 구조입니다.
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
}