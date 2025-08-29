package com.iroomclass.springbackend.domain.admin.exam.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetUpdateRequest;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSelectedUnit;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSelectedUnitRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 관리 서비스
 * 
 * 시험지 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamSheetService {
    
    private final ExamSheetRepository examSheetRepository;
    private final ExamSelectedUnitRepository examSelectedUnitRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UnitRepository unitRepository;
    
    /**
     * 시험지 생성
     * 
     * @param request 시험지 생성 요청
     * @return 생성된 시험지 정보
     */
    @Transactional
    public ExamSheetCreateResponse createExamSheet(ExamSheetCreateRequest request) {
        log.info("시험지 생성 요청: 학년={}, 단원={}개, 문제={}개, 이름={}", 
            request.grade(), request.unitIds().size(), request.totalQuestions(), request.examName());
        
        // 1단계: 입력값 검증
        validateCreateRequest(request);
        
        // 2단계: 시험지 생성
        ExamSheet examSheet = ExamSheet.builder()
            .examName(request.examName())
            .grade(request.grade())
            .totalQuestions(request.totalQuestions())
            .build();
        
        examSheet = examSheetRepository.save(examSheet);
        
        // 3단계: 선택된 단원들 저장
        List<ExamSelectedUnit> selectedUnits = new ArrayList<>();
        for (Long unitId : request.unitIds()) {
            Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId));
            
            ExamSelectedUnit selectedUnit = ExamSelectedUnit.builder()
                .examSheet(examSheet)  // Note: ExamSelectedUnit needs to be updated to reference ExamSheet
                .unit(unit)
                .build();
            
            selectedUnits.add(selectedUnit);
        }
        
        examSelectedUnitRepository.saveAll(selectedUnits);
        
        // 4단계: 랜덤 문제 선택 및 저장
        List<Question> selectedQuestions = selectRandomQuestions(request.unitIds(), request.totalQuestions());
        
        List<ExamSheetQuestion> examSheetQuestions = new ArrayList<>();
        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question question = selectedQuestions.get(i);
            // 배점 계산 (총 100점을 문제 수로 나누기)
            int points = 100 / request.totalQuestions();
            // 마지막 문제는 나머지 점수 추가
            if (i == selectedQuestions.size() - 1) {
                points += 100 % request.totalQuestions();
            }
            
            ExamSheetQuestion examSheetQuestion = ExamSheetQuestion.builder()
                .examSheet(examSheet)
                .question(question)
                .seqNo(i + 1)
                .points(points)
                .build();
            
            examSheetQuestions.add(examSheetQuestion);
        }
        
        examSheetQuestionRepository.saveAll(examSheetQuestions);
        
        log.info("시험지 생성 완료: ID={}, 문제={}개", examSheet.getId(), selectedQuestions.size());
        
        return new ExamSheetCreateResponse(
            examSheet.getId(),
            examSheet.getExamName(),
            examSheet.getGrade(),
            examSheet.getTotalQuestions(),
            selectedUnits.size()
        );
    }
    
    /**
     * 전체 시험지 목록 조회
     * 
     * @return 모든 시험지 목록 (최신순)
     */
    public ExamSheetListResponse getAllExamSheets() {
        log.info("전체 시험지 목록 조회 요청");
        
        List<ExamSheet> examSheets = examSheetRepository.findAllByOrderByIdDesc();
        
        List<ExamSheetListResponse.ExamSheetInfo> examSheetInfos = new ArrayList<>();
        for (ExamSheet examSheet : examSheets) {
            // 선택된 단원 수 조회 (Note: method needs to be updated to use exam_sheet_id)
            long selectedUnitCount = examSelectedUnitRepository.countByExamSheetId(examSheet.getId());
            
            ExamSheetListResponse.ExamSheetInfo examSheetInfo = new ExamSheetListResponse.ExamSheetInfo(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                examSheet.getTotalQuestions(),
                (int) selectedUnitCount
            );
            
            examSheetInfos.add(examSheetInfo);
        }
        
        log.info("전체 시험지 목록 조회 완료: {}개", examSheetInfos.size());
        
        return new ExamSheetListResponse(
            null, // 전체 목록이므로 학년 정보 없음
            examSheetInfos,
            examSheetInfos.size()
        );
    }
    
    /**
     * 학년별 시험지 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 목록
     */
    public ExamSheetListResponse getExamSheetsByGrade(int grade) {
        log.info("학년 {} 시험지 목록 조회 요청", grade);
        
        List<ExamSheet> examSheets = examSheetRepository.findByGradeOrderByIdDesc(grade);
        
        List<ExamSheetListResponse.ExamSheetInfo> examSheetInfos = new ArrayList<>();
        for (ExamSheet examSheet : examSheets) {
            // 선택된 단원 수 조회 (Note: method needs to be updated to use exam_sheet_id)
            long selectedUnitCount = examSelectedUnitRepository.countByExamSheetId(examSheet.getId());
            
            ExamSheetListResponse.ExamSheetInfo examSheetInfo = new ExamSheetListResponse.ExamSheetInfo(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                examSheet.getTotalQuestions(),
                (int) selectedUnitCount
            );
            
            examSheetInfos.add(examSheetInfo);
        }
        
        log.info("학년 {} 시험지 목록 조회 완료: {}개", grade, examSheetInfos.size());
        
        return new ExamSheetListResponse(
            null, // 전체 목록이므로 학년 정보 없음
            examSheetInfos,
            examSheetInfos.size()
        );
    }
    
    /**
     * 시험지 상세 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 상세 정보
     */
    public ExamSheetDetailResponse getExamSheetDetail(Long examSheetId) {
        log.info("시험지 {} 상세 조회 요청", examSheetId);
        
        // 1단계: 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));
        
        // 2단계: 선택된 단원들 조회 (Note: method needs to be updated to use exam_sheet_id)
        List<ExamSelectedUnit> selectedUnits = examSelectedUnitRepository.findByExamSheetId(examSheetId);
        List<ExamSheetDetailResponse.UnitInfo> unitInfos = new ArrayList<>();
        for (ExamSelectedUnit selectedUnit : selectedUnits) {
            Unit unit = selectedUnit.getUnit();
            ExamSheetDetailResponse.UnitInfo unitInfo = new ExamSheetDetailResponse.UnitInfo(
                unit.getId(),
                unit.getUnitName()
            );
            unitInfos.add(unitInfo);
        }
        
        // 3단계: 선택된 문제들 조회
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository.findByExamSheetIdOrderBySeqNo(examSheetId);
        List<ExamSheetDetailResponse.QuestionInfo> questionInfos = new ArrayList<>();
        for (ExamSheetQuestion examSheetQuestion : examSheetQuestions) {
            Question question = examSheetQuestion.getQuestion();
            Unit unit = question.getUnit();
            
            ExamSheetDetailResponse.QuestionInfo questionInfo = new ExamSheetDetailResponse.QuestionInfo(
                examSheetQuestion.getSeqNo(),
                question.getId(),
                unit.getId(),
                unit.getUnitName(),
                question.getDifficulty().name(),
                question.getQuestionTextAsHtml(),
                examSheetQuestion.getPoints()
            );
            questionInfos.add(questionInfo);
        }
        
        log.info("시험지 {} 상세 조회 완료: 단원={}개, 문제={}개", 
            examSheetId, unitInfos.size(), questionInfos.size());
        
        return new ExamSheetDetailResponse(
            examSheet.getId(),
            examSheet.getExamName(),
            examSheet.getGrade(),
            examSheet.getTotalQuestions(),
            unitInfos,
            questionInfos
        );
    }
    
    /**
     * 시험지 수정 (문제 교체)
     * 
     * @param examSheetId 시험지 ID
     * @param request 수정 요청
     * @return 수정된 시험지 정보
     */
    @Transactional
    public ExamSheetDetailResponse updateExamSheet(Long examSheetId, ExamSheetUpdateRequest request) {
        log.info("시험지 {} 수정 요청: 문제={}번 교체", examSheetId, request.seqNo());
        
        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));
        
        // 2단계: 교체할 문제 조회
        ExamSheetQuestion currentQuestion = examSheetQuestionRepository.findByExamSheetIdAndSeqNo(examSheetId, request.seqNo())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 번호입니다: " + request.seqNo()));
        
        // 3단계: 같은 단원에서 다른 문제 선택
        Question newQuestion = selectRandomQuestionFromUnit(currentQuestion.getQuestion().getUnit().getId(), currentQuestion.getQuestion().getId());
        
        // 4단계: 기존 문제 삭제 후 새 문제 생성
        examSheetQuestionRepository.delete(currentQuestion);
        
        ExamSheetQuestion newExamSheetQuestion = ExamSheetQuestion.builder()
            .examSheet(examSheet)
            .question(newQuestion)
            .seqNo(request.seqNo())
            .points(currentQuestion.getPoints())
            .build();
        
        examSheetQuestionRepository.save(newExamSheetQuestion);
        
        log.info("시험지 {} 문제 교체 완료: {}번 문제", examSheetId, request.seqNo());
        
        // 5단계: 수정된 시험지 상세 정보 반환
        return getExamSheetDetail(examSheetId);
    }
    
    /**
     * 입력값 검증
     */
    private void validateCreateRequest(ExamSheetCreateRequest request) {
        if (request.grade() < 1 || request.grade() > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3만 가능합니다.");
        }
        
        if (request.totalQuestions() < 1 || request.totalQuestions() > 30) {
            throw new IllegalArgumentException("문제 개수는 1~30개만 가능합니다.");
        }
        
        if (request.unitIds().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 단원을 선택해야 합니다.");
        }
        
        // 선택된 단원들의 문제 수 확인
        long totalAvailableQuestions = questionRepository.countByUnitIdIn(request.unitIds());
        if (totalAvailableQuestions < request.totalQuestions()) {
            throw new IllegalArgumentException(
                String.format("선택된 단원들에는 %d문제만 있는데 %d문제를 요청했습니다.", 
                    totalAvailableQuestions, request.totalQuestions()));
        }
    }
    
    /**
     * 랜덤 문제 선택
     */
    private List<Question> selectRandomQuestions(List<Long> unitIds, int totalQuestions) {
        List<Question> allQuestions = questionRepository.findByUnitIdIn(unitIds);
        
        if (allQuestions.size() < totalQuestions) {
            throw new IllegalArgumentException("요청한 문제 수보다 사용 가능한 문제가 적습니다.");
        }
        
        // 랜덤 셔플 후 앞에서부터 선택
        java.util.Collections.shuffle(allQuestions);
        return allQuestions.subList(0, totalQuestions);
    }
    
    /**
     * 같은 단원에서 다른 문제 선택
     */
    private Question selectRandomQuestionFromUnit(Long unitId, Long excludeQuestionId) {
        List<Question> questions = questionRepository.findByUnitId(unitId);
        
        // 현재 문제 제외
        List<Question> availableQuestions = new ArrayList<>();
        for (Question question : questions) {
            if (!question.getId().equals(excludeQuestionId)) {
                availableQuestions.add(question);
            }
        }
        
        if (availableQuestions.isEmpty()) {
            throw new IllegalArgumentException("교체할 수 있는 문제가 없습니다.");
        }
        
        // 랜덤 선택
        java.util.Collections.shuffle(availableQuestions);
        return availableQuestions.get(0);
    }
}