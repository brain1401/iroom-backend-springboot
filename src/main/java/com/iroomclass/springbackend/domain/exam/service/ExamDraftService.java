package com.iroomclass.springbackend.domain.exam.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.entity.ExamDraft;
import com.iroomclass.springbackend.domain.exam.entity.ExamSelectedUnit;
import com.iroomclass.springbackend.domain.exam.entity.ExamDraftQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamDraftRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSelectedUnitRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamDraftQuestionRepository;
import com.iroomclass.springbackend.domain.question.entity.Question;
import com.iroomclass.springbackend.domain.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.repository.UnitRepository;
import com.iroomclass.springbackend.domain.exam.dto.ExamDraftCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.ExamDraftCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamDraftListResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamDraftDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamDraftUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 초안 관리 서비스
 * 
 * 시험지 초안 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamDraftService {
    
    private final ExamDraftRepository examDraftRepository;
    private final ExamSelectedUnitRepository examSelectedUnitRepository;
    private final ExamDraftQuestionRepository examDraftQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UnitRepository unitRepository;
    
    /**
     * 시험지 초안 생성
     * 
     * @param request 시험지 초안 생성 요청
     * @return 생성된 시험지 초안 정보
     */
    @Transactional
    public ExamDraftCreateResponse createExamDraft(ExamDraftCreateRequest request) {
        log.info("시험지 초안 생성 요청: 학년={}, 단원={}개, 문제={}개, 이름={}", 
            request.getGrade(), request.getUnitIds().size(), request.getTotalQuestions(), request.getExamName());
        
        // 1단계: 입력값 검증
        validateCreateRequest(request);
        
        // 2단계: 시험지 초안 생성
        ExamDraft examDraft = ExamDraft.builder()
            .examName(request.getExamName())
            .grade(request.getGrade())
            .totalQuestions(request.getTotalQuestions())
            .build();
        
        examDraft = examDraftRepository.save(examDraft);
        
        // 3단계: 선택된 단원들 저장
        List<ExamSelectedUnit> selectedUnits = new ArrayList<>();
        for (Long unitId : request.getUnitIds()) {
            Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId));
            
            ExamSelectedUnit selectedUnit = ExamSelectedUnit.builder()
                .examDraft(examDraft)
                .unit(unit)
                .build();
            
            selectedUnits.add(selectedUnit);
        }
        
        examSelectedUnitRepository.saveAll(selectedUnits);
        
        // 4단계: 랜덤 문제 선택 및 저장
        List<Question> selectedQuestions = selectRandomQuestions(request.getUnitIds(), request.getTotalQuestions());
        
        List<ExamDraftQuestion> examDraftQuestions = new ArrayList<>();
        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question question = selectedQuestions.get(i);
            // 배점 계산 (총 100점을 문제 수로 나누기)
            int points = 100 / request.getTotalQuestions();
            // 마지막 문제는 나머지 점수 추가
            if (i == selectedQuestions.size() - 1) {
                points += 100 % request.getTotalQuestions();
            }
            
            ExamDraftQuestion examDraftQuestion = ExamDraftQuestion.builder()
                .examDraft(examDraft)
                .question(question)
                .seqNo(i + 1)
                .points(points)
                .build();
            
            examDraftQuestions.add(examDraftQuestion);
        }
        
        examDraftQuestionRepository.saveAll(examDraftQuestions);
        
        log.info("시험지 초안 생성 완료: ID={}, 문제={}개", examDraft.getId(), selectedQuestions.size());
        
        return ExamDraftCreateResponse.builder()
            .examDraftId(examDraft.getId())
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .totalQuestions(examDraft.getTotalQuestions())
            .selectedUnitCount(selectedUnits.size())
            .build();
    }
    
    /**
     * 전체 시험지 초안 목록 조회
     * 
     * @return 모든 시험지 초안 목록 (최신순)
     */
    public ExamDraftListResponse getAllExamDrafts() {
        log.info("전체 시험지 초안 목록 조회 요청");
        
        List<ExamDraft> examDrafts = examDraftRepository.findAllByOrderByIdDesc();
        
        List<ExamDraftListResponse.ExamDraftInfo> examDraftInfos = new ArrayList<>();
        for (ExamDraft examDraft : examDrafts) {
            // 선택된 단원 수 조회
            long selectedUnitCount = examSelectedUnitRepository.countByExamDraftId(examDraft.getId());
            
            ExamDraftListResponse.ExamDraftInfo examDraftInfo = ExamDraftListResponse.ExamDraftInfo.builder()
                .examDraftId(examDraft.getId())
                .examName(examDraft.getExamName())
                .grade(examDraft.getGrade())
                .totalQuestions(examDraft.getTotalQuestions())
                .selectedUnitCount((int) selectedUnitCount)
                .build();
            
            examDraftInfos.add(examDraftInfo);
        }
        
        log.info("전체 시험지 초안 목록 조회 완료: {}개", examDraftInfos.size());
        
        return ExamDraftListResponse.builder()
            .grade(null) // 전체 목록이므로 학년 정보 없음
            .examDrafts(examDraftInfos)
            .totalCount(examDraftInfos.size())
            .build();
    }
    
    /**
     * 학년별 시험지 초안 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 초안 목록
     */
    public ExamDraftListResponse getExamDraftsByGrade(int grade) {
        log.info("학년 {} 시험지 초안 목록 조회 요청", grade);
        
        List<ExamDraft> examDrafts = examDraftRepository.findByGradeOrderByIdDesc(grade);
        
        List<ExamDraftListResponse.ExamDraftInfo> examDraftInfos = new ArrayList<>();
        for (ExamDraft examDraft : examDrafts) {
            // 선택된 단원 수 조회
            long selectedUnitCount = examSelectedUnitRepository.countByExamDraftId(examDraft.getId());
            
            ExamDraftListResponse.ExamDraftInfo examDraftInfo = ExamDraftListResponse.ExamDraftInfo.builder()
                .examDraftId(examDraft.getId())
                .examName(examDraft.getExamName())
                .grade(examDraft.getGrade())
                .totalQuestions(examDraft.getTotalQuestions())
                .selectedUnitCount((int) selectedUnitCount)
                .build();
            
            examDraftInfos.add(examDraftInfo);
        }
        
        log.info("학년 {} 시험지 초안 목록 조회 완료: {}개", grade, examDraftInfos.size());
        
        return ExamDraftListResponse.builder()
            .grade(grade)
            .examDrafts(examDraftInfos)
            .totalCount(examDraftInfos.size())
            .build();
    }
    
    /**
     * 시험지 초안 상세 조회
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 시험지 초안 상세 정보
     */
    public ExamDraftDetailResponse getExamDraftDetail(Long examDraftId) {
        log.info("시험지 초안 {} 상세 조회 요청", examDraftId);
        
        // 1단계: 시험지 초안 조회
        ExamDraft examDraft = examDraftRepository.findById(examDraftId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + examDraftId));
        
        // 2단계: 선택된 단원들 조회
        List<ExamSelectedUnit> selectedUnits = examSelectedUnitRepository.findByExamDraftId(examDraftId);
        List<ExamDraftDetailResponse.UnitInfo> unitInfos = new ArrayList<>();
        for (ExamSelectedUnit selectedUnit : selectedUnits) {
            Unit unit = selectedUnit.getUnit();
            ExamDraftDetailResponse.UnitInfo unitInfo = ExamDraftDetailResponse.UnitInfo.builder()
                .unitId(unit.getId())
                .unitName(unit.getUnitName())
                .build();
            unitInfos.add(unitInfo);
        }
        
        // 3단계: 선택된 문제들 조회
        List<ExamDraftQuestion> examDraftQuestions = examDraftQuestionRepository.findByExamDraftIdOrderBySeqNo(examDraftId);
        List<ExamDraftDetailResponse.QuestionInfo> questionInfos = new ArrayList<>();
        for (ExamDraftQuestion examDraftQuestion : examDraftQuestions) {
            Question question = examDraftQuestion.getQuestion();
            Unit unit = question.getUnit();
            
            ExamDraftDetailResponse.QuestionInfo questionInfo = ExamDraftDetailResponse.QuestionInfo.builder()
                .seqNo(examDraftQuestion.getSeqNo())
                .questionId(question.getId())
                .unitId(unit.getId())
                .unitName(unit.getUnitName())
                .difficulty(question.getDifficulty().name())
                .stem(question.getStem())
                .points(examDraftQuestion.getPoints())
                .build();
            questionInfos.add(questionInfo);
        }
        
        log.info("시험지 초안 {} 상세 조회 완료: 단원={}개, 문제={}개", 
            examDraftId, unitInfos.size(), questionInfos.size());
        
        return ExamDraftDetailResponse.builder()
            .examDraftId(examDraft.getId())
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .totalQuestions(examDraft.getTotalQuestions())
            .units(unitInfos)
            .questions(questionInfos)
            .build();
    }
    
    /**
     * 시험지 초안 수정 (문제 교체)
     * 
     * @param examDraftId 시험지 초안 ID
     * @param request 수정 요청
     * @return 수정된 시험지 초안 정보
     */
    @Transactional
    public ExamDraftDetailResponse updateExamDraft(Long examDraftId, ExamDraftUpdateRequest request) {
        log.info("시험지 초안 {} 수정 요청: 문제={}번 교체", examDraftId, request.getSeqNo());
        
        // 1단계: 시험지 초안 존재 확인
        ExamDraft examDraft = examDraftRepository.findById(examDraftId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + examDraftId));
        
        // 2단계: 교체할 문제 조회
        ExamDraftQuestion currentQuestion = examDraftQuestionRepository.findByExamDraftIdAndSeqNo(examDraftId, request.getSeqNo())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 번호입니다: " + request.getSeqNo()));
        
        // 3단계: 같은 단원에서 다른 문제 선택
        Question newQuestion = selectRandomQuestionFromUnit(currentQuestion.getQuestion().getUnit().getId(), currentQuestion.getQuestion().getId());
        
        // 4단계: 기존 문제 삭제 후 새 문제 생성
        examDraftQuestionRepository.delete(currentQuestion);
        
        ExamDraftQuestion newExamDraftQuestion = ExamDraftQuestion.builder()
            .examDraft(examDraft)
            .question(newQuestion)
            .seqNo(request.getSeqNo())
            .points(currentQuestion.getPoints())
            .build();
        
        examDraftQuestionRepository.save(newExamDraftQuestion);
        
        log.info("시험지 초안 {} 문제 교체 완료: {}번 문제", examDraftId, request.getSeqNo());
        
        // 5단계: 수정된 시험지 초안 상세 정보 반환
        return getExamDraftDetail(examDraftId);
    }
    
    /**
     * 입력값 검증
     */
    private void validateCreateRequest(ExamDraftCreateRequest request) {
        if (request.getGrade() < 1 || request.getGrade() > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3만 가능합니다.");
        }
        
        if (request.getTotalQuestions() < 1 || request.getTotalQuestions() > 30) {
            throw new IllegalArgumentException("문제 개수는 1~30개만 가능합니다.");
        }
        
        if (request.getUnitIds().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 단원을 선택해야 합니다.");
        }
        
        // 선택된 단원들의 문제 수 확인
        long totalAvailableQuestions = questionRepository.countByUnitIdIn(request.getUnitIds());
        if (totalAvailableQuestions < request.getTotalQuestions()) {
            throw new IllegalArgumentException(
                String.format("선택된 단원들에는 %d문제만 있는데 %d문제를 요청했습니다.", 
                    totalAvailableQuestions, request.getTotalQuestions()));
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
