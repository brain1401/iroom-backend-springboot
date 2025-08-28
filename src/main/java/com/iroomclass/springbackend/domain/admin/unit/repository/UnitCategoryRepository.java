package com.iroomclass.springbackend.domain.admin.unit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;

/**
 * 단원 대분류 Repository
 * 
 * 단원 대분류 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface UnitCategoryRepository extends JpaRepository<UnitCategory, Long> {

    // UnitCategory는 대분류이므로 학년 정보가 없습니다.
    // 학년별 조회는 Unit 엔티티에서 처리합니다.
}