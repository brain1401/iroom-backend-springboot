package com.iroomclass.springbackend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import com.iroomclass.springbackend.common.UUIDv7Generator;

/**
 * 사용자 엔티티
 * 
 * <p>
 * 시스템 내 모든 사용자(학생, 관리자)를 통합 관리하는 엔티티입니다.
 * Spring Security UserDetails 인터페이스를 구현하여 인증/인가를 지원합니다.
 * </p>
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
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    /**
     * 사용자 고유 ID
     * UUIDv7로 생성되는 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * 로그인 사용자명 (관리자만 사용)
     * 관리자 로그인 시 사용하는 고유한 식별자
     */
    @Column(unique = true, length = 50)
    private String username;

    /**
     * 로그인 비밀번호 (관리자만 사용)
     * BCrypt로 암호화되어 저장
     */
    @Column(length = 255)
    private String password;

    /**
     * 사용자 이름
     * 모든 사용자에게 공통으로 사용되는 실제 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 이메일 주소 (관리자만 사용)
     */
    @Column(length = 100)
    private String email;

    /**
     * 전화번호 (학생만 사용)
     */
    @Column(length = 20)
    private String phone;

    /**
     * 사용자 역할
     * STUDENT 또는 ADMIN
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 학년 (학생만 사용)
     * 1, 2, 3학년 중 하나
     */
    private Integer grade;

    /**
     * 생년월일 (학생만 사용)
     * 3-factor 인증에 활용
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 학원명 (관리자만 사용)
     */
    @Column(name = "academy_name", length = 100)
    private String academyName;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 엔티티 저장 전 UUID 자동 생성
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUIDv7Generator.generate();
        }
    }

    // ========== UserDetails 구현 ==========

    /**
     * 사용자의 권한 목록 반환
     * 역할에 따라 "ROLE_STUDENT" 또는 "ROLE_ADMIN" 권한 부여
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * 로그인 사용자명 반환
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 로그인 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 계정 만료 여부
     * 항상 false (만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     * 항상 false (잠금되지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명 만료 여부
     * 항상 false (만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     * 항상 true (활성화됨)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 관리자 여부 확인
     * 
     * @return 관리자이면 true, 그렇지 않으면 false
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    /**
     * 학생 여부 확인
     * 
     * @return 학생이면 true, 그렇지 않으면 false
     */
    public boolean isStudent() {
        return UserRole.STUDENT.equals(this.role);
    }

    /**
     * 로그인 가능 여부 확인
     * 관리자만 로그인 가능 (username, password가 있는 경우)
     * 
     * @return 로그인 가능하면 true, 그렇지 않으면 false
     */
    public boolean canLogin() {
        return isAdmin() && username != null && password != null;
    }
}