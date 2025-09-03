package com.iroomclass.springbackend.config;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.auth.repository.TeacherRepository;
import com.iroomclass.springbackend.domain.curriculum.entity.Unit;
import com.iroomclass.springbackend.domain.curriculum.repository.UnitRepository;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetSelectedUnit;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheetQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetSelectedUnitRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.exam.repository.QuestionResultRepository;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetProblemRepository;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 풍부한 테스트 데이터 생성기
 * 
 * 실제 운영 환경에서 사용할 수 있는 수준의 풍부하고 다양한 테스트 데이터를 생성합니다.
 * 기존 데이터를 먼저 정리한 후 새로운 데이터를 생성합니다.
 * 
 * 생성되는 데이터:
 * - 학생: 각 학년별 40명씩 총 120명
 * - 교사: 총 10명
 * - 시험지: 각 학년별 4개씩 총 12개
 * - 시험지 문제 연결: 시험지당 15-25개 문제
 * - 시험지 선택 단원: 학년별 관련 단원들
 * - 시험: 시험지당 2-4개씩 총 40개
 * - 시험 제출 및 결과: 기본적인 구조만 생성
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RichDataInitializer implements CommandLineRunner {

    // Repository 의존성 주입
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ExamSheetRepository examSheetRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final ExamSheetSelectedUnitRepository examSheetSelectedUnitRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamResultRepository examResultRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResultRepository questionResultRepository;
    private final UnitRepository unitRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final StudentAnswerSheetProblemRepository studentAnswerSheetProblemRepository;
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;

    // JPA EntityManager 주입
    private final EntityManager entityManager;

    /**
     * Spring Boot 시작 시 실행되는 메서드
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("===============================================");
        log.info("RichDataInitializer 실행 시작");
        log.info("===============================================");

        // 안전한 데이터 초기화
        clearExistingData();

        // 풍부한 테스트 데이터 생성
        generateRichTestData();

        log.info("===============================================");
        log.info("RichDataInitializer 실행 완료");
        log.info("===============================================");
    }

    /**
     * 기존 데이터 정리 (보존할 테이블 제외)
     * 
     * 보존할 테이블: unit, unit_category, unit_subcategory, question
     * 삭제할 테이블: 나머지 모든 테이블의 데이터
     */
    @Transactional
    private void clearExistingData() {
        log.info("=== 기존 데이터 정리 시작 ===");
        log.info("보존할 데이터: unit, unit_category, unit_subcategory, question 테이블");

        // 외래키 체크 비활성화
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        try {
            // 삭제할 테이블 목록 (순서 중요 - 참조 관계 고려)
            List<String> tablesToClear = Arrays.asList(
                    "question_result",
                    "exam_result_question",
                    "student_answer_sheet_problem",
                    "student_answer_sheet",
                    "exam_result",
                    "exam_submission",
                    "exam",
                    "exam_sheet_question",
                    "exam_sheet_selected_unit",
                    "exam_sheet",
                    "exam_document",
                    "teacher",
                    "student");

            for (String table : tablesToClear) {
                try {
                    deleteTableData(table, "기존 " + table + " 테이블 데이터");
                } catch (Exception e) {
                    log.warn("테이블 {} 정리 중 오류 (테이블이 존재하지 않을 수 있음): {}", table, e.getMessage());
                }
            }

            log.info("=== 기존 데이터 정리 완료 ===");

        } finally {
            // 외래키 체크 재활성화
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        }
    }

    /**
     * 테이블 데이터 삭제 (트랜잭션 안전)
     */
    private void deleteTableData(String tableName, String description) {
        log.info("{} 삭제 중...", description);
        try {
            // 데이터 삭제
            int deletedCount = entityManager.createNativeQuery("DELETE FROM " + tableName).executeUpdate();
            log.info("{} 삭제 완료: {}건", description, deletedCount);

            // AUTO_INCREMENT 리셋 (해당하는 경우)
            try {
                entityManager.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
            } catch (Exception e) {
                // AUTO_INCREMENT가 없는 테이블의 경우 무시
                log.debug("테이블 {}에는 AUTO_INCREMENT가 없습니다: {}", tableName, e.getMessage());
            }
        } catch (Exception e) {
            log.warn("{} 테이블 삭제 중 오류 (테이블이 존재하지 않을 수 있음): {}", description, e.getMessage());
        }
    }

    /**
     * 풍부한 테스트 데이터 생성
     */
    private void generateRichTestData() {
        log.info("=== 풍부한 가데이터 생성 시작 ===");

        // 기본 데이터 생성
        List<Student> students = createStudents();
        List<Teacher> teachers = createTeachers();
        List<ExamSheet> examSheets = createExamSheets();

        // 시험지 문제 연결 데이터 생성
        log.info("=== 시험지 문제 연결 데이터 생성 시작 ===");
        List<ExamSheetQuestion> examSheetQuestions = createExamSheetQuestions(examSheets);

        // 시험지 선택 단원 데이터 생성
        log.info("=== 시험지 선택 단원 데이터 생성 시작 ===");
        List<ExamSheetSelectedUnit> examSheetSelectedUnits = createExamSheetSelectedUnits(examSheets);

        // 시험 데이터 생성
        log.info("=== 시험 데이터 생성 시작 ===");
        List<Exam> exams = createExams(examSheets);

        // 시험 제출 데이터 생성 (기본적인 구조만)
        log.info("=== 시험 제출 데이터 생성 시작 ===");
        List<ExamSubmission> examSubmissions = createExamSubmissions(students, exams);

        // 학생 답안지 데이터 생성
        log.info("=== 학생 답안지 데이터 생성 시작 ===");
        List<StudentAnswerSheet> studentAnswerSheets = createStudentAnswerSheets(examSubmissions);

        log.info("=== 풍부한 가데이터 생성 완료 ===");
        log.info("최종 데이터: 학생 {}명, 교사 {}명, 시험지 {}개, 시험지문제 {}개, 시험지단원 {}개, 시험 {}개",
                students.size(), teachers.size(), examSheets.size(),
                examSheetQuestionRepository.count(), examSheetSelectedUnitRepository.count(),
                examRepository.count());
    }

    /**
     * 학생 데이터 생성 (각 학년별 40명씩 총 120명)
     */
    private List<Student> createStudents() {
        log.info("학생 데이터 생성 중...");
        List<Student> students = new ArrayList<>();

        for (int grade = 1; grade <= 3; grade++) {
            // 각 학년별로 40명씩 생성
            for (int i = 1; i <= 40; i++) {
                String name = generateKoreanName();
                String phone = generatePhoneNumber();
                LocalDate birthDate = generateBirthDate(grade);

                Student student = Student.builder()
                        .name(name)
                        .phone(phone)
                        .birthDate(birthDate)
                        .build();

                students.add(student);
            }
        }

        List<Student> savedStudents = studentRepository.saveAll(students);
        log.info("학생 {}명 생성 완료", savedStudents.size());
        return savedStudents;
    }

    /**
     * 교사 데이터 생성 (10명)
     */
    private List<Teacher> createTeachers() {
        log.info("교사 데이터 생성 중...");
        List<Teacher> teachers = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String username = String.format("math_teacher%02d", i);
            String password = "password123"; // 실제 환경에서는 암호화 필요

            Teacher teacher = Teacher.builder()
                    .username(username)
                    .password(password)
                    .build();

            teachers.add(teacher);
        }

        List<Teacher> savedTeachers = teacherRepository.saveAll(teachers);
        log.info("교사 {}명 생성 완료", savedTeachers.size());
        return savedTeachers;
    }

    /**
     * 시험지 데이터 생성 (각 학년별 4개씩 총 12개)
     */
    private List<ExamSheet> createExamSheets() {
        log.info("시험지 데이터 생성 중...");
        List<ExamSheet> examSheets = new ArrayList<>();

        for (int grade = 1; grade <= 3; grade++) {
            String[] examTypes = { "1학기 중간고사", "1학기 기말고사", "2학기 중간고사", "2학기 기말고사" };

            for (String examType : examTypes) {
                String examName = String.format("%d학년 %s", grade, examType);

                ExamSheet examSheet = ExamSheet.builder()
                        .examName(examName)
                        .grade(grade)
                        .build();

                examSheets.add(examSheet);
            }
        }

        List<ExamSheet> savedExamSheets = examSheetRepository.saveAll(examSheets);
        log.info("시험지 {}개 생성 완료", savedExamSheets.size());
        return savedExamSheets;
    }

    /**
     * 시험지 문제 연결 데이터 생성 (수정된 버전)
     */
    private List<ExamSheetQuestion> createExamSheetQuestions(List<ExamSheet> examSheets) {
        log.info("시험지 문제 연결 데이터 생성 중...");

        // 전체 문제 개수 확인
        long totalQuestions = questionRepository.count();
        if (totalQuestions == 0) {
            log.warn("question 테이블에 데이터가 없어 시험지 문제 연결을 생성할 수 없습니다.");
            return null;
        }

        log.info("전체 문제 개수: {}개", totalQuestions);

        List<ExamSheetQuestion> examSheetQuestions = new ArrayList<>();
        int totalCreatedConnections = 0;

        for (ExamSheet examSheet : examSheets) {
            // 학년별 문제 개수 결정 (15-25개)
            int questionCount = ThreadLocalRandom.current().nextInt(15, 26);

            // 해당 학년의 문제들을 조회
            List<Question> availableQuestions = questionRepository.findByUnit_Grade(examSheet.getGrade());

            if (availableQuestions.isEmpty()) {
                log.warn("학년 {}에 해당하는 문제가 없습니다. 시험지: {}",
                        examSheet.getGrade(), examSheet.getExamName());
                continue;
            }

            // Question ID 유효성 검증
            List<Question> validQuestions = availableQuestions.stream()
                    .filter(q -> q.getId() != null)
                    .collect(Collectors.toList());

            if (validQuestions.isEmpty()) {
                log.warn("학년 {}에서 유효한 ID를 가진 문제가 없습니다. 시험지: {}",
                        examSheet.getGrade(), examSheet.getExamName());
                continue;
            }

            log.debug("시험지 '{}' ({}학년): 사용 가능한 문제 {}개 중 {}개 선택",
                    examSheet.getExamName(), examSheet.getGrade(),
                    validQuestions.size(), Math.min(questionCount, validQuestions.size()));

            // 문제 선택 - 랜덤 선택
            List<Question> selectedQuestions = selectRandomQuestions(validQuestions,
                    Math.min(questionCount, validQuestions.size()));

            // 선택된 문제들을 시험지에 연결
            for (int i = 0; i < selectedQuestions.size(); i++) {
                Question question = selectedQuestions.get(i);

                // Question ID 재검증
                if (question.getId() == null) {
                    log.error("Question ID가 null입니다. 문제 스킵: index={}", i);
                    continue;
                }

                try {
                    ExamSheetQuestion examSheetQuestion = ExamSheetQuestion.builder()
                            .examSheet(examSheet)
                            .question(question)
                            .seqNo(i + 1) // 순서 번호 (1부터 시작)
                            .points(calculateQuestionPoints(question)) // 문제 점수
                            .selectionMethod(ExamSheetQuestion.SelectionMethod.RANDOM) // 선택 방법
                            .build();

                    examSheetQuestions.add(examSheetQuestion);

                } catch (Exception e) {
                    log.error("ExamSheetQuestion 생성 중 오류. question_id={}, examSheet_id={}, error={}",
                            question.getId(), examSheet.getId(), e.getMessage());
                    continue;
                }
            }

            totalCreatedConnections += selectedQuestions.size();
        }

        // 배치 저장
        if (!examSheetQuestions.isEmpty()) {
            try {
                List<ExamSheetQuestion> savedQuestions = examSheetQuestionRepository.saveAll(examSheetQuestions);
                log.info("시험지 문제 연결 {}개 생성 완료 (평균 시험지당 {:.1f}개)",
                        savedQuestions.size(),
                        (double) totalCreatedConnections / examSheets.size());

                return savedQuestions;
            } catch (Exception e) {
                log.error("시험지 문제 연결 저장 중 오류: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.warn("생성된 시험지 문제 연결이 없습니다.");
        }

        return null;
    }

    /**
     * 리스트에서 랜덤하게 지정된 개수만큼 선택
     */
    private List<Question> selectRandomQuestions(List<Question> questions, int count) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        // 요청한 개수가 사용 가능한 문제보다 많으면 전체 선택
        if (count >= questions.size()) {
            return new ArrayList<>(questions);
        }

        // 랜덤 선택
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * 문제 배점 계산
     */
    private Integer calculateQuestionPoints(Question question) {
        // 난이도별 기본 배점
        return switch (question.getDifficulty()) {
            case 하 -> 3; // 쉬운 문제: 3점
            case 중 -> 5; // 중간 문제: 5점
            case 상 -> 7; // 어려운 문제: 7점
        };
    }

    /**
     * 시험지 선택 단원 데이터 생성
     */
    private List<ExamSheetSelectedUnit> createExamSheetSelectedUnits(List<ExamSheet> examSheets) {
        log.info("시험지 선택 단원 데이터 생성 중...");

        List<ExamSheetSelectedUnit> selectedUnits = new ArrayList<>();

        for (ExamSheet examSheet : examSheets) {
            // 해당 학년의 단원들을 조회
            List<Unit> availableUnits = unitRepository.findByGradeOrderById(examSheet.getGrade());

            if (availableUnits.isEmpty()) {
                log.warn("학년 {}에 해당하는 단원이 없습니다.", examSheet.getGrade());
                continue;
            }

            // 시험지당 2-4개의 단원 선택
            int unitCount = ThreadLocalRandom.current().nextInt(2, Math.min(5, availableUnits.size() + 1));
            Collections.shuffle(availableUnits);
            List<Unit> selectedUnitsForExam = availableUnits.subList(0, unitCount);

            // 선택된 단원들을 시험지에 연결
            for (Unit unit : selectedUnitsForExam) {
                ExamSheetSelectedUnit selectedUnit = ExamSheetSelectedUnit.builder()
                        .examSheet(examSheet)
                        .unit(unit)
                        .build();

                selectedUnits.add(selectedUnit);
            }
        }

        // 배치 저장
        if (!selectedUnits.isEmpty()) {
            List<ExamSheetSelectedUnit> savedUnits = examSheetSelectedUnitRepository.saveAll(selectedUnits);
            log.info("시험지 선택 단원 {}개 생성 완료", savedUnits.size());

            return savedUnits;
        }

        return null;
    }

    /**
     * 시험 데이터 생성 (시험지당 2-4개씩)
     */
    private List<Exam> createExams(List<ExamSheet> examSheets) {
        log.info("시험 데이터 생성 중...");
        List<Exam> exams = new ArrayList<>();

        for (ExamSheet examSheet : examSheets) {
            // 시험지당 2-4개의 시험 생성
            int examCount = ThreadLocalRandom.current().nextInt(2, 5);

            for (int i = 1; i <= examCount; i++) {
                String examName = String.format("%s - %d차", examSheet.getExamName(), i);
                String content = String.format("%s 시험 내용", examName);

                Exam exam = Exam.builder()
                        .examName(examName)
                        .examSheet(examSheet)
                        .grade(examSheet.getGrade())
                        .content(content)
                        .build();

                exams.add(exam);
            }
        }

        List<Exam> savedExams = examRepository.saveAll(exams);
        log.info("시험 {}개 생성 완료", savedExams.size());
        return savedExams;
    }

    /**
     * 시험 제출 데이터 생성 (기본적인 구조만)
     */
    private List<ExamSubmission> createExamSubmissions(List<Student> students, List<Exam> exams) {
        log.info("시험 제출 데이터 생성 중...");

        List<ExamSubmission> examSubmissions = new ArrayList<>();

        for (Exam exam : exams) {
            log.debug("시험 '{}' 제출 데이터 생성 중...", exam.getExamName());

            // 해당 학년 학생들만 필터링
            List<Student> eligibleStudents = students.stream()
                    .filter(student -> getStudentGrade(student.getBirthDate()) == exam.getGrade())
                    .collect(Collectors.toList());

            // 50-80% 제출율로 랜덤 선택
            int submissionRate = ThreadLocalRandom.current().nextInt(50, 81);
            int submissionCount = (int) (eligibleStudents.size() * submissionRate / 100.0);

            Collections.shuffle(eligibleStudents);
            List<Student> submittingStudents = eligibleStudents.subList(0,
                    Math.min(submissionCount, eligibleStudents.size()));

            log.debug("시험 '{}': 대상 학생 {}명 중 {}명 제출 (제출율: {}%)",
                    exam.getExamName(), eligibleStudents.size(), submittingStudents.size(), submissionRate);

            for (Student student : submittingStudents) {
                // 제출 시간 생성 (현재 시간 기준 과거 1-30일)
                LocalDateTime submittedAt = LocalDateTime.now()
                        .minusDays(ThreadLocalRandom.current().nextInt(1, 31))
                        .withHour(ThreadLocalRandom.current().nextInt(9, 18))
                        .withMinute(ThreadLocalRandom.current().nextInt(0, 60));

                ExamSubmission submission = ExamSubmission.builder()
                        .exam(exam)
                        .student(student)
                        .submittedAt(submittedAt)
                        .build();

                examSubmissions.add(submission);
            }
        }

        // 배치 저장
        if (!examSubmissions.isEmpty()) {
            List<ExamSubmission> savedSubmissions = examSubmissionRepository.saveAll(examSubmissions);
            log.info("시험 제출 {}개 생성 완료", savedSubmissions.size());
        }

        return examSubmissions;
    }

    private List<StudentAnswerSheet> createStudentAnswerSheets(List<ExamSubmission> examSubmissions) {
        log.info("학생 답안지 데이터 생성 중...");
        List<StudentAnswerSheet> studentAnswerSheets = new ArrayList<>();

        for (ExamSubmission examSubmission : examSubmissions) {
            String studentName = examSubmission.getStudent().getName();

            StudentAnswerSheet studentAnswerSheet = StudentAnswerSheet.builder()
                    .studentAnswerSheetQuestions(new ArrayList<>())
                    .examSubmission(examSubmission)
                    .studentName(studentName)
                    .build();
            studentAnswerSheets.add(studentAnswerSheet);
        }

        if (!studentAnswerSheets.isEmpty()) {
            List<StudentAnswerSheet> savedSheets = studentAnswerSheetRepository.saveAll(studentAnswerSheets);
            log.info("학생 답안지 {}개 생성 완료", savedSheets.size());

            return savedSheets;
        }

        return null;
    }

    /**
     * 생년월일을 기준으로 학년 계산
     */
    private Integer getStudentGrade(LocalDate birthDate) {
        int birthYear = birthDate.getYear();
        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear;

        // 나이를 기준으로 학년 추정 (대략적인 계산)
        if (age >= 8 && age <= 10)
            return 1; // 1학년 (8-10세)
        if (age >= 11 && age <= 13)
            return 2; // 2학년 (11-13세)
        if (age >= 14 && age <= 16)
            return 3; // 3학년 (14-16세)
        return 1; // 기본값
    }

    /**
     * 한국식 이름 생성
     */
    private String generateKoreanName() {
        String[] lastNames = { "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송",
                "류", "전" };
        String[] firstNames = { "민준", "서연", "도윤", "하은", "시우", "아린", "주원", "지우", "건우", "서현", "우진", "지민", "현우", "서준",
                "민서", "예준", "하준", "유진", "서율", "시현" };

        String lastName = lastNames[ThreadLocalRandom.current().nextInt(lastNames.length)];
        String firstName = firstNames[ThreadLocalRandom.current().nextInt(firstNames.length)];

        return lastName + firstName;
    }

    /**
     * 전화번호 생성 (010-xxxx-xxxx 형식)
     */
    private String generatePhoneNumber() {
        int middle = ThreadLocalRandom.current().nextInt(1000, 10000);
        int last = ThreadLocalRandom.current().nextInt(1000, 10000);
        return String.format("010-%04d-%04d", middle, last);
    }

    /**
     * 학년별 생년월일 생성
     */
    private LocalDate generateBirthDate(int grade) {
        int currentYear = LocalDate.now().getYear();
        int birthYear = switch (grade) {
            case 1 -> currentYear - ThreadLocalRandom.current().nextInt(8, 11); // 1학년: 8-10세
            case 2 -> currentYear - ThreadLocalRandom.current().nextInt(11, 14); // 2학년: 11-13세
            case 3 -> currentYear - ThreadLocalRandom.current().nextInt(14, 17); // 3학년: 14-16세
            default -> currentYear - 10; // 기본값
        };

        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 29); // 안전하게 28일까지

        return LocalDate.of(birthYear, month, day);
    }
}