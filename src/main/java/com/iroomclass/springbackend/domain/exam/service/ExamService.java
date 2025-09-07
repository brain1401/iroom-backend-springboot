package com.iroomclass.springbackend.domain.exam.service;

import com.iroomclass.springbackend.domain.exam.dto.CreateExamRequest;
import com.iroomclass.springbackend.domain.exam.dto.CreateExamResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamAnswerSheetDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamAttendeeDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamFilterRequest;
import com.iroomclass.springbackend.domain.exam.dto.ExamQuestionsResponseDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamSubmissionStatusDto;
import com.iroomclass.springbackend.domain.exam.dto.ExamWithUnitsDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitSummaryDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitNameDto;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitNameProjection;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository.ExamSubmissionStats;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitProjection;
import com.iroomclass.springbackend.domain.exam.repository.projection.UnitBasicProjection;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 시험 관련 비즈니스 로직 처리 서비스
 * 
 * <p>
 * 시험 조회, 시험 제출 현황 통계, 시험 관리 기능을 제공합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final ExamSheetRepository examSheetRepository;
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;
    private final ExamResultRepository examResultRepository;

    /**
     * 시험 생성
     * 
     * <p>
     * 선택한 시험지를 기반으로 새로운 시험을 생성합니다.
     * 시험지의 학년 정보와 문제 정보를 자동으로 복사합니다.
     * </p>
     * 
     * @param request   시험 생성 요청 정보
     * @param teacherId 생성하는 선생님의 ID
     * @return 생성된 시험 정보
     * @throws RuntimeException         시험지를 찾을 수 없을 때
     * @throws IllegalArgumentException 중복된 시험명이 존재할 때
     */
    @Transactional
    public CreateExamResponse createExam(CreateExamRequest request) {
        log.info("시험 생성 시작: examName={}, examSheetId={}",
                request.examName(), request.examSheetId());

        // 1. 시험지 조회 및 검증
        ExamSheet examSheet = examSheetRepository.findById(request.examSheetId())
                .orElseThrow(() -> new RuntimeException("시험지를 찾을 수 없습니다: " + request.examSheetId()));

        // 2. 중복 시험명 확인 (선택적 - 필요시 주석 해제)
        // boolean existsSameName = examRepository.existsByExamName(request.examName());
        // if (existsSameName) {
        // log.warn("중복된 시험명 발견: examName={}", request.examName());
        // throw new IllegalArgumentException("이미 동일한 이름의 시험이 존재합니다: " +
        // request.examName());
        // }

        // 3. 시험 엔티티 생성
        Exam exam = Exam.builder()
                .examSheet(examSheet)
                .examName(request.examName())
                .grade(examSheet.getGrade()) // 시험지의 학년 정보 복사
                .content(request.description())
                .maxStudent(request.maxStudent()) // 요청에서 최대 학생 수 설정
                .qrCodeUrl(null) // QR 코드는 나중에 생성
                .build();

        // 4. 시험 저장
        Exam savedExam = examRepository.save(exam);
        log.info("시험 생성 완료: examId={}, examName={}", savedExam.getId(), savedExam.getExamName());

        // 5. 응답 DTO 생성 및 반환
        return CreateExamResponse.from(
                savedExam,
                examSheet,
                null, // teacherUsername은 더 이상 필요하지 않음
                request.startDate(),
                request.endDate(),
                request.duration());
    }

    /**
     * 시험 ID로 상세 정보 조회
     * 
     * @param examId 시험 식별자
     * @return 시험 상세 정보 DTO
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamDto findById(UUID examId) {
        log.info("시험 조회 시작: examId={}", examId);

        Exam exam = examRepository.findByIdWithExamSheet(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 실제 응시 학생 수 계산
        long actualStudentCount = examSubmissionRepository.countByExamId(examId);

        // ExamDto 생성
        ExamDto examDto = ExamDto.fromWithExamSheet(exam);

        // actualStudentCount를 포함한 새로운 ExamDto 생성
        ExamDto resultDto = new ExamDto(
                examDto.id(),
                examDto.examName(),
                examDto.grade(),
                examDto.content(),
                examDto.maxStudent(),
                (int) actualStudentCount,
                examDto.qrCodeUrl(),
                examDto.createdAt(),
                examDto.examSheetInfo());

        log.info("시험 조회 완료: examId={}, examName={}, actualStudentCount={}",
                examId, exam.getExamName(), actualStudentCount);
        return resultDto;
    }

    /**
     * 시험 문제 목록 조회 (학생 제출용)
     * 
     * <p>
     * 학생이 시험에 응시할 때 필요한 모든 문제 정보를 제공합니다.
     * 문제 내용, 선택지, 이미지, 통계 정보 등이 포함됩니다.
     * </p>
     * 
     * @param examId 시험 식별자
     * @return 시험 문제 상세 정보 및 통계
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamQuestionsResponseDto findExamQuestions(UUID examId) {
        log.info("시험 문제 조회 시작: examId={}", examId);

        // 1. 시험 정보 조회
        Exam exam = examRepository.findByIdWithExamSheet(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 2. 시험지의 문제 목록 조회 (문제와 단원 정보 포함)
        List<com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdWithQuestionsAndUnits(exam.getExamSheet().getId());

        // 3. DTO 변환
        ExamQuestionsResponseDto responseDto = ExamQuestionsResponseDto.from(exam, examSheetQuestions);

        log.info("시험 문제 조회 완료: examId={}, totalQuestions={}, multipleChoice={}, subjective={}",
                examId, responseDto.totalQuestions(), responseDto.multipleChoiceCount(), responseDto.subjectiveCount());

        return responseDto;
    }

    /**
     * 시험별 제출 현황 상세 조회
     * 
     * @param examId 시험 식별자
     * @return 시험 제출 현황 상세 정보
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamSubmissionStatusDto getExamSubmissionStatus(UUID examId) {
        log.info("시험 제출 현황 조회 시작: examId={}", examId);

        // 시험 정보 조회
        Exam exam = examRepository.findByIdWithExamSheet(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 제출 통계 조회
        Long submissionCount = examSubmissionRepository.countByExamId(examId);

        // Exam 엔티티의 maxStudent 값 사용 (확실한 최대 응시 가능 학생 수)
        Long totalStudentsInGrade = Long.valueOf(exam.getMaxStudent());

        // 시간별 제출 통계 조회
        List<ExamSubmissionRepository.HourlySubmissionStats> hourlyStats = examSubmissionRepository
                .findHourlySubmissionStats(examId);

        // 최근 제출자 목록 (최대 5명)
        List<com.iroomclass.springbackend.domain.exam.entity.ExamSubmission> recentSubmissionEntities = examSubmissionRepository
                .findByExamIdWithStudent(examId);
        List<ExamSubmissionStatusDto.RecentSubmission> recentSubmissions = recentSubmissionEntities.stream()
                .limit(5)
                .map(ExamSubmissionStatusDto.RecentSubmission::from)
                .collect(Collectors.toList());

        log.info("시험 제출 현황 조회 완료: examId={}, submissionCount={}, maxStudent={}",
                examId, submissionCount, exam.getMaxStudent());

        return new ExamSubmissionStatusDto(
                ExamSubmissionStatusDto.ExamInfo.from(exam),
                ExamSubmissionStatusDto.SubmissionStats.create(totalStudentsInGrade, submissionCount),
                recentSubmissions,
                hourlyStats.stream()
                        .map(ExamSubmissionStatusDto.HourlyStats::from)
                        .collect(Collectors.toList()));
    }

    /**
     * 학년별 시험 목록 조회 (페이징 지원)
     * 
     * @param grade    학년 (1, 2, 3)
     * @param pageable 페이징 정보
     * @return 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findByGrade(Integer grade, Pageable pageable) {
        log.info("학년별 시험 목록 조회: grade={}, page={}, size={}",
                grade, pageable.getPageNumber(), pageable.getPageSize());

        Page<Exam> examPage = examRepository.findByGradeOrderByCreatedAtDesc(grade, pageable);

        log.info("학년별 시험 목록 조회 완료: grade={}, totalElements={}, totalPages={}",
                grade, examPage.getTotalElements(), examPage.getTotalPages());

        return examPage.map(ExamDto::from);
    }

    /**
     * 모든 시험 목록 조회 (페이징 지원)
     * 
     * @param pageable 페이징 정보
     * @return 전체 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findAll(Pageable pageable) {
        log.info("전체 시험 목록 조회: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Exam> examPage = examRepository.findAllOrderByCreatedAtDesc(pageable);

        log.info("전체 시험 목록 조회 완료: totalElements={}, totalPages={}",
                examPage.getTotalElements(), examPage.getTotalPages());

        return examPage.map(ExamDto::from);
    }

    /**
     * 여러 시험의 제출 현황 통계 조회
     * 
     * @param examIds 시험 식별자 목록
     * @return 시험별 제출 통계 맵 (examId -> submissionCount)
     */
    public Map<UUID, Long> getSubmissionCountsByExamIds(List<UUID> examIds) {
        log.info("여러 시험 제출 통계 조회: examCount={}", examIds.size());

        List<ExamSubmissionStats> stats = examSubmissionRepository.countByExamIds(examIds);

        Map<UUID, Long> result = stats.stream()
                .collect(Collectors.toMap(
                        ExamSubmissionStats::getExamId,
                        ExamSubmissionStats::getSubmissionCount));

        log.info("여러 시험 제출 통계 조회 완료: resultCount={}", result.size());
        return result;
    }

    /**
     * 시험명으로 검색 (페이징 지원)
     * 
     * @param examName 검색할 시험명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> searchByExamName(String examName, Pageable pageable) {
        log.info("시험명 검색: examName={}, page={}, size={}",
                examName, pageable.getPageNumber(), pageable.getPageSize());

        Page<Exam> examPage = examRepository.findByExamNameContainingIgnoreCaseOrderByCreatedAtDesc(
                examName, pageable);

        log.info("시험명 검색 완료: examName={}, totalElements={}",
                examName, examPage.getTotalElements());

        return examPage.map(ExamDto::from);
    }

    /**
     * 학년별 및 시험명으로 복합 검색
     * 
     * @param grade    학년 (1, 2, 3)
     * @param examName 검색할 시험명 (부분 일치)
     * @param pageable 페이징 정보
     * @return 검색된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> searchByGradeAndExamName(Integer grade, String examName, Pageable pageable) {
        log.info("학년 및 시험명 복합 검색: grade={}, examName={}", grade, examName);

        Page<Exam> examPage = examRepository.findByGradeAndExamNameContainingIgnoreCaseOrderByCreatedAtDesc(
                grade, examName, pageable);

        log.info("학년 및 시험명 복합 검색 완료: grade={}, examName={}, totalElements={}",
                grade, examName, examPage.getTotalElements());

        return examPage.map(ExamDto::from);
    }

    /**
     * 통합 필터링을 통한 시험 목록 조회
     * 
     * <p>
     * 다양한 필터링 조건을 조합하여 시험 목록을 조회합니다.
     * 기존 개별 메서드들을 통합한 단일 진입점입니다.
     * </p>
     * 
     * @param filter   필터링 조건
     * @param pageable 페이징 정보
     * @return 필터링된 시험 목록 (페이지 형태)
     */
    public Page<ExamDto> findExamsWithFilter(ExamFilterRequest filter, Pageable pageable) {
        log.info("통합 필터링 시험 조회: {}, page={}, size={}",
                filter.getFilterDescription(), pageable.getPageNumber(), pageable.getPageSize());

        Page<ExamDto> result;

        // 필터 조건에 따라 적절한 메서드 호출
        if (filter.hasGradeFilter() && filter.hasSearchFilter()) {
            // 학년 + 검색어 복합 필터
            result = searchByGradeAndExamName(filter.grade(), filter.search(), pageable);

        } else if (filter.hasGradeFilter()) {
            // 학년별 필터
            result = findByGrade(filter.grade(), pageable);

        } else if (filter.hasSearchFilter()) {
            // 검색어 필터
            result = searchByExamName(filter.search(), pageable);

        } else {
            // 전체 조회 또는 최근 시험
            result = findAll(pageable);
        }

        log.info("통합 필터링 시험 조회 완료: {}, totalElements={}",
                filter.getFilterDescription(), result.getTotalElements());

        return result;
    }

    /**
     * 통합 필터링 시험 목록 조회 (단원 정보 포함)
     * 
     * <p>
     * 다양한 필터링 조건을 적용하여 시험 목록을 조회하고, 각 시험에 포함된 단원 정보를 함께 제공합니다.
     * </p>
     * 
     * @param filter   필터 조건 (학년, 검색어, 최신 여부)
     * @param pageable 페이징 정보
     * @return 단원 정보가 포함된 시험 목록 페이지
     */
    public Page<ExamWithUnitsDto> findExamsWithFilterAndUnits(ExamFilterRequest filter, Pageable pageable) {
        log.info("단원 정보 포함 통합 필터링 시험 조회: {}, page={}, size={}",
                filter.getFilterDescription(), pageable.getPageNumber(), pageable.getPageSize());

        // 1. 먼저 필터링된 시험 목록을 조회
        Page<ExamDto> examPage = findExamsWithFilter(filter, pageable);

        if (examPage.isEmpty()) {
            log.info("필터링 결과가 없습니다: {}", filter.getFilterDescription());
            return Page.empty(pageable);
        }

        // 2. 시험 ID 목록 추출
        List<UUID> examIds = examPage.getContent().stream()
                .map(ExamDto::id)
                .collect(Collectors.toList());

        log.info("단원 정보 배치 조회 시작: examCount={}", examIds.size());

        // 3. 배치로 단원 정보가 포함된 시험 데이터 조회
        List<ExamWithUnitsDto> examsWithUnits = findByIdsWithUnits(examIds);

        // 4. 원본 페이지의 순서를 유지하면서 ExamWithUnitsDto로 변환
        Map<UUID, ExamWithUnitsDto> examUnitsMap = examsWithUnits.stream()
                .collect(Collectors.toMap(ExamWithUnitsDto::id, exam -> exam));

        List<ExamWithUnitsDto> orderedExamsWithUnits = examPage.getContent().stream()
                .map(examDto -> examUnitsMap.get(examDto.id()))
                .filter(Objects::nonNull) // null 체크 (만약 데이터 불일치가 있는 경우)
                .collect(Collectors.toList());

        // 5. 새로운 Page 객체 생성 (페이징 정보 유지)
        Page<ExamWithUnitsDto> result = new PageImpl<>(
                orderedExamsWithUnits,
                pageable,
                examPage.getTotalElements());

        log.info("단원 정보 포함 통합 필터링 시험 조회 완료: {}, totalElements={}, unitsIncluded={}",
                filter.getFilterDescription(), result.getTotalElements(),
                orderedExamsWithUnits.stream().mapToInt(ExamWithUnitsDto::getUnitCount).sum());

        return result;
    }

    /**
     * 시험 통계 조회 (통합)
     * 
     * @param statisticsType 통계 타입 ("by-grade" 등)
     * @return 통계 데이터 맵
     */
    public Map<String, Object> getExamStatistics(String statisticsType) {
        log.info("시험 통계 조회: type={}", statisticsType);

        Map<String, Object> statistics = new java.util.HashMap<>();

        switch (statisticsType) {
            case "by-grade" -> {
                // 학년별 통계
                long grade1Count = examRepository.countByGrade(1);
                long grade2Count = examRepository.countByGrade(2);
                long grade3Count = examRepository.countByGrade(3);
                long totalCount = grade1Count + grade2Count + grade3Count;

                statistics.put("grade1", grade1Count);
                statistics.put("grade2", grade2Count);
                statistics.put("grade3", grade3Count);
                statistics.put("total", totalCount);

                // 비율 계산
                if (totalCount > 0) {
                    Map<String, Double> percentages = new java.util.HashMap<>();
                    percentages.put("grade1Percentage",
                            Math.round((double) grade1Count / totalCount * 100 * 100.0) / 100.0);
                    percentages.put("grade2Percentage",
                            Math.round((double) grade2Count / totalCount * 100 * 100.0) / 100.0);
                    percentages.put("grade3Percentage",
                            Math.round((double) grade3Count / totalCount * 100 * 100.0) / 100.0);
                    statistics.put("percentages", percentages);
                }
            }
            default -> throw new IllegalArgumentException("지원하지 않는 통계 타입입니다: " + statisticsType);
        }

        log.info("시험 통계 조회 완료: type={}, resultKeys={}", statisticsType, statistics.keySet());
        return statistics;
    }

    // ========================================
    // 단원 정보 포함 최적화 메서드
    // ========================================

    /**
     * 시험 ID로 단원 정보가 포함된 상세 정보 조회 (성능 최적화)
     * 
     * <p>
     * 
     * @EntityGraph를 사용하여 N+1 쿼리 문제 없이 한 번의 쿼리로
     *               시험과 관련된 모든 단원 정보를 가져옵니다.
     *               </p>
     * 
     * @param examId 시험 식별자
     * @return 단원 정보가 포함된 시험 DTO
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    public ExamWithUnitsDto findByIdWithUnits(UUID examId) {
        log.info("단원 정보 포함 시험 조회 시작: examId={}", examId);

        // 성능 최적화된 쿼리로 시험 + 단원 정보 조회
        Exam exam = examRepository.findByIdWithUnits(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 응시 현황 정보 계산 (먼저 계산)
        Long actualAttendees = examSubmissionRepository.countByExamId(examId);

        // 기본 시험 DTO 생성 및 실제 총점 계산
        ExamDto examDto = ExamDto.fromWithExamSheet(exam);

        // actualStudentCount를 포함한 ExamDto 생성
        ExamDto examDtoWithCount = new ExamDto(
                examDto.id(),
                examDto.examName(),
                examDto.grade(),
                examDto.content(),
                examDto.maxStudent(),
                actualAttendees.intValue(),
                examDto.qrCodeUrl(),
                examDto.createdAt(),
                examDto.examSheetInfo());

        // 실제 총점 계산 (ExamSheetQuestion.points 합계)
        Integer actualTotalPoints = examSheetQuestionRepository.sumPointsByExamSheetId(exam.getExamSheet().getId());
        ExamDto correctedExamDto = createExamDtoWithCorrectPoints(examDtoWithCount, actualTotalPoints);
        Long totalAssigned = examSubmissionRepository.countStudentsByGrade(exam.getGrade());
        ExamWithUnitsDto.ExamAttendanceInfo attendanceInfo = ExamWithUnitsDto.ExamAttendanceInfo
                .of(actualAttendees.intValue(), totalAssigned.intValue());

        // 단원 정보 추출 및 변환
        List<UnitNameDto> units = extractUnitsFromExam(exam);
        List<ExamWithUnitsDto.UnitQuestionCount> unitQuestionCounts = calculateUnitQuestionCounts(exam);

        log.info("단원 정보 포함 시험 조회 완료: examId={}, unitCount={}, attendanceRate={}%",
                examId, units.size(), attendanceInfo.attendanceRate());

        return ExamWithUnitsDto.from(correctedExamDto, attendanceInfo, units, unitQuestionCounts);
    }

    /**
     * 다중 시험 ID로 단원 정보가 포함된 시험 목록 배치 조회
     * 
     * <p>
     * 여러 시험을 한 번에 조회하여 대시보드나 목록 페이지에서
     * 여러 시험의 단원 정보를 효율적으로 표시할 때 사용합니다.
     * </p>
     * 
     * @param examIds 시험 식별자 목록
     * @return 단원 정보가 포함된 시험 DTO 목록
     */
    public List<ExamWithUnitsDto> findByIdsWithUnits(List<UUID> examIds) {
        log.info("다중 시험 단원 정보 배치 조회 시작: examCount={}", examIds.size());

        if (examIds.isEmpty()) {
            return List.of();
        }

        // 1. 배치 조회 (최적화된 쿼리)
        List<Exam> exams = examRepository.findByIdInWithUnits(examIds);

        // 2. 배치로 제출 통계 조회 (성능 최적화)
        Map<UUID, Long> submissionCounts = examSubmissionRepository.countByExamIds(examIds).stream()
                .collect(Collectors.toMap(
                        stat -> stat.getExamId(),
                        stat -> stat.getSubmissionCount()));

        // 3. 학년별 총 학생 수 조회 (중복 제거)
        Map<Integer, Long> totalStudentsByGrade = exams.stream()
                .map(Exam::getGrade)
                .distinct()
                .collect(Collectors.toMap(
                        grade -> grade,
                        grade -> examSubmissionRepository.countStudentsByGrade(grade)));

        // 4. 각 시험별로 DTO 변환
        List<ExamWithUnitsDto> result = exams.stream()
                .map((Exam exam) -> {
                    // 응시 현황 정보 계산 (먼저 계산)
                    Long actualAttendees = submissionCounts.getOrDefault(exam.getId(), 0L);

                    // ExamDto 생성 및 actualStudentCount 포함
                    ExamDto baseExamDto = ExamDto.fromWithExamSheet(exam);
                    ExamDto examDto = new ExamDto(
                            baseExamDto.id(),
                            baseExamDto.examName(),
                            baseExamDto.grade(),
                            baseExamDto.content(),
                            baseExamDto.maxStudent(),
                            actualAttendees.intValue(),
                            baseExamDto.qrCodeUrl(),
                            baseExamDto.createdAt(),
                            baseExamDto.examSheetInfo());
                    Long totalAssigned = totalStudentsByGrade.getOrDefault(exam.getGrade(), 0L);
                    ExamWithUnitsDto.ExamAttendanceInfo attendanceInfo = ExamWithUnitsDto.ExamAttendanceInfo
                            .of(actualAttendees.intValue(), totalAssigned.intValue());

                    // 단원 정보 추출
                    List<UnitNameDto> units = extractUnitsFromExam(exam);
                    List<ExamWithUnitsDto.UnitQuestionCount> unitQuestionCounts = calculateUnitQuestionCounts(exam);

                    return ExamWithUnitsDto.from(examDto, attendanceInfo, units, unitQuestionCounts);
                })
                .collect(Collectors.toList());

        log.info("다중 시험 단원 정보 배치 조회 완료: examCount={}, resultCount={}, avgAttendanceRate={}%",
                examIds.size(), result.size(),
                result.stream().mapToDouble(dto -> dto.attendanceInfo().attendanceRate()).average().orElse(0.0));
        return result;
    }

    /**
     * Projection을 사용한 경량 단원 정보 조회
     * 
     * <p>
     * 메모리 사용량을 최소화하면서 필요한 단원 정보만 조회합니다.
     * 대량의 시험 데이터를 다룰 때 유용합니다.
     * </p>
     * 
     * @param examId 시험 식별자
     * @return 단원 정보 Projection 목록
     */
    /**
     * 시험별 단원 이름 정보 조회 (간소화된 버전)
     * 
     * <p>
     * 단일 시험에 포함된 단원의 이름 정보만 조회합니다.
     * 복잡한 계층 구조 정보 없이 최고 성능을 제공합니다.
     * </p>
     * 
     * @param examId 시험 고유 식별자
     * @return 단원 이름 정보 목록
     */
    public List<UnitNameDto> findUnitNamesByExamId(UUID examId) {
        log.info("시험별 단원 이름 정보 조회 시작: examId={}", examId);

        List<UnitNameProjection> nameProjections = examRepository.findUnitNamesByExamId(examId);

        List<UnitNameDto> units = nameProjections.stream()
                .map(this::convertProjectionToNameDto)
                .collect(Collectors.toList());

        log.info("시험별 단원 이름 정보 조회 완료: examId={}, unitCount={}", examId, units.size());
        return units;
    }

    public List<UnitSummaryDto> findUnitsByExamId(UUID examId) {
        log.info("시험별 단원 정보 Projection 조회 시작: examId={}", examId);

        List<UnitProjection> unitProjections = examRepository.findUnitsByExamId(examId);

        List<UnitSummaryDto> units = unitProjections.stream()
                .map(this::convertToUnitSummaryDto)
                .collect(Collectors.toList());

        log.info("시험별 단원 정보 Projection 조회 완료: examId={}, unitCount={}", examId, units.size());
        return units;
    }

    /**
     * 단원별 문제 수 통계 조회
     * 
     * <p>
     * 특정 시험의 각 단원별 문제 수와 배점을 계산합니다.
     * 시험 분석이나 리포트 생성에 활용됩니다.
     * </p>
     * 
     * @param examId 시험 식별자
     * @return 단원별 문제 수 통계
     */
    public List<ExamWithUnitsDto.UnitQuestionCount> getUnitQuestionCounts(UUID examId) {
        log.info("시험별 단원 문제 수 통계 조회 시작: examId={}", examId);

        List<Object[]> rawData = examRepository.findUnitQuestionCountsByExamId(examId);

        List<ExamWithUnitsDto.UnitQuestionCount> counts = rawData.stream()
                .map(row -> new ExamWithUnitsDto.UnitQuestionCount(
                        (UUID) row[0], // unitId
                        (String) row[1], // unitName
                        ((Number) row[2]).intValue(), // questionCount
                        ((Number) row[3]).intValue() // totalPoints
                ))
                .collect(Collectors.toList());

        log.info("시험별 단원 문제 수 통계 조회 완료: examId={}, unitCount={}", examId, counts.size());
        return counts;
    }

    /**
     * 다중 시험의 단원별 문제 수 통계 배치 조회
     * 
     * @param examIds 시험 식별자 목록
     * @return 시험별 단원별 문제 수 통계 맵
     */
    public Map<UUID, List<ExamWithUnitsDto.UnitQuestionCount>> getUnitQuestionCountsByExamIds(List<UUID> examIds) {
        log.info("다중 시험 단원 문제 수 통계 배치 조회 시작: examCount={}", examIds.size());

        if (examIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rawData = examRepository.findUnitQuestionCountsByExamIds(examIds);

        Map<UUID, List<ExamWithUnitsDto.UnitQuestionCount>> result = rawData.stream()
                .collect(Collectors.groupingBy(
                        row -> (UUID) row[0], // examId로 그룹핑
                        Collectors.mapping(
                                row -> new ExamWithUnitsDto.UnitQuestionCount(
                                        (UUID) row[1], // unitId
                                        (String) row[2], // unitName
                                        ((Number) row[3]).intValue(), // questionCount
                                        ((Number) row[4]).intValue() // totalPoints
                                ),
                                Collectors.toList())));

        log.info("다중 시험 단원 문제 수 통계 배치 조회 완료: examCount={}, resultCount={}", examIds.size(), result.size());
        return result;
    }

    /**
     * 특정 학년의 모든 시험에서 사용된 단원 목록 조회
     * 
     * <p>
     * 학년별 커리큘럼 분석이나 단원별 출제 빈도 분석에 사용됩니다.
     * </p>
     * 
     * @param grade 학년
     * @return 해당 학년 시험에 사용된 단원 목록
     */
    public List<UnitSummaryDto> getDistinctUnitsByGrade(Integer grade) {
        log.info("학년별 사용된 단원 목록 조회 시작: grade={}", grade);

        List<UnitBasicProjection> basicProjections = examRepository.findDistinctUnitsByGrade(grade);

        List<UnitSummaryDto> units = basicProjections.stream()
                .map(this::convertBasicToUnitSummaryDto)
                .collect(Collectors.toList());

        log.info("학년별 사용된 단원 목록 조회 완료: grade={}, unitCount={}", grade, units.size());
        return units;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * 시험 엔티티에서 단원 정보를 추출하여 DTO로 변환
     */
    private List<UnitNameDto> extractUnitsFromExam(Exam exam) {
        return exam.getExamSheet().getQuestions().stream()
                .map(esq -> esq.getQuestion().getUnit())
                .distinct()
                .map(this::convertUnitToNameDto)
                .sorted((u1, u2) -> u1.unitName().compareTo(u2.unitName()))
                .collect(Collectors.toList());
    }

    /**
     * 시험 엔티티에서 단원별 문제 수를 계산
     */
    private List<ExamWithUnitsDto.UnitQuestionCount> calculateUnitQuestionCounts(Exam exam) {
        Map<UUID, Long> questionCountByUnit = exam.getExamSheet().getQuestions().stream()
                .collect(Collectors.groupingBy(
                        esq -> esq.getQuestion().getUnit().getId(),
                        Collectors.counting()));

        Map<UUID, Integer> totalPointsByUnit = exam.getExamSheet().getQuestions().stream()
                .collect(Collectors.groupingBy(
                        esq -> esq.getQuestion().getUnit().getId(),
                        Collectors.summingInt(esq -> esq.getPoints() != null ? esq.getPoints() : 0)));

        return exam.getExamSheet().getQuestions().stream()
                .map(esq -> esq.getQuestion().getUnit())
                .distinct()
                .map(unit -> new ExamWithUnitsDto.UnitQuestionCount(
                        unit.getId(),
                        unit.getUnitName(),
                        questionCountByUnit.get(unit.getId()).intValue(),
                        totalPointsByUnit.get(unit.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Unit 엔티티를 UnitNameDto로 변환 (간소화된 버전)
     */
    private UnitNameDto convertUnitToNameDto(com.iroomclass.springbackend.domain.unit.entity.Unit unit) {
        return new UnitNameDto(
                unit.getId(),
                unit.getUnitName());
    }

    /**
     * UnitNameProjection을 UnitNameDto로 변환 (간소화된 버전)
     */
    private UnitNameDto convertProjectionToNameDto(UnitNameProjection projection) {
        return new UnitNameDto(
                projection.getId(),
                projection.getUnitName());
    }

    /**
     * Unit 엔티티를 UnitSummaryDto로 변환
     */
    private UnitSummaryDto convertUnitToDto(com.iroomclass.springbackend.domain.unit.entity.Unit unit) {
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
                unit.getSubcategory().getId(),
                unit.getSubcategory().getSubcategoryName(),
                unit.getSubcategory().getDescription(),
                unit.getSubcategory().getDisplayOrder());

        var category = new UnitSummaryDto.UnitCategoryInfo(
                unit.getSubcategory().getCategory().getId(),
                unit.getSubcategory().getCategory().getCategoryName(),
                unit.getSubcategory().getCategory().getDescription(),
                unit.getSubcategory().getCategory().getDisplayOrder());

        return new UnitSummaryDto(
                unit.getId(),
                unit.getUnitName(),
                unit.getUnitCode(),
                unit.getGrade(),
                unit.getDescription(),
                unit.getDisplayOrder(),
                subcategory,
                category);
    }

    /**
     * UnitProjection을 UnitSummaryDto로 변환
     */
    private UnitSummaryDto convertToUnitSummaryDto(UnitProjection projection) {
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
                projection.getSubcategory().getId(),
                projection.getSubcategory().getSubcategoryName(),
                projection.getSubcategory().getDescription(),
                projection.getSubcategory().getDisplayOrder());

        var category = new UnitSummaryDto.UnitCategoryInfo(
                projection.getSubcategory().getCategory().getId(),
                projection.getSubcategory().getCategory().getCategoryName(),
                projection.getSubcategory().getCategory().getDescription(),
                projection.getSubcategory().getCategory().getDisplayOrder());

        return new UnitSummaryDto(
                projection.getId(),
                projection.getUnitName(),
                projection.getUnitCode(),
                projection.getGrade(),
                projection.getDescription(),
                projection.getDisplayOrder(),
                subcategory,
                category);
    }

    /**
     * UnitBasicProjection을 UnitSummaryDto로 변환 (계층 정보 없음)
     */
    private UnitSummaryDto convertBasicToUnitSummaryDto(UnitBasicProjection projection) {
        // 기본 projection은 계층 정보가 없으므로 별도 조회가 필요할 수 있음
        // 여기서는 null로 설정하고, 필요시 추가 조회 로직 구현
        var subcategory = new UnitSummaryDto.UnitSubcategoryInfo(
                null, "정보 없음", null, 0);

        var category = new UnitSummaryDto.UnitCategoryInfo(
                null, "정보 없음", null, 0);

        return new UnitSummaryDto(
                projection.getId(),
                projection.getUnitName(),
                projection.getUnitCode(),
                projection.getGrade(),
                null, // description 없음
                projection.getDisplayOrder(),
                subcategory,
                category);
    }

    /**
     * ExamDto의 totalPoints를 실제 계산된 값으로 수정하여 새 DTO 생성
     */
    private ExamDto createExamDtoWithCorrectPoints(ExamDto originalDto, Integer actualTotalPoints) {
        if (originalDto.examSheetInfo() == null) {
            return originalDto;
        }

        // 수정된 ExamSheetInfo 생성
        ExamDto.ExamSheetInfo correctedExamSheetInfo = new ExamDto.ExamSheetInfo(
                originalDto.examSheetInfo().id(),
                originalDto.examSheetInfo().examName(),
                originalDto.examSheetInfo().totalQuestions(),
                actualTotalPoints != null ? actualTotalPoints : 0,
                originalDto.examSheetInfo().createdAt());

        // 수정된 ExamDto 생성
        return new ExamDto(
                originalDto.id(),
                originalDto.examName(),
                originalDto.grade(),
                originalDto.content(),
                originalDto.maxStudent(),
                originalDto.actualStudentCount(),
                originalDto.qrCodeUrl(),
                originalDto.createdAt(),
                correctedExamSheetInfo);
    }

    /**
     * 특정 시험의 응시자 목록을 페이징하여 조회
     * 
     * <p>
     * 시험 ID를 기반으로 해당 시험에 응시한 학생들의 정보를
     * 페이징 처리하여 조회합니다. 학생 정보와 제출 시간 정보를 포함합니다.
     * </p>
     * 
     * @param examId   시험 ID
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬)
     * @return 응시자 정보 페이지
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public Page<ExamAttendeeDto> getExamAttendees(UUID examId, Pageable pageable) {
        log.info("시험 응시자 조회 시작: examId={}, page={}, size={}",
                examId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 시험 존재 여부 확인
        boolean examExists = examRepository.existsById(examId);
        if (!examExists) {
            log.error("시험을 찾을 수 없습니다: examId={}", examId);
            throw new RuntimeException("시험을 찾을 수 없습니다: " + examId);
        }

        // 2. 응시자 정보 페이징 조회
        Page<ExamSubmission> submissions = examSubmissionRepository.findAttendeesByExamId(examId, pageable);

        // 3. DTO 변환
        Page<ExamAttendeeDto> attendeePage = submissions.map(submission -> {
            // 시험 정보는 별도로 조회 (N+1 문제 방지를 위해 필요시 최적화)
            Exam exam = examRepository.findById(examId).orElse(null);
            if (exam != null) {
                // 시험 정보를 포함한 DTO 생성
                return new ExamAttendeeDto(
                        submission.getId(),
                        submission.getStudent().getId(),
                        submission.getStudent().getName(),
                        submission.getStudent().getPhone(),
                        submission.getStudent().getBirthDate(),
                        submission.getSubmittedAt(),
                        exam.getId(),
                        exam.getExamName());
            } else {
                // 시험 정보 없이 DTO 생성
                return ExamAttendeeDto.fromWithoutExam(submission);
            }
        });

        log.info("시험 응시자 조회 완료: examId={}, 총 {}명 중 {}명 조회 (페이지 {})",
                examId, attendeePage.getTotalElements(),
                attendeePage.getContent().size(),
                pageable.getPageNumber());

        return attendeePage;
    }

    /**
     * 특정 시험의 응시자 목록을 페이징하여 조회 (최적화 버전)
     * 
     * <p>
     * 시험 정보를 한 번만 조회하여 N+1 문제를 방지합니다.
     * 대량의 응시자가 있는 경우 이 메서드를 사용하는 것이 효율적입니다.
     * </p>
     * 
     * @param examId   시험 ID
     * @param pageable 페이징 정보
     * @return 응시자 정보 페이지
     * @throws RuntimeException 시험을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public Page<ExamAttendeeDto> getExamAttendeesOptimized(UUID examId, Pageable pageable) {
        log.info("시험 응시자 최적화 조회 시작: examId={}, page={}, size={}",
                examId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 시험 정보 미리 조회
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("시험을 찾을 수 없습니다: " + examId));

        // 2. 응시자 정보 페이징 조회
        Page<ExamSubmission> submissions = examSubmissionRepository.findAttendeesByExamId(examId, pageable);

        // 3. DTO 변환 (시험 정보 재사용)
        Page<ExamAttendeeDto> attendeePage = submissions.map(submission -> new ExamAttendeeDto(
                submission.getId(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getStudent().getPhone(),
                submission.getStudent().getBirthDate(),
                submission.getSubmittedAt(),
                exam.getId(),
                exam.getExamName()));

        log.info("시험 응시자 최적화 조회 완료: examId={}, 총 {}명 중 {}명 조회",
                examId, attendeePage.getTotalElements(), attendeePage.getContent().size());

        return attendeePage;
    }

    /**
     * 시험 제출 ID로 학생 답안지 조회
     * 
     * <p>
     * 특정 시험 제출에 대한 학생의 전체 답안 정보를 조회합니다.
     * 학생 정보, 시험 정보, 각 문제별 답안을 포함한 상세 정보를 반환합니다.
     * </p>
     * 
     * @param submissionId 시험 제출 ID
     * @return 학생 답안지 상세 정보
     * @throws RuntimeException 제출 정보 또는 답안지를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public ExamAnswerSheetDto getExamAnswerSheet(UUID submissionId) {
        log.info("학생 답안지 조회 시작: submissionId={}", submissionId);

        // 1. 시험 제출 정보 조회
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> {
                    log.error("시험 제출 정보를 찾을 수 없습니다: submissionId={}", submissionId);
                    return new RuntimeException("시험 제출 정보를 찾을 수 없습니다: " + submissionId);
                });

        // 2. 학생 답안지 조회 (문제별 답안 포함)
        StudentAnswerSheet answerSheet = studentAnswerSheetRepository.findBySubmissionIdWithQuestions(submissionId)
                .orElseThrow(() -> {
                    log.error("학생 답안지를 찾을 수 없습니다: submissionId={}", submissionId);
                    return new RuntimeException("학생 답안지를 찾을 수 없습니다: " + submissionId);
                });

        // 3. 채점 결과 조회 (최신 버전)
        List<ExamResult> examResults = examResultRepository.findAllVersionsBySubmissionId(submissionId);
        ExamResult latestResult = null;
        Map<UUID, ExamResultQuestion> resultQuestionMap = null;
        
        if (!examResults.isEmpty()) {
            // 최신 버전 찾기 (version 컬럼이 없으므로 가장 최근 것 사용)
            latestResult = examResults.stream()
                    .filter(r -> r.getStatus() == ExamResult.ResultStatus.COMPLETED)
                    .findFirst()
                    .orElse(examResults.get(0)); // 완료된 채점이 없으면 첫 번째 것 사용
            
            // 문제별 채점 결과 맵 생성
            if (latestResult != null && latestResult.getQuestionResults() != null) {
                resultQuestionMap = latestResult.getQuestionResults().stream()
                        .collect(Collectors.toMap(
                                erq -> erq.getQuestion().getId(),
                                erq -> erq
                        ));
            }
        }
        
        final Map<UUID, ExamResultQuestion> finalResultMap = resultQuestionMap;
        final ExamResult finalLatestResult = latestResult;

        // 4. 학생 정보 DTO 생성
        ExamAnswerSheetDto.StudentInfo studentInfo = new ExamAnswerSheetDto.StudentInfo(
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getStudent().getPhone());

        // 5. 시험 정보 DTO 생성
        Exam exam = submission.getExam();
        ExamAnswerSheetDto.ExamInfo examInfo = new ExamAnswerSheetDto.ExamInfo(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getCreatedAt());

        // 6. 문제별 답안 DTO 리스트 생성
        final AtomicInteger questionCounter = new AtomicInteger(1);
        List<ExamAnswerSheetDto.QuestionAnswerDto> questionAnswers = answerSheet.getStudentAnswerSheetQuestions()
                .stream()
                .map(sheetQuestion -> {
                    Question question = sheetQuestion.getQuestion();

                    // 단원 정보
                    ExamAnswerSheetDto.QuestionAnswerDto.UnitInfo unitInfo = new ExamAnswerSheetDto.QuestionAnswerDto.UnitInfo(
                            question.getUnit().getId(),
                            question.getUnit().getUnitName(),
                            question.getUnit().getUnitCode());

                    // 객관식 선택지 처리
                    List<String> choices = null;
                    if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE
                            && question.getChoices() != null) {
                        try {
                            choices = List.of(question.getChoices());
                        } catch (Exception e) {
                            log.warn("선택지 파싱 실패: questionId={}", question.getId());
                        }
                    }

                    // 채점 결과 정보 추출
                    Boolean isCorrect = null;
                    Integer score = null;
                    String feedback = null;
                    
                    if (finalResultMap != null && finalResultMap.containsKey(question.getId())) {
                        ExamResultQuestion erq = finalResultMap.get(question.getId());
                        isCorrect = erq.getIsCorrect();
                        score = erq.getScore();
                        feedback = erq.getScoringComment();
                    }

                    // 답안 DTO 생성
                    return new ExamAnswerSheetDto.QuestionAnswerDto(
                            questionCounter.getAndIncrement(),
                            question.getId(),
                            question.getQuestionType().toString(),
                            question.getQuestionText(),
                            choices,
                            sheetQuestion.getAnswerContent(),
                            question.getAnswerText(),
                            sheetQuestion.hasAnswer(),
                            isCorrect,  // 채점 결과 포함
                            score,      // 점수 포함
                            question.getPoints(),
                            feedback,   // 피드백 포함
                            unitInfo);
                })
                .collect(Collectors.toList());

        // 7. 채점 결과 정보 DTO 생성
        ExamAnswerSheetDto.GradingResult gradingResult = null;
        if (finalLatestResult != null) {
            // 정답/오답 개수 계산
            long correctCount = questionAnswers.stream()
                    .filter(q -> q.isCorrect() != null && q.isCorrect())
                    .count();
            long wrongCount = questionAnswers.stream()
                    .filter(q -> q.isCorrect() != null && !q.isCorrect())
                    .count();
            
            gradingResult = new ExamAnswerSheetDto.GradingResult(
                    finalLatestResult.getId(),
                    finalLatestResult.getTotalScore(),
                    finalLatestResult.getStatus(),
                    finalLatestResult.getGradedAt(),
                    finalLatestResult.getScoringComment(),
                    (int) correctCount,
                    (int) wrongCount
            );
        }

        // 8. 전체 답안지 DTO 생성
        ExamAnswerSheetDto answerSheetDto = new ExamAnswerSheetDto(
                submissionId,
                studentInfo,
                examInfo,
                submission.getSubmittedAt(),
                answerSheet.getTotalProblemCount(),
                answerSheet.getAnsweredProblemCount(),
                questionAnswers,
                gradingResult  // 채점 결과 추가
        );

        log.info("학생 답안지 조회 완료: submissionId={}, studentName={}, totalQuestions={}, answeredQuestions={}, graded={}",
                submissionId, studentInfo.studentName(),
                answerSheetDto.totalQuestions(), answerSheetDto.answeredQuestions(),
                gradingResult != null);

        return answerSheetDto;
    }
}