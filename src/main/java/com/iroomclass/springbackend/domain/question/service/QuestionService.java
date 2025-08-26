package com.iroomclass.springbackend.domain.question.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.question.entity.Question;
import com.iroomclass.springbackend.domain.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.repository.UnitRepository;
import com.iroomclass.springbackend.domain.question.dto.QuestionListResponse;
import com.iroomclass.springbackend.domain.question.dto.QuestionDetailResponse;
import com.iroomclass.springbackend.domain.question.dto.QuestionStatisticsResponse;
import com.iroomclass.springbackend.domain.question.dto.QuestionSearchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 문제 관리 서비스
 * 
 * 문제 목록 조회, 상세 조회, 통계, 검색 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final UnitRepository unitRepository;
    
    /**
     * 단원별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @return 해당 단원의 문제 목록
     */
    public QuestionListResponse getQuestionsByUnit(Long unitId) {
        log.info("단원 {} 문제 목록 조회 요청", unitId);
        
        // 1단계: 단원 존재 여부 확인
        Optional<Unit> unitOpt = unitRepository.findById(unitId);
        if (unitOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId);
        }
        
        Unit unit = unitOpt.get();
        
        // 2단계: 해당 단원의 모든 문제 조회
        List<Question> questions = questionRepository.findByUnitId(unitId);
        
        // 3단계: DTO 변환
        List<QuestionListResponse.QuestionInfo> questionInfos = questions.stream()
            .map(question -> QuestionListResponse.QuestionInfo.builder()
                .questionId(question.getId())
                .difficulty(question.getDifficulty().name())
                .build())
            .collect(Collectors.toList());
        
        // 4단계: 통계 계산
        int totalQuestions = questionInfos.size();
        long easyCount = questionInfos.stream()
            .filter(q -> "하".equals(q.getDifficulty()))
            .count();
        long mediumCount = questionInfos.stream()
            .filter(q -> "중".equals(q.getDifficulty()))
            .count();
        long hardCount = questionInfos.stream()
            .filter(q -> "상".equals(q.getDifficulty()))
            .count();
        
        log.info("단원 {} 문제 목록 조회 완료: {}개 문제", unitId, totalQuestions);
        
        return QuestionListResponse.builder()
            .unitId(unitId)
            .unitName(unit.getUnitName())
            .questions(questionInfos)
            .totalQuestions(totalQuestions)
            .easyCount((int) easyCount)
            .mediumCount((int) mediumCount)
            .hardCount((int) hardCount)
            .build();
    }
    
    /**
     * 난이도별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @param difficulty 난이도 (하, 중, 상)
     * @return 해당 단원의 특정 난이도 문제 목록
     */
    public QuestionListResponse getQuestionsByUnitAndDifficulty(Long unitId, String difficulty) {
        log.info("단원 {} 난이도 {} 문제 목록 조회 요청", unitId, difficulty);
        
        // 1단계: 단원 존재 여부 확인
        Optional<Unit> unitOpt = unitRepository.findById(unitId);
        if (unitOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId);
        }
        
        Unit unit = unitOpt.get();
        
        // 2단계: 난이도 enum 변환
        Question.Difficulty difficultyEnum;
        try {
            difficultyEnum = Question.Difficulty.valueOf(difficulty);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 난이도입니다: " + difficulty);
        }
        
        // 3단계: 해당 단원의 특정 난이도 문제 조회
        List<Question> questions = questionRepository.findByUnitIdAndDifficulty(unitId, difficultyEnum);
        
        // 4단계: DTO 변환
        List<QuestionListResponse.QuestionInfo> questionInfos = questions.stream()
            .map(question -> QuestionListResponse.QuestionInfo.builder()
                .questionId(question.getId())
                .difficulty(question.getDifficulty().name())
                .build())
            .collect(Collectors.toList());
        
        // 5단계: 통계 계산
        int totalQuestions = questionInfos.size();
        
        log.info("단원 {} 난이도 {} 문제 목록 조회 완료: {}개 문제", unitId, difficulty, totalQuestions);
        
        return QuestionListResponse.builder()
            .unitId(unitId)
            .unitName(unit.getUnitName())
            .questions(questionInfos)
            .totalQuestions(totalQuestions)
            .easyCount("하".equals(difficulty) ? totalQuestions : 0)
            .mediumCount("중".equals(difficulty) ? totalQuestions : 0)
            .hardCount("상".equals(difficulty) ? totalQuestions : 0)
            .build();
    }
    
    /**
     * 문제 상세 조회
     * 
     * @param questionId 문제 ID
     * @return 문제 상세 정보
     */
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        log.info("문제 {} 상세 조회 요청", questionId);
        
        // 1단계: 문제 조회
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 문제입니다: " + questionId);
        }
        
        Question question = questionOpt.get();
        
        // 2단계: 단원 정보 조회
        Unit unit = question.getUnit();
        
        log.info("문제 {} 상세 조회 완료", questionId);
        
        return QuestionDetailResponse.builder()
            .questionId(question.getId())
            .unitId(unit.getId())
            .unitName(unit.getUnitName())
            .difficulty(question.getDifficulty().name())
            .stem(question.getStem())
            .answerKey(question.getAnswerKey())
            .build();
    }
    
    /**
     * 단원별 문제 통계 조회
     * 
     * @param unitId 단원 ID
     * @return 단원별 문제 통계
     */
    public QuestionStatisticsResponse getQuestionStatisticsByUnit(Long unitId) {
        log.info("단원 {} 문제 통계 조회 요청", unitId);
        
        // 1단계: 단원 존재 여부 확인
        Optional<Unit> unitOpt = unitRepository.findById(unitId);
        if (unitOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId);
        }
        
        Unit unit = unitOpt.get();
        
        // 2단계: 난이도별 문제 수 조회
        long easyCount = questionRepository.countByUnitIdAndDifficulty(unitId, Question.Difficulty.하);
        long mediumCount = questionRepository.countByUnitIdAndDifficulty(unitId, Question.Difficulty.중);
        long hardCount = questionRepository.countByUnitIdAndDifficulty(unitId, Question.Difficulty.상);
        
        int totalQuestions = (int) (easyCount + mediumCount + hardCount);
        
        log.info("단원 {} 문제 통계 조회 완료: 총 {}개 (하: {}, 중: {}, 상: {})", 
            unitId, totalQuestions, easyCount, mediumCount, hardCount);
        
        return QuestionStatisticsResponse.builder()
            .unitId(unitId)
            .unitName(unit.getUnitName())
            .totalQuestions(totalQuestions)
            .easyCount((int) easyCount)
            .mediumCount((int) mediumCount)
            .hardCount((int) hardCount)
            .build();
    }
    
    /**
     * 문제 검색
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 문제 목록
     */
    public QuestionSearchResponse searchQuestions(String keyword) {
        log.info("문제 검색 요청: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드를 입력해주세요.");
        }
        
        // 1단계: 키워드로 문제 검색 (문제 내용에서 검색)
        List<Question> questions = questionRepository.findByStemContaining(keyword.trim());
        
        // 2단계: DTO 변환
        List<QuestionSearchResponse.QuestionInfo> questionInfos = questions.stream()
            .map(question -> {
                Unit unit = question.getUnit();
                return QuestionSearchResponse.QuestionInfo.builder()
                    .questionId(question.getId())
                    .unitId(unit.getId())
                    .unitName(unit.getUnitName())
                    .difficulty(question.getDifficulty().name())
                    .stem(question.getStem())
                    .build();
            })
            .collect(Collectors.toList());
        
        int totalResults = questionInfos.size();
        
        log.info("문제 검색 완료: {}개 결과", totalResults);
        
        return QuestionSearchResponse.builder()
            .keyword(keyword)
            .questions(questionInfos)
            .totalResults(totalResults)
            .build();
    }
}
