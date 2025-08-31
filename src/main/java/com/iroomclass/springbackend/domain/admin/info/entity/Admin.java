package com.iroomclass.springbackend.domain.admin.info.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import com.iroomclass.springbackend.common.UUIDv7Generator;

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
     * UUIDv7로 생성되는 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

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
    
    /**
     * 관리자 이름
     */
    @Column(length = 50)
    private String name;
    
    /**
     * 관리자 이메일
     */
    @Column(length = 100)
    private String email;
    
    /**
     * 엔티티 저장 전 UUID 자동 생성
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUIDv7Generator.generate();
        }
    }
    
    /**
     * 관리자 이름 반환
     * 
     * @return 관리자 이름
     */
    public String getName() {
        return name;
    }
    
    /**
     * 관리자 이메일 반환
     * 
     * @return 관리자 이메일
     */
    public String getEmail() {
        return email;
    }
    
}
