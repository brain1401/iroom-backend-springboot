package com.iroomclass.springbackend.domain.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 엔티티
 * 
 * <p>학원 관리 시스템의 관리자 계정 정보를 저장합니다.
 * 아이디와 비밀번호만으로 간단한 로그인을 지원합니다.</p>
 */
@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Admin {

    /**
     * 관리자 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 관리자 아이디 (로그인용)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 관리자 비밀번호 (암호화된 상태로 저장)
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 학원명
     */
    @Column(length = 100)
    private String academyName;
    
}
