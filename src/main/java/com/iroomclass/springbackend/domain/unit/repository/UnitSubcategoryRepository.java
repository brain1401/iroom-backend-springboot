package com.iroomclass.springbackend.domain.unit.repository;

import java.util.List;

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

    /**
     * 대분류별 단원 중분류 조회
     * 
     * 사용처: 단원 계층 구조 조회 시 대분류 아래의 중분류 목록 표시
     * 예시: "수와 연산" 대분류 선택 → "정수와 유리수", "문자와 식" 등 표시
     * 
     * @param categoryId 대분류 ID
     * @return 해당 대분류에 속한 중분류 목록 (표시 순서대로 정렬)
     */
    List<UnitSubcategory> findByCategoryIdOrderByDisplayOrder(Long categoryId);
}
