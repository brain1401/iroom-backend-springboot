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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 학생 엔티티
 * 
 * <p>
 * 학생의 기본 정보를 저장하는 엔티티입니다.
 * 이름, 전화번호, 생년월일을 통한 3-factor 인증에 사용됩니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "student")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Student {

    /**
     * 학생 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 학생 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 학생 전화번호
     */
    @Column(nullable = false, length = 20)
    private String phone;

    /**
     * 학생 생년월일
     */
    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;

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
     * 학생 정보 유효성 검증
     * 
     * @param name      이름
     * @param phone     전화번호
     * @param birthDate 생년월일
     * @return 검증 성공 여부
     */
    public boolean matches(String name, String phone, LocalDate birthDate) {
        return Objects.equals(this.name, name) &&
                Objects.equals(this.phone, phone) &&
                Objects.equals(this.birthDate, birthDate);
    }
}