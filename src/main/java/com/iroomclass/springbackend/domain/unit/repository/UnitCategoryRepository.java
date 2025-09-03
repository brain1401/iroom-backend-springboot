package com.iroomclass.springbackend.domain.unit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.unit.entity.UnitCategory;

/**
 * 단원 대분류 Repository
 * 
 * 단원 대분류 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface UnitCategoryRepository extends JpaRepository<UnitCategory, UUID> {

    /**
     * 모든 대분류를 표시 순서로 조회
     * 
     * @return 표시 순서로 정렬된 대분류 목록
     */
    List<UnitCategory> findAllByOrderByDisplayOrder();
}