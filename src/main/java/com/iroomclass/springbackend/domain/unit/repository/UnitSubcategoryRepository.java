package com.iroomclass.springbackend.domain.unit.repository;

import com.iroomclass.springbackend.domain.unit.entity.UnitSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 단원 중분류 Repository
 * 
 * 단원 중분류 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface UnitSubcategoryRepository extends JpaRepository<UnitSubcategory, Long> {
    // 기본 CRUD만 사용
    // 현재 UI에서는 학년 선택 후 바로 세부단원 선택하므로 
    // 별도 조회 메서드 불필요
}
