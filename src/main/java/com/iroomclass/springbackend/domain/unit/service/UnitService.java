package com.iroomclass.springbackend.domain.unit.service;

import com.iroomclass.springbackend.domain.unit.dto.QuestionDto;
import com.iroomclass.springbackend.domain.unit.dto.UnitTreeNode;
import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.unit.entity.UnitSubcategory;
import com.iroomclass.springbackend.domain.unit.repository.UnitCategoryRepository;
import com.iroomclass.springbackend.domain.unit.repository.UnitRepository;
import com.iroomclass.springbackend.domain.unit.repository.UnitSubcategoryRepository;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 단원 관리 서비스
 * 
 * 교육과정의 계층적 단원 구조 (대분류 → 중분류 → 세부단원) 관리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnitService {

    private final UnitCategoryRepository unitCategoryRepository;
    private final UnitSubcategoryRepository unitSubcategoryRepository;
    private final UnitRepository unitRepository;
    private final QuestionRepository questionRepository;

    /**
     * 전체 단원 트리 구조 조회
     * 
     * 대분류 → 중분류 → 세부단원의 계층적 구조를 트리 형태로 반환합니다.
     * 각 레벨은 displayOrder 순서로 정렬됩니다.
     * 
     * @return 전체 단원 트리 구조
     */
    public List<UnitTreeNode> getAllUnitsAsTree() {
        log.info("전체 단원 트리 구조 조회 시작");
        
        // 1. 모든 대분류를 displayOrder 순으로 조회
        List<UnitCategory> categories = unitCategoryRepository.findAllByOrderByDisplayOrder();
        log.debug("대분류 조회 완료: {} 개", categories.size());
        
        // 2. 각 대분류에 대해 하위 구조 구성
        List<UnitTreeNode> categoryNodes = categories.stream()
            .map(this::buildCategoryNode)
            .collect(Collectors.toList());
        
        log.info("전체 단원 트리 구조 조회 완료: 대분류 {} 개", categoryNodes.size());
        return categoryNodes;
    }
    
    /**
     * 학년별 단원 트리 구조 조회
     * 
     * 특정 학년의 세부단원만 포함된 트리 구조를 반환합니다.
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 단원 트리 구조
     */
    public List<UnitTreeNode> getUnitsByGradeAsTree(Integer grade) {
        log.info("학년별 단원 트리 구조 조회 시작: grade={}", grade);
        
        if (grade == null || grade < 1 || grade > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3 중 하나여야 합니다: " + grade);
        }
        
        // 1. 모든 대분류 조회
        List<UnitCategory> categories = unitCategoryRepository.findAllByOrderByDisplayOrder();
        
        // 2. 각 대분류에 대해 해당 학년의 하위 구조만 구성
        List<UnitTreeNode> categoryNodes = categories.stream()
            .map(category -> buildCategoryNodeForGrade(category, grade))
            .filter(node -> !node.children().isEmpty()) // 하위 노드가 있는 대분류만 포함
            .collect(Collectors.toList());
        
        log.info("학년별 단원 트리 구조 조회 완료: grade={}, 대분류 {} 개", grade, categoryNodes.size());
        return categoryNodes;
    }
    
    /**
     * 대분류 노드와 전체 하위 구조 생성
     *
     * @param category 대분류 엔티티
     * @return 하위 구조가 포함된 대분류 노드
     */
    private UnitTreeNode buildCategoryNode(UnitCategory category) {
        // 해당 대분류의 중분류들 조회
        List<UnitSubcategory> subcategories = unitSubcategoryRepository
            .findByCategoryOrderByDisplayOrder(category);
        
        // 각 중분류에 대해 하위 세부단원들과 함께 노드 생성
        List<UnitTreeNode> subcategoryNodes = subcategories.stream()
            .map(this::buildSubcategoryNode)
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromCategory(category, subcategoryNodes);
    }
    
    /**
     * 대분류 노드와 특정 학년의 하위 구조 생성
     *
     * @param category 대분류 엔티티
     * @param grade 학년
     * @return 해당 학년의 하위 구조가 포함된 대분류 노드
     */
    private UnitTreeNode buildCategoryNodeForGrade(UnitCategory category, Integer grade) {
        // 해당 대분류의 중분류들 조회
        List<UnitSubcategory> subcategories = unitSubcategoryRepository
            .findByCategoryOrderByDisplayOrder(category);
        
        // 각 중분류에 대해 해당 학년의 하위 세부단원들과 함께 노드 생성
        List<UnitTreeNode> subcategoryNodes = subcategories.stream()
            .map(subcategory -> buildSubcategoryNodeForGrade(subcategory, grade))
            .filter(node -> !node.children().isEmpty()) // 하위 노드가 있는 중분류만 포함
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromCategory(category, subcategoryNodes);
    }
    
    /**
     * 중분류 노드와 전체 하위 세부단원들 생성
     *
     * @param subcategory 중분류 엔티티
     * @return 하위 세부단원들이 포함된 중분류 노드
     */
    private UnitTreeNode buildSubcategoryNode(UnitSubcategory subcategory) {
        // 해당 중분류의 세부단원들 조회
        List<Unit> units = unitRepository.findBySubcategoryOrderByDisplayOrder(subcategory);
        
        // 각 세부단원을 리프 노드로 변환
        List<UnitTreeNode> unitNodes = units.stream()
            .map(UnitTreeNode::fromUnit)
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromSubcategory(subcategory, unitNodes);
    }
    
    /**
     * 중분류 노드와 특정 학년의 하위 세부단원들 생성
     *
     * @param subcategory 중분류 엔티티
     * @param grade 학년
     * @return 해당 학년의 하위 세부단원들이 포함된 중분류 노드
     */
    private UnitTreeNode buildSubcategoryNodeForGrade(UnitSubcategory subcategory, Integer grade) {
        // 해당 중분류의 특정 학년 세부단원들 조회
        List<Unit> units = unitRepository.findBySubcategoryOrderByDisplayOrder(subcategory)
            .stream()
            .filter(unit -> grade.equals(unit.getGrade()))
            .collect(Collectors.toList());
        
        // 각 세부단원을 리프 노드로 변환
        List<UnitTreeNode> unitNodes = units.stream()
            .map(UnitTreeNode::fromUnit)
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromSubcategory(subcategory, unitNodes);
    }
    
    /**
     * 학년별 단원 목록 조회 (플랫 구조)
     * 
     * 시험지 등록 등에서 사용할 수 있는 평면적인 단원 목록을 반환합니다.
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 세부단원 목록
     */
    public List<UnitTreeNode> getUnitsByGrade(Integer grade) {
        log.info("학년별 단원 목록 조회 시작: grade={}", grade);
        
        if (grade == null || grade < 1 || grade > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3 중 하나여야 합니다: " + grade);
        }
        
        List<Unit> units = unitRepository.findByGradeOrderByDisplayOrder(grade);
        
        List<UnitTreeNode> unitNodes = units.stream()
            .map(UnitTreeNode::fromUnit)
            .collect(Collectors.toList());
        
        log.info("학년별 단원 목록 조회 완료: grade={}, 단원 {} 개", grade, unitNodes.size());
        return unitNodes;
    }
    
    /**
     * 전체 단원 트리 구조 조회 (문제 정보 포함)
     * 
     * 대분류 → 중분류 → 세부단원의 계층적 구조를 트리 형태로 반환하며,
     * 각 세부단원에는 해당하는 문제들이 포함됩니다.
     * N+1 문제를 방지하기 위해 배치 쿼리를 사용합니다.
     * 
     * @return 전체 단원 트리 구조 (문제 정보 포함)
     */
    public List<UnitTreeNode> getAllUnitsAsTreeWithQuestions() {
        log.info("전체 단원 트리 구조 조회 시작 (문제 정보 포함)");
        
        // 1. 모든 대분류를 displayOrder 순으로 조회
        List<UnitCategory> categories = unitCategoryRepository.findAllByOrderByDisplayOrder();
        log.debug("대분류 조회 완료: {} 개", categories.size());
        
        // 2. 각 대분류에 대해 하위 구조 구성 (문제 포함)
        List<UnitTreeNode> categoryNodes = categories.stream()
            .map(this::buildCategoryNodeWithQuestions)
            .collect(Collectors.toList());
        
        log.info("전체 단원 트리 구조 조회 완료 (문제 정보 포함): 대분류 {} 개", categoryNodes.size());
        return categoryNodes;
    }
    
    /**
     * 학년별 단원 트리 구조 조회 (문제 정보 포함)
     * 
     * 특정 학년의 세부단원만 포함된 트리 구조를 반환하며,
     * 각 세부단원에는 해당하는 문제들이 포함됩니다.
     * N+1 문제를 방지하기 위해 배치 쿼리를 사용합니다.
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 단원 트리 구조 (문제 정보 포함)
     */
    public List<UnitTreeNode> getUnitsByGradeAsTreeWithQuestions(Integer grade) {
        log.info("학년별 단원 트리 구조 조회 시작 (문제 정보 포함): grade={}", grade);
        
        if (grade == null || grade < 1 || grade > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3 중 하나여야 합니다: " + grade);
        }
        
        // 1. 모든 대분류 조회
        List<UnitCategory> categories = unitCategoryRepository.findAllByOrderByDisplayOrder();
        
        // 2. 각 대분류에 대해 해당 학년의 하위 구조만 구성 (문제 포함)
        List<UnitTreeNode> categoryNodes = categories.stream()
            .map(category -> buildCategoryNodeForGradeWithQuestions(category, grade))
            .filter(node -> !node.children().isEmpty()) // 하위 노드가 있는 대분류만 포함
            .collect(Collectors.toList());
        
        log.info("학년별 단원 트리 구조 조회 완료 (문제 정보 포함): grade={}, 대분류 {} 개", grade, categoryNodes.size());
        return categoryNodes;
    }
    
    /**
     * 대분류 노드와 전체 하위 구조 생성 (문제 정보 포함)
     *
     * @param category 대분류 엔티티
     * @return 하위 구조가 포함된 대분류 노드 (문제 정보 포함)
     */
    private UnitTreeNode buildCategoryNodeWithQuestions(UnitCategory category) {
        // 해당 대분류의 중분류들 조회
        List<UnitSubcategory> subcategories = unitSubcategoryRepository
            .findByCategoryOrderByDisplayOrder(category);
        
        // 각 중분류에 대해 하위 세부단원들과 함께 노드 생성 (문제 포함)
        List<UnitTreeNode> subcategoryNodes = subcategories.stream()
            .map(this::buildSubcategoryNodeWithQuestions)
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromCategory(category, subcategoryNodes);
    }
    
    /**
     * 대분류 노드와 특정 학년의 하위 구조 생성 (문제 정보 포함)
     *
     * @param category 대분류 엔티티
     * @param grade 학년
     * @return 해당 학년의 하위 구조가 포함된 대분류 노드 (문제 정보 포함)
     */
    private UnitTreeNode buildCategoryNodeForGradeWithQuestions(UnitCategory category, Integer grade) {
        // 해당 대분류의 중분류들 조회
        List<UnitSubcategory> subcategories = unitSubcategoryRepository
            .findByCategoryOrderByDisplayOrder(category);
        
        // 각 중분류에 대해 해당 학년의 하위 세부단원들과 함께 노드 생성 (문제 포함)
        List<UnitTreeNode> subcategoryNodes = subcategories.stream()
            .map(subcategory -> buildSubcategoryNodeForGradeWithQuestions(subcategory, grade))
            .filter(node -> !node.children().isEmpty()) // 하위 노드가 있는 중분류만 포함
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromCategory(category, subcategoryNodes);
    }
    
    /**
     * 중분류 노드와 전체 하위 세부단원들 생성 (문제 정보 포함)
     *
     * @param subcategory 중분류 엔티티
     * @return 하위 세부단원들이 포함된 중분류 노드 (문제 정보 포함)
     */
    private UnitTreeNode buildSubcategoryNodeWithQuestions(UnitSubcategory subcategory) {
        // 해당 중분류의 세부단원들 조회
        List<Unit> units = unitRepository.findBySubcategoryOrderByDisplayOrder(subcategory);
        
        if (units.isEmpty()) {
            return UnitTreeNode.fromSubcategory(subcategory, List.of());
        }
        
        // N+1 문제 방지: 한 번의 쿼리로 모든 문제 조회
        List<UUID> unitIds = units.stream().map(Unit::getId).collect(Collectors.toList());
        List<Question> questions = questionRepository.findByUnitIdIn(unitIds);
        
        // Unit ID별로 Question들을 그룹화
        Map<UUID, List<QuestionDto>> questionsByUnitId = questions.stream()
            .collect(Collectors.groupingBy(
                question -> question.getUnit().getId(),
                Collectors.mapping(QuestionDto::from, Collectors.toList())
            ));
        
        // 각 세부단원을 문제와 함께 노드로 변환
        List<UnitTreeNode> unitNodes = units.stream()
            .map(unit -> {
                List<QuestionDto> unitQuestions = questionsByUnitId.getOrDefault(unit.getId(), List.of());
                return UnitTreeNode.fromUnitWithQuestions(unit, unitQuestions);
            })
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromSubcategory(subcategory, unitNodes);
    }
    
    /**
     * 중분류 노드와 특정 학년의 하위 세부단원들 생성 (문제 정보 포함)
     *
     * @param subcategory 중분류 엔티티
     * @param grade 학년
     * @return 해당 학년의 하위 세부단원들이 포함된 중분류 노드 (문제 정보 포함)
     */
    private UnitTreeNode buildSubcategoryNodeForGradeWithQuestions(UnitSubcategory subcategory, Integer grade) {
        // 해당 중분류의 특정 학년 세부단원들 조회
        List<Unit> units = unitRepository.findBySubcategoryOrderByDisplayOrder(subcategory)
            .stream()
            .filter(unit -> grade.equals(unit.getGrade()))
            .collect(Collectors.toList());
        
        if (units.isEmpty()) {
            return UnitTreeNode.fromSubcategory(subcategory, List.of());
        }
        
        // N+1 문제 방지: 한 번의 쿼리로 모든 문제 조회
        List<UUID> unitIds = units.stream().map(Unit::getId).collect(Collectors.toList());
        List<Question> questions = questionRepository.findByUnitIdIn(unitIds);
        
        // Unit ID별로 Question들을 그룹화
        Map<UUID, List<QuestionDto>> questionsByUnitId = questions.stream()
            .collect(Collectors.groupingBy(
                question -> question.getUnit().getId(),
                Collectors.mapping(QuestionDto::from, Collectors.toList())
            ));
        
        // 각 세부단원을 문제와 함께 노드로 변환
        List<UnitTreeNode> unitNodes = units.stream()
            .map(unit -> {
                List<QuestionDto> unitQuestions = questionsByUnitId.getOrDefault(unit.getId(), List.of());
                return UnitTreeNode.fromUnitWithQuestions(unit, unitQuestions);
            })
            .collect(Collectors.toList());
        
        return UnitTreeNode.fromSubcategory(subcategory, unitNodes);
    }
}