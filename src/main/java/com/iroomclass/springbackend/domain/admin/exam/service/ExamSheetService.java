package com.iroomclass.springbackend.domain.admin.exam.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetQuestionManageResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetPreviewResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetUpdateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionReplaceRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionSelectionRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.SelectableQuestionsResponse;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetSelectedUnit;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetSelectedUnitRepository;
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
    private final ExamSheetSelectedUnitRepository examSheetSelectedUnitRepository;
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
                .multipleChoiceCount(request.multipleChoiceCount())
                .subjectiveCount(request.subjectiveCount())
                .build();

        examSheet = examSheetRepository.save(examSheet);

        // 3단계: 선택된 단원들 저장
        List<ExamSheetSelectedUnit> selectedUnits = new ArrayList<>();
        for (UUID unitId : request.unitIds()) {
            Unit unit = unitRepository.findById(unitId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단원입니다: " + unitId));

            ExamSheetSelectedUnit selectedUnit = ExamSheetSelectedUnit.builder()
                    .examSheet(examSheet) // Note: ExamSheetSelectedUnit updated to reference ExamSheet
                    .unit(unit)
                    .build();

            selectedUnits.add(selectedUnit);
        }

        examSheetSelectedUnitRepository.saveAll(selectedUnits);

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
                selectedUnits.size());
    }

    /**
     * 전체 시험지 목록 조회
     * 
     * @return 모든 시험지 목록 (최신순)
     */
    public ExamSheetListResponse getAllExamSheets() {
        // TODO: 페이지네이션 필요!

        log.info("전체 시험지 목록 조회 요청");

        List<ExamSheet> examSheets = examSheetRepository.findAllByOrderByIdDesc();

        List<ExamSheetListResponse.ExamSheetInfo> examSheetInfos = new ArrayList<>();
        for (ExamSheet examSheet : examSheets) {
            // 선택된 단원 수 조회 (Note: method needs to be updated to use exam_sheet_id)
            long selectedUnitCount = examSheetSelectedUnitRepository.countByExamSheetId(examSheet.getId());

            ExamSheetListResponse.ExamSheetInfo examSheetInfo = new ExamSheetListResponse.ExamSheetInfo(
                    examSheet.getId(),
                    examSheet.getExamName(),
                    examSheet.getGrade(),
                    examSheet.getTotalQuestions(),
                    (int) selectedUnitCount);

            examSheetInfos.add(examSheetInfo);
        }

        log.info("전체 시험지 목록 조회 완료: {}개", examSheetInfos.size());

        return new ExamSheetListResponse(
                null, // 전체 목록이므로 학년 정보 없음
                examSheetInfos,
                examSheetInfos.size());
    }

    /**
     * 학년별 시험지 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 목록
     */
    public ExamSheetListResponse getExamSheetsByGrade(int grade) {
        log.info("학년 {} 시험지 목록 조회 요청", grade);

        // 학년 유효성 검증
        if (grade < 1 || grade > 3) {
            throw new IllegalArgumentException("학년은 1, 2, 3만 가능합니다: " + grade);
        }

        List<ExamSheet> examSheets = examSheetRepository.findByGradeOrderByIdDesc(grade);

        List<ExamSheetListResponse.ExamSheetInfo> examSheetInfos = new ArrayList<>();
        for (ExamSheet examSheet : examSheets) {
            // 선택된 단원 수 조회 (Note: method needs to be updated to use exam_sheet_id)
            long selectedUnitCount = examSheetSelectedUnitRepository.countByExamSheetId(examSheet.getId());

            ExamSheetListResponse.ExamSheetInfo examSheetInfo = new ExamSheetListResponse.ExamSheetInfo(
                    examSheet.getId(),
                    examSheet.getExamName(),
                    examSheet.getGrade(),
                    examSheet.getTotalQuestions(),
                    (int) selectedUnitCount);

            examSheetInfos.add(examSheetInfo);
        }

        log.info("학년 {} 시험지 목록 조회 완료: {}개", grade, examSheetInfos.size());

        return new ExamSheetListResponse(
                null, // 전체 목록이므로 학년 정보 없음
                examSheetInfos,
                examSheetInfos.size());
    }

    /**
     * 시험지 상세 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 상세 정보
     */
    public ExamSheetDetailResponse getExamSheetDetail(UUID examSheetId) {
        log.info("시험지 {} 상세 조회 요청", examSheetId);

        // 1단계: 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 선택된 단원들 조회 (Note: method needs to be updated to use exam_sheet_id)
        List<ExamSheetSelectedUnit> selectedUnits = examSheetSelectedUnitRepository.findByExamSheetId(examSheetId);
        List<ExamSheetDetailResponse.UnitInfo> unitInfos = new ArrayList<>();
        for (ExamSheetSelectedUnit selectedUnit : selectedUnits) {
            Unit unit = selectedUnit.getUnit();
            ExamSheetDetailResponse.UnitInfo unitInfo = new ExamSheetDetailResponse.UnitInfo(
                    unit.getId(),
                    unit.getUnitName());
            unitInfos.add(unitInfo);
        }

        // 3단계: 선택된 문제들 조회
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdOrderBySeqNo(examSheetId);
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
                    examSheetQuestion.getPoints());
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
                questionInfos);
    }

    /**
     * 시험지 수정 (문제 교체)
     * 
     * @param examSheetId 시험지 ID
     * @param request     수정 요청
     * @return 수정된 시험지 정보
     */
    @Transactional
    public ExamSheetDetailResponse updateExamSheet(UUID examSheetId, ExamSheetUpdateRequest request) {
        log.info("시험지 {} 수정 요청: 문제={}번 교체", examSheetId, request.seqNo());

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 교체할 문제 조회
        ExamSheetQuestion currentQuestion = examSheetQuestionRepository
                .findByExamSheetIdAndSeqNo(examSheetId, request.seqNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 번호입니다: " + request.seqNo()));

        // 3단계: 같은 단원에서 다른 문제 선택
        Question newQuestion = selectRandomQuestionFromUnit(currentQuestion.getQuestion().getUnit().getId(),
                currentQuestion.getQuestion().getId());

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
     * 시험지 문제 교체 (명시적)
     * 
     * 문제 직접 선택 시스템에서 특정 문제를 다른 문제로 교체합니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request     문제 교체 요청
     * @return 교체 완료된 시험지 정보
     */
    @Transactional
    public ExamSheetDetailResponse replaceQuestion(UUID examSheetId, QuestionReplaceRequest request) {
        log.info("시험지 {} 문제 교체 요청: {} → {}",
                examSheetId, request.oldQuestionId(), request.newQuestionId());

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 기존 문제가 시험지에 포함되어 있는지 확인
        ExamSheetQuestion currentExamSheetQuestion = examSheetQuestionRepository
                .findByExamSheetIdAndQuestionId(examSheetId, request.oldQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "시험지에 포함되지 않은 문제입니다: " + request.oldQuestionId()));

        // 3단계: 새로운 문제 존재 확인
        Question newQuestion = questionRepository.findById(request.newQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 문제입니다: " + request.newQuestionId()));

        // 4단계: 새로운 문제가 이미 시험지에 포함되어 있지 않은지 확인
        boolean newQuestionExists = examSheetQuestionRepository
                .findByExamSheetIdAndQuestionId(examSheetId, request.newQuestionId())
                .isPresent();

        if (newQuestionExists) {
            throw new IllegalArgumentException("새로운 문제가 이미 시험지에 포함되어 있습니다: " + request.newQuestionId());
        }

        // 5단계: 문제 교체 - 기존 문제 정보 유지하면서 Question 참조만 변경
        ExamSheetQuestion updatedExamSheetQuestion = ExamSheetQuestion.builder()
                .examSheet(currentExamSheetQuestion.getExamSheet())
                .question(newQuestion)
                .seqNo(currentExamSheetQuestion.getSeqNo())
                .points(currentExamSheetQuestion.getPoints())
                .build();

        // 6단계: 기존 문제 삭제 후 새 문제 저장
        examSheetQuestionRepository.delete(currentExamSheetQuestion);
        examSheetQuestionRepository.save(updatedExamSheetQuestion);

        log.info("시험지 {} 문제 교체 완료: {} → {} ({}번 문제)",
                examSheetId, request.oldQuestionId(), request.newQuestionId(),
                currentExamSheetQuestion.getSeqNo());

        // 7단계: 수정된 시험지 상세 정보 반환
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
    private List<Question> selectRandomQuestions(List<UUID> unitIds, int totalQuestions) {
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
    private Question selectRandomQuestionFromUnit(UUID unitId, UUID excludeQuestionId) {
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

    /**
     * 선택 가능한 문제 목록 조회
     * 
     * 시험지에 추가할 수 있는 문제들을 단원별, 난이도별, 유형별로 필터링하여 조회합니다.
     * 이미 시험지에 포함된 문제들은 표시되지만 선택 불가능으로 표시됩니다.
     * 
     * @param examSheetId  시험지 ID
     * @param unitId       단원 ID (선택)
     * @param difficulty   난이도 (선택)
     * @param questionType 문제 유형 (선택)
     * @return 선택 가능한 문제 목록
     */
    public SelectableQuestionsResponse getSelectableQuestions(UUID examSheetId, UUID unitId,
            String difficulty, String questionType) {
        log.info("시험지 {} 선택 가능한 문제 목록 조회: 단원={}, 난이도={}, 유형={}",
                examSheetId, unitId, difficulty, questionType);

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 시험지에 이미 포함된 문제 ID 목록 조회
        List<UUID> selectedQuestionIds = examSheetQuestionRepository
                .findByExamSheetIdOrderBySeqNo(examSheetId)
                .stream()
                .map(esq -> esq.getQuestion().getId())
                .toList();

        // 3단계: 필터링 조건에 따른 문제 조회
        List<Question> filteredQuestions;

        if (unitId != null) {
            // 특정 단원의 문제만 조회
            if (difficulty != null && questionType != null) {
                // 단원 + 난이도 + 유형
                filteredQuestions = questionRepository.findByUnitIdAndDifficultyNameAndQuestionType(
                        unitId, difficulty, Question.QuestionType.valueOf(questionType));
            } else if (difficulty != null) {
                // 단원 + 난이도
                filteredQuestions = questionRepository.findByUnitIdAndDifficultyName(unitId, difficulty);
            } else if (questionType != null) {
                // 단원 + 유형
                filteredQuestions = questionRepository.findByUnitIdAndQuestionType(
                        unitId, Question.QuestionType.valueOf(questionType));
            } else {
                // 단원만
                filteredQuestions = questionRepository.findByUnitId(unitId);
            }
        } else {
            // 전체 문제 조회 (학년 조건 추가 필요)
            if (difficulty != null && questionType != null) {
                // 난이도 + 유형
                filteredQuestions = questionRepository.findByUnit_GradeAndDifficultyNameAndQuestionType(
                        examSheet.getGrade(), difficulty, Question.QuestionType.valueOf(questionType));
            } else if (difficulty != null) {
                // 난이도만
                filteredQuestions = questionRepository.findByUnit_GradeAndDifficultyName(
                        examSheet.getGrade(), difficulty);
            } else if (questionType != null) {
                // 유형만
                filteredQuestions = questionRepository.findByUnit_GradeAndQuestionType(
                        examSheet.getGrade(), Question.QuestionType.valueOf(questionType));
            } else {
                // 학년만 (전체)
                filteredQuestions = questionRepository.findByUnit_Grade(examSheet.getGrade());
            }
        }

        // 4단계: 응답 DTO 생성
        List<SelectableQuestionsResponse.SelectableQuestion> selectableQuestions = new ArrayList<>();
        for (Question question : filteredQuestions) {
            Unit unit = question.getUnit();
            boolean alreadySelected = selectedQuestionIds.contains(question.getId());

            SelectableQuestionsResponse.SelectableQuestion selectableQuestion = new SelectableQuestionsResponse.SelectableQuestion(
                    question.getId(),
                    unit.getId(),
                    unit.getUnitName(),
                    question.getQuestionType().name(),
                    question.getDifficulty().name(),
                    question.getQuestionTextAsHtml(),
                    alreadySelected);

            selectableQuestions.add(selectableQuestion);
        }

        // 5단계: 검색 조건 정보 설정
        String unitName = null;
        if (unitId != null) {
            Unit unit = unitRepository.findById(unitId).orElse(null);
            unitName = unit != null ? unit.getUnitName() : null;
        }

        log.info("시험지 {} 선택 가능한 문제 목록 조회 완료: {}개 문제 (이미 선택됨: {}개)",
                examSheetId, selectableQuestions.size(), selectedQuestionIds.size());

        return new SelectableQuestionsResponse(
                unitId,
                unitName,
                difficulty,
                questionType,
                selectableQuestions.size(),
                selectableQuestions);
    }

    /**
     * 시험지에 문제 추가
     * 
     * 문제 직접 선택 시스템에서 특정 문제를 시험지에 추가합니다.
     * 문제 순서와 배점을 지정할 수 있습니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request     문제 선택 요청
     * @return 업데이트된 시험지 문제 관리 정보
     */
    @Transactional
    public ExamSheetQuestionManageResponse addQuestionToExamSheet(UUID examSheetId,
            QuestionSelectionRequest request) {
        log.info("시험지 {} 문제 추가: 문제={}, 배점={}, 순서={}",
                examSheetId, request.questionId(), request.points(), request.questionOrder());

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 문제 존재 확인
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다: " + request.questionId()));

        // 3단계: 중복 문제 확인
        boolean questionExists = examSheetQuestionRepository
                .findByExamSheetIdAndQuestionId(examSheetId, request.questionId())
                .isPresent();

        if (questionExists) {
            throw new IllegalArgumentException("이미 시험지에 포함된 문제입니다: " + request.questionId());
        }

        // 4단계: 문제 순서 결정
        int questionOrder;
        if (request.questionOrder() != null) {
            questionOrder = request.questionOrder();
            // 기존 문제들의 순서 재조정 (해당 순서 이후 문제들을 1씩 증가)
            List<ExamSheetQuestion> existingQuestions = examSheetQuestionRepository
                    .findByExamSheetIdAndQuestionOrderGreaterThanEqual(examSheetId, questionOrder);

            for (ExamSheetQuestion existingQuestion : existingQuestions) {
                existingQuestion.updateQuestionOrder(existingQuestion.getQuestionOrder() + 1);
            }
            examSheetQuestionRepository.saveAll(existingQuestions);
        } else {
            // 마지막 순서에 추가
            questionOrder = (int) examSheetQuestionRepository.countByExamSheetId(examSheetId) + 1;
        }

        // 5단계: 새 문제 추가
        ExamSheetQuestion newExamSheetQuestion = ExamSheetQuestion.builder()
                .examSheet(examSheet)
                .question(question)
                .seqNo(questionOrder) // seqNo와 questionOrder 동일하게 설정
                .questionOrder(questionOrder)
                .points(request.points())
                .selectionMethod(ExamSheetQuestion.SelectionMethod.MANUAL)
                .build();

        examSheetQuestionRepository.save(newExamSheetQuestion);

        log.info("시험지 {} 문제 추가 완료: 문제={}, 순서={}",
                examSheetId, request.questionId(), questionOrder);

        // 6단계: 업데이트된 시험지 문제 관리 정보 반환
        return getExamSheetQuestionManagement(examSheetId);
    }

    /**
     * 시험지에서 문제 제거
     * 
     * 문제 직접 선택 시스템에서 시험지에 포함된 특정 문제를 제거합니다.
     * 제거 후 문제 순서가 자동으로 재조정됩니다.
     * 
     * @param examSheetId 시험지 ID
     * @param questionId  제거할 문제 ID
     * @return 업데이트된 시험지 문제 관리 정보
     */
    @Transactional
    public ExamSheetQuestionManageResponse removeQuestionFromExamSheet(UUID examSheetId, UUID questionId) {
        log.info("시험지 {} 문제 제거: 문제={}", examSheetId, questionId);

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 시험지에서 해당 문제 조회
        ExamSheetQuestion targetQuestion = examSheetQuestionRepository
                .findByExamSheetIdAndQuestionId(examSheetId, questionId)
                .orElseThrow(() -> new IllegalArgumentException("시험지에 포함되지 않은 문제입니다: " + questionId));

        int removedOrder = targetQuestion.getQuestionOrder();

        // 3단계: 문제 제거
        examSheetQuestionRepository.delete(targetQuestion);

        // 4단계: 나머지 문제들의 순서 재조정 (제거된 문제 이후의 문제들을 1씩 감소)
        List<ExamSheetQuestion> remainingQuestions = examSheetQuestionRepository
                .findByExamSheetIdAndQuestionOrderGreaterThan(examSheetId, removedOrder);

        for (ExamSheetQuestion remainingQuestion : remainingQuestions) {
            remainingQuestion.updateQuestionOrder(remainingQuestion.getQuestionOrder() - 1);
        }
        examSheetQuestionRepository.saveAll(remainingQuestions);

        log.info("시험지 {} 문제 제거 완료: 문제={}, 제거된 순서={}",
                examSheetId, questionId, removedOrder);

        // 5단계: 업데이트된 시험지 문제 관리 정보 반환
        return getExamSheetQuestionManagement(examSheetId);
    }

    /**
     * 시험지 문제 관리 현황 조회
     * 
     * 현재 시험지에 포함된 문제들의 상세 정보와 관리 상태를 조회합니다.
     * 문제 순서, 배점, 유형별 개수 등을 확인할 수 있습니다.
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 문제 관리 현황
     */
    public ExamSheetQuestionManageResponse getExamSheetQuestionManagement(UUID examSheetId) {
        log.info("시험지 {} 문제 관리 현황 조회", examSheetId);

        // 1단계: 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 시험지에 포함된 문제들 조회
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdOrderByQuestionOrder(examSheetId);

        // 3단계: 문제별 상세 정보 생성
        List<ExamSheetQuestionManageResponse.QuestionInExamSheet> questions = new ArrayList<>();
        int multipleChoiceCount = 0;
        int subjectiveCount = 0;
        int totalPoints = 0;

        for (ExamSheetQuestion esq : examSheetQuestions) {
            Question question = esq.getQuestion();
            Unit unit = question.getUnit();

            // 문제 유형별 개수 계산
            if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE) {
                multipleChoiceCount++;
            } else {
                subjectiveCount++;
            }

            // 총 배점 계산
            totalPoints += esq.getPoints();

            ExamSheetQuestionManageResponse.QuestionInExamSheet questionInfo = new ExamSheetQuestionManageResponse.QuestionInExamSheet(
                    esq.getQuestionOrder(),
                    question.getId(),
                    unit.getId(),
                    unit.getUnitName(),
                    question.getQuestionType().name(),
                    question.getDifficulty().name(),
                    esq.getPoints(),
                    esq.getSelectionMethod().name());

            questions.add(questionInfo);
        }

        log.info("시험지 {} 문제 관리 현황 조회 완료: {}/{} 문제, 객관식={}, 주관식={}, {}점",
                examSheetId, examSheetQuestions.size(), examSheet.getTotalQuestions(),
                multipleChoiceCount, subjectiveCount, totalPoints);

        return new ExamSheetQuestionManageResponse(
                examSheetId,
                examSheet.getExamName(),
                examSheetQuestions.size(),
                examSheet.getTotalQuestions(),
                multipleChoiceCount,
                subjectiveCount,
                totalPoints,
                questions);
    }

    /**
     * 시험지 미리보기 조회
     * 
     * 시험지의 전체 구성과 통계 정보를 제공합니다.
     * 포함된 모든 문제의 상세 정보와 함께 문제 타입별, 난이도별, 단원별 분포를 계산합니다.
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 미리보기 응답
     * @throws IllegalArgumentException 시험지가 존재하지 않을 때
     */
    @Transactional(readOnly = true)
    public ExamSheetPreviewResponse getExamSheetPreview(UUID examSheetId) {
        log.info("시험지 {} 미리보기 조회 시작", examSheetId);

        // 1. 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2. 시험지에 포함된 문제들 조회 (순서대로)
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdOrderByQuestionOrder(examSheetId);

        log.info("시험지 {} 포함 문제 개수: {}", examSheetId, examSheetQuestions.size());

        // 3. 문제 미리보기 정보 생성
        List<ExamSheetPreviewResponse.ExamSheetQuestionPreview> questionPreviews = examSheetQuestions.stream()
                .map(this::createQuestionPreview)
                .collect(Collectors.toList());

        // 4. 통계 정보 계산
        ExamSheetPreviewResponse.QuestionTypeStatistics statistics = calculateQuestionStatistics(examSheetQuestions);

        // 5. 총 배점 계산
        Integer totalPoints = examSheetQuestions.stream()
                .mapToInt(ExamSheetQuestion::getPoints)
                .sum();

        log.info("시험지 {} 미리보기 생성 완료: {}문제, {}점",
                examSheetId, examSheetQuestions.size(), totalPoints);

        return new ExamSheetPreviewResponse(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                examSheet.getTotalQuestions(),
                examSheet.getMultipleChoiceCount(),
                examSheet.getSubjectiveCount(),
                totalPoints,
                questionPreviews,
                statistics);
    }

    /**
     * ExamSheetQuestion을 문제 미리보기 DTO로 변환
     */
    private ExamSheetPreviewResponse.ExamSheetQuestionPreview createQuestionPreview(
            ExamSheetQuestion examSheetQuestion) {

        Question question = examSheetQuestion.getQuestion();
        Unit unit = question.getUnit();

        // 문제 내용 요약 생성 (HTML 태그 제거 후 50자로 제한)
        String questionSummary = createQuestionSummary(question.getQuestionTextAsHtml());

        return new ExamSheetPreviewResponse.ExamSheetQuestionPreview(
                question.getId(),
                examSheetQuestion.getQuestionOrder(),
                examSheetQuestion.getPoints(),
                question.getQuestionType().name(),
                question.getDifficulty().name(),
                unit.getUnitName(),
                questionSummary,
                examSheetQuestion.getSelectionMethod().name());
    }

    /**
     * 문제 내용으로부터 요약 생성
     */
    private String createQuestionSummary(String questionHtml) {
        if (questionHtml == null) {
            return "문제 내용 없음";
        }

        // HTML 태그 제거 (간단한 정규식 사용)
        String plainText = questionHtml.replaceAll("<[^>]*>", "");

        // 50자로 제한
        if (plainText.length() <= 50) {
            return plainText;
        } else {
            return plainText.substring(0, 50) + "...";
        }
    }

    /**
     * 문제 타입별 통계 계산
     */
    private ExamSheetPreviewResponse.QuestionTypeStatistics calculateQuestionStatistics(
            List<ExamSheetQuestion> examSheetQuestions) {

        // 객관식/주관식 배점 계산
        int multipleChoicePoints = 0;
        int subjectivePoints = 0;

        // 난이도별 개수 계산
        int easyCount = 0;
        int mediumCount = 0;
        int hardCount = 0;

        // 단원별 분포 계산을 위한 Map
        Map<UUID, ExamSheetPreviewResponse.UnitDistribution> unitDistributionMap = new HashMap<>();

        for (ExamSheetQuestion esq : examSheetQuestions) {
            Question question = esq.getQuestion();
            Unit unit = question.getUnit();
            int points = esq.getPoints();

            // 문제 타입별 배점
            if (question.isMultipleChoice()) {
                multipleChoicePoints += points;
            } else {
                subjectivePoints += points;
            }

            // 난이도별 개수
            switch (question.getDifficulty().name()) {
                case "EASY" -> easyCount++;
                case "MEDIUM" -> mediumCount++;
                case "HARD" -> hardCount++;
            }

            // 단원별 분포
            UUID unitId = unit.getId();
            if (unitDistributionMap.containsKey(unitId)) {
                ExamSheetPreviewResponse.UnitDistribution existing = unitDistributionMap.get(unitId);
                unitDistributionMap.put(unitId, new ExamSheetPreviewResponse.UnitDistribution(
                        unitId,
                        unit.getUnitName(),
                        existing.questionCount() + 1,
                        existing.totalPoints() + points));
            } else {
                unitDistributionMap.put(unitId, new ExamSheetPreviewResponse.UnitDistribution(
                        unitId,
                        unit.getUnitName(),
                        1,
                        points));
            }
        }

        List<ExamSheetPreviewResponse.UnitDistribution> unitDistributions = new ArrayList<>(
                unitDistributionMap.values());

        return new ExamSheetPreviewResponse.QuestionTypeStatistics(
                multipleChoicePoints,
                subjectivePoints,
                easyCount,
                mediumCount,
                hardCount,
                unitDistributions);
    }

    /**
     * 시험지 문제 교체
     * 
     * 시험지에서 기존 문제를 다른 문제로 교체합니다.
     * 문제 순서와 위치는 유지되며, 배점은 조정 가능합니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request     문제 교체 요청
     * @return 업데이트된 시험지 문제 관리 정보
     * @throws IllegalArgumentException 시험지나 문제가 존재하지 않거나 유효하지 않을 때
     */
    @Transactional
    public ExamSheetQuestionManageResponse replaceQuestionInExamSheet(UUID examSheetId,
            QuestionReplaceRequest request) {
        log.info("시험지 {} 문제 교체 시작: 기존 {} → 새 {}",
                examSheetId, request.oldQuestionId(), request.newQuestionId());

        // 1. 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2. 기존 문제가 시험지에 있는지 확인
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdOrderByQuestionOrder(examSheetId);

        ExamSheetQuestion existingQuestion = examSheetQuestions.stream()
                .filter(esq -> esq.getQuestion().getId().equals(request.oldQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "기존 문제가 해당 시험지에 포함되어 있지 않습니다: " + request.oldQuestionId()));

        // 3. 새 문제 존재 확인
        Question newQuestion = questionRepository.findById(request.newQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 새 문제입니다: " + request.newQuestionId()));

        // 4. 새 문제가 이미 시험지에 포함되어 있는지 확인
        boolean isAlreadyIncluded = examSheetQuestions.stream()
                .anyMatch(esq -> esq.getQuestion().getId().equals(request.newQuestionId()));

        if (isAlreadyIncluded) {
            throw new IllegalArgumentException("새 문제가 이미 해당 시험지에 포함되어 있습니다: " + request.newQuestionId());
        }

        // 5. 문제 교체 수행
        int oldPoints = existingQuestion.getPoints();
        int newPoints = request.points() != null ? request.points() : oldPoints; // 배점 유지 또는 변경

        // 기존 ExamSheetQuestion의 문제와 배점 업데이트
        ExamSheetQuestion updatedExamSheetQuestion = ExamSheetQuestion.builder()
                .id(existingQuestion.getId()) // 동일한 ID 유지
                .examSheet(examSheet)
                .question(newQuestion) // 새 문제로 교체
                .seqNo(existingQuestion.getSeqNo()) // 기존 순서 번호 유지
                .points(newPoints) // 배점 변경 또는 유지
                .questionOrder(existingQuestion.getQuestionOrder()) // 문제 순서 유지
                .selectionMethod(existingQuestion.getSelectionMethod()) // 선택 방식 유지
                .build();

        examSheetQuestionRepository.save(updatedExamSheetQuestion);

        // 6. 문제 타입 개수 업데이트 (필요한 경우)
        Question oldQuestion = existingQuestion.getQuestion();
        updateQuestionTypeCounts(examSheet, oldQuestion, newQuestion);

        log.info("시험지 {} 문제 교체 완료: {} → {}, 배점 {} → {}",
                examSheetId, request.oldQuestionId(), request.newQuestionId(), oldPoints, newPoints);

        if (request.reason() != null) {
            log.info("교체 사유: {}", request.reason());
        }

        // 7. 업데이트된 시험지 문제 관리 정보 반환
        return getExamSheetQuestionManagement(examSheetId);
    }

    /**
     * 문제 교체 시 문제 타입별 개수 업데이트
     * 
     * @param examSheet   시험지
     * @param oldQuestion 기존 문제
     * @param newQuestion 새 문제
     */
    private void updateQuestionTypeCounts(ExamSheet examSheet, Question oldQuestion, Question newQuestion) {
        boolean oldIsMultipleChoice = oldQuestion.isMultipleChoice();
        boolean newIsMultipleChoice = newQuestion.isMultipleChoice();

        // 문제 타입이 변경된 경우에만 개수 업데이트
        if (oldIsMultipleChoice != newIsMultipleChoice) {
            int multipleChoiceChange = 0;
            int subjectiveChange = 0;

            if (oldIsMultipleChoice && !newIsMultipleChoice) {
                // 객관식 → 주관식
                multipleChoiceChange = -1;
                subjectiveChange = 1;
            } else if (!oldIsMultipleChoice && newIsMultipleChoice) {
                // 주관식 → 객관식
                multipleChoiceChange = 1;
                subjectiveChange = -1;
            }

            // 시험지의 문제 타입별 개수 업데이트
            examSheet.updateQuestionCounts(
                    examSheet.getTotalQuestions(), // 총 개수는 동일
                    examSheet.getMultipleChoiceCount() + multipleChoiceChange,
                    examSheet.getSubjectiveCount() + subjectiveChange);

            examSheetRepository.save(examSheet);

            log.info("문제 타입 변경으로 인한 개수 업데이트: 객관식={}, 주관식={}",
                    examSheet.getMultipleChoiceCount(), examSheet.getSubjectiveCount());
        }
    }

    /**
     * 필터링된 시험지 목록 조회
     * 
     * 다양한 조건으로 시험지 목록을 필터링하여 조회합니다.
     * 
     * @param grade         학년 (선택사항)
     * @param minQuestions  최소 문제 개수 (선택사항)
     * @param maxQuestions  최대 문제 개수 (선택사항)
     * @param startDate     시작 날짜 (YYYY-MM-DD 형식, 선택사항)
     * @param endDate       종료 날짜 (YYYY-MM-DD 형식, 선택사항)
     * @param keyword       키워드 검색 (시험지명에서 검색, 선택사항)
     * @param sortBy        정렬 기준 (createdAt, examName, totalQuestions, grade)
     * @param sortDirection 정렬 방향 (asc, desc)
     * @return 필터링된 시험지 목록 응답
     */
    @Transactional(readOnly = true)
    public ExamSheetListResponse getFilteredExamSheets(
            Integer grade,
            Integer minQuestions,
            Integer maxQuestions,
            String startDate,
            String endDate,
            String keyword,
            String sortBy,
            String sortDirection) {

        log.info("필터링된 시험지 목록 조회 시작: grade={}, minQuestions={}, maxQuestions={}, " +
                "startDate={}, endDate={}, keyword={}, sortBy={}, sortDirection={}",
                grade, minQuestions, maxQuestions, startDate, endDate, keyword, sortBy, sortDirection);

        // 1. 날짜 문자열을 LocalDateTime으로 변환
        LocalDateTime startDateTime = parseDate(startDate, true); // 하루 시작
        LocalDateTime endDateTime = parseDate(endDate, false); // 하루 끝

        // 2. 페이징 없이 전체 목록 조회 (Repository 메서드 활용)
        List<ExamSheet> examSheets;
        if (grade != null) {
            examSheets = examSheetRepository.findByGradeWithFilters(
                    grade, keyword, null, startDateTime, endDateTime,
                    Pageable.unpaged()).getContent();
        } else {
            examSheets = examSheetRepository.findWithFilters(
                    keyword, null, startDateTime, endDateTime,
                    Pageable.unpaged()).getContent();
        }

        // 3. 문제 개수 범위 필터링 (service 레벨에서 처리)
        List<ExamSheet> filteredSheets = examSheets.stream()
                .filter(sheet -> {
                    int totalQuestions = sheet.getTotalQuestions() != null ? sheet.getTotalQuestions() : 0;

                    // 최소 문제 개수 조건
                    if (minQuestions != null && totalQuestions < minQuestions) {
                        return false;
                    }

                    // 최대 문제 개수 조건
                    if (maxQuestions != null && totalQuestions > maxQuestions) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // 4. 정렬 처리
        filteredSheets = sortExamSheets(filteredSheets, sortBy, sortDirection);

        // 5. ExamSheetListResponse 생성
        List<ExamSheetListResponse.ExamSheetInfo> examSheetInfos = filteredSheets.stream()
                .map(this::createExamSheetInfo)
                .collect(Collectors.toList());

        log.info("필터링된 시험지 목록 조회 완료: 총 {}건", examSheetInfos.size());

        return new ExamSheetListResponse(
                grade,
                examSheetInfos,
                examSheetInfos.size());
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     * 
     * @param dateStr      날짜 문자열 (YYYY-MM-DD 형식)
     * @param isStartOfDay true면 하루 시작(00:00:00), false면 하루 끝(23:59:59)
     * @return 변환된 LocalDateTime, null이면 null 반환
     */
    private LocalDateTime parseDate(String dateStr, boolean isStartOfDay) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            return isStartOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }

    /**
     * 시험지 목록 정렬
     * 
     * @param examSheets    정렬할 시험지 목록
     * @param sortBy        정렬 기준
     * @param sortDirection 정렬 방향
     * @return 정렬된 시험지 목록
     */
    private List<ExamSheet> sortExamSheets(List<ExamSheet> examSheets, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt"; // 기본 정렬
        }

        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc"; // 기본 내림차순
        }

        Comparator<ExamSheet> comparator = switch (sortBy.toLowerCase()) {
            case "examname" -> Comparator.comparing(ExamSheet::getExamName, Comparator.nullsLast(String::compareTo));
            case "totalquestions" -> Comparator.comparing(
                    exam -> exam.getTotalQuestions() != null ? exam.getTotalQuestions() : 0);
            case "grade" -> Comparator.comparing(
                    exam -> exam.getGrade() != null ? exam.getGrade() : 0);
            default -> Comparator.comparing(ExamSheet::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
        };

        // 정렬 방향 적용
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return examSheets.stream().sorted(comparator).collect(Collectors.toList());
        } else {
            return examSheets.stream().sorted(comparator.reversed()).collect(Collectors.toList());
        }
    }

    /**
     * ExamSheet를 ExamSheetInfo DTO로 변환
     * 
     * @param examSheet 시험지 엔티티
     * @return 시험지 정보 DTO
     */
    private ExamSheetListResponse.ExamSheetInfo createExamSheetInfo(ExamSheet examSheet) {
        // 선택된 단원 수 계산 (ExamSheetSelectedUnit과의 관계를 통해)
        int selectedUnitCount = (int) examSheetSelectedUnitRepository.countByExamSheetId(examSheet.getId());

        return new ExamSheetListResponse.ExamSheetInfo(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                examSheet.getTotalQuestions(),
                selectedUnitCount);
    }
}