package com.iroomclass.springbackend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 선생님 엔티티
 * 
 * <p>
 * 선생님의 기본 정보를 저장하는 엔티티입니다.
 * 사용자명과 비밀번호를 통한 인증에 사용됩니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "teachers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Teacher {
    
    /**
     * 선생님 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자명 (로그인용)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 비밀번호
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 선생님 인증 정보 유효성 검증
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return 검증 성공 여부
     */
    public boolean matches(String username, String password) {
        return Objects.equals(this.username, username) &&
               Objects.equals(this.password, password);
    }
}