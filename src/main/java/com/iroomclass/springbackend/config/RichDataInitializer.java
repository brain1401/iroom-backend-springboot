package com.iroomclass.springbackend.config;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.auth.repository.TeacherRepository;
import com.iroomclass.springbackend.domain.curriculum.entity.Unit;
import com.iroomclass.springbackend.domain.curriculum.repository.UnitRepository;
import com.iroomclass.springbackend.domain.exam.entity.*;
import com.iroomclass.springbackend.domain.exam.repository.*;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetProblemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 풍부한 가데이터를 생성하는 초기화 컴포넌트
 * 
 * <p>실제 중학교 환경을 시뮬레이션하는 현실적이고 풍부한 테스트 데이터를 생성합니다.
 * units 관련 테이블과 question 테이블은 제외하고, question_result는 포함합니다.</p>
 * 
 * <p>생성되는 데이터:</p>
 * <ul>
 * <li>학생: 각 학년별 40명씩 총 120명 (1,2,3학년만)</li>
 * <li>교사: 10명의 수학 교사</li>
 * <li>시험지: 각 학년별 4개씩 총 12개 (중간/기말고사)</li>
 * <li>시험: 시험지별 1개씩 총 12개</li>
 * <li>시험 제출: 약 80-90% 제출률</li>
 * <li>시험 결과 및 문제별 결과: 현실적인 점수 분포</li>
 * <li>답안지: 제출별 답안 기록</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@Profile({"dev", "local"}) // 개발 환경에서만 실행
@RequiredArgsConstructor
@Slf4j
public class RichDataInitializer implements CommandLineRunner {
    
    // Repository 의존성
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ExamSheetRepository examSheetRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final ExamSheetSelectedUnitRepository examSheetSelectedUnitRepository;
    private final UnitRepository unitRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamResultRepository examResultRepository;
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;
    private final StudentAnswerSheetProblemRepository studentAnswerSheetProblemRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResultRepository questionResultRepository;
    
    // 한국식 성씨와 이름 데이터
    private static final String[] KOREAN_SURNAMES = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"
    };
    
    private static final String[] KOREAN_GIVEN_NAMES = {
        "지훈", "예은", "서준", "하은", "도현", "시은", "주원", "채원", "건우", "유나",
        "현서", "윤서", "민준", "서윤", "시우", "지우", "준서", "지안", "현준", "예린",
        "수빈", "소율", "민서", "하준", "다온", "아린", "율", "시현", "진우", "유진",
        "민재", "서영", "태현", "수아", "지호", "채은", "승우", "연우", "지민", "서현"
    };
    
    // 시험명 템플릿
    private static final Map<Integer, String[]> EXAM_NAMES_BY_GRADE = Map.of(
        1, new String[]{"1학년 1학기 중간고사", "1학년 1학기 기말고사", "1학년 2학기 중간고사", "1학년 2학기 기말고사"},
        2, new String[]{"2학년 1학기 중간고사", "2학년 1학기 기말고사", "2학년 2학기 중간고사", "2학년 2학기 기말고사"},
        3, new String[]{"3학년 1학기 중간고사", "3학년 1학기 기말고사", "3학년 2학기 중간고사", "3학년 2학기 기말고사"}
    );
    
    private final Random random = new Random();
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== 데이터 상태 확인 중 ===");
        
        boolean hasStudents = studentRepository.count() > 10;
        boolean hasTeachers = teacherRepository.count() > 5;
        boolean hasExamSheets = examSheetRepository.count() > 0;
        boolean hasExamSheetQuestions = examSheetQuestionRepository.count() > 0;
        boolean hasExamSheetSelectedUnits = examSheetSelectedUnitRepository.count() > 0;
        
        log.info("데이터 상태: 학생={}, 교사={}, 시험지={}, 시험지문제={}, 시험지단원={}", 
                hasStudents, hasTeachers, hasExamSheets, hasExamSheetQuestions, hasExamSheetSelectedUnits);
        
        // 기본 데이터 생성 (없을 경우만)
        List<Student> students = hasStudents ? studentRepository.findAll() : createStudents();
        List<Teacher> teachers = hasTeachers ? teacherRepository.findAll() : createTeachers();
        List<ExamSheet> examSheets = hasExamSheets ? examSheetRepository.findAll() : createExamSheets();
        
        // 중요: 시험지 관련 테이블은 데이터가 없으면 항상 생성
        if (!hasExamSheetQuestions && !examSheets.isEmpty()) {
            log.info("=== 시험지 문제 연결 데이터 생성 시작 ===");
            createExamSheetQuestions(examSheets);
        } else if (hasExamSheetQuestions) {
            log.info("시험지 문제 연결 데이터가 이미 존재합니다.");
        }
        
        if (!hasExamSheetSelectedUnits && !examSheets.isEmpty()) {
            log.info("=== 시험지 선택 단원 데이터 생성 시작 ===");
            createExamSheetSelectedUnits(examSheets);
        } else if (hasExamSheetSelectedUnits) {
            log.info("시험지 선택 단원 데이터가 이미 존재합니다.");
        }
        
        // 시험 및 결과 데이터 생성 (필요한 경우만)
        boolean hasExams = examRepository.count() > 0;
        List<Exam> exams = null;
        
        if (!hasExams && !examSheets.isEmpty()) {
            log.info("=== 시험 데이터 생성 시작 ===");
            exams = createExams(examSheets);
        } else if (hasExams) {
            log.info("시험 데이터가 이미 존재합니다.");
            exams = examRepository.findAll();
        }
        
        // 시험 제출 및 결과 생성 (필요한 경우만)
        boolean hasExamSubmissions = examSubmissionRepository.count() > 0;
        if (!hasExamSubmissions && exams != null && !exams.isEmpty() && !students.isEmpty()) {
            log.info("=== 시험 제출 및 결과 데이터 생성 시작 ===");
            createExamSubmissionsAndResults(students, exams);
        } else if (hasExamSubmissions) {
            log.info("시험 제출 및 결과 데이터가 이미 존재합니다.");
        }
        
        log.info("=== 데이터 초기화 완료 ===");
        log.info("최종 데이터: 학생 {}명, 교사 {}명, 시험지 {}개, 시험지문제 {}개, 시험지단원 {}개", 
                students.size(), teachers.size(), examSheets.size(), 
                examSheetQuestionRepository.count(), examSheetSelectedUnitRepository.count());
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
            String[] examNames = EXAM_NAMES_BY_GRADE.get(grade);
            
            for (String examName : examNames) {
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
     * 시험지별 문제 연결 데이터 생성
     * 각 시험지에 학년별로 적절한 문제들을 15-25개씩 선택하여 연결
     */
    private void createExamSheetQuestions(List<ExamSheet> examSheets) {
        log.info("시험지 문제 연결 데이터 생성 중...");
        
        List<ExamSheetQuestion> examSheetQuestions = new ArrayList<>();
        
        for (ExamSheet examSheet : examSheets) {
            // 학년별 문제 개수 결정 (15-25개)
            int questionCount = ThreadLocalRandom.current().nextInt(15, 26);
            
            // 해당 학년의 문제들을 조회 (실제 DB에서 조회)
            List<Question> availableQuestions = questionRepository.findByUnit_Grade(examSheet.getGrade());
            
            if (availableQuestions.isEmpty()) {
                log.warn("학년 {}에 해당하는 문제가 없습니다. 시험지: {}", 
                        examSheet.getGrade(), examSheet.getExamName());
                continue;
            }
            
            // 문제 선택 - 난이도별로 균등하게 분배
            List<Question> selectedQuestions = selectQuestionsForExamSheet(
                availableQuestions, questionCount);
            
            // 선택된 문제들을 시험지에 연결
            for (int i = 0; i < selectedQuestions.size(); i++) {
                Question question = selectedQuestions.get(i);
                
                ExamSheetQuestion examSheetQuestion = ExamSheetQuestion.builder()
                    .examSheet(examSheet)
                    .question(question)
                    .seqNo(i + 1)  // 순서 번호 (1부터 시작)
                    .points(calculateQuestionPoints(question))  // 문제 점수
                    .selectionMethod(ExamSheetQuestion.SelectionMethod.RANDOM)  // 선택 방법
                    .build();
                
                examSheetQuestions.add(examSheetQuestion);
            }
        }
        
        // 배치 저장
        List<ExamSheetQuestion> savedQuestions = examSheetQuestionRepository.saveAll(examSheetQuestions);
        log.info("시험지 문제 연결 {}개 생성 완료", savedQuestions.size());
    }
    
    /**
     * 시험지용 문제 선택 - 난이도별 균등 분배
     */
    private List<Question> selectQuestionsForExamSheet(List<Question> availableQuestions, int totalCount) {
        // 난이도별로 문제 분류
        Map<Question.Difficulty, List<Question>> questionsByDifficulty = availableQuestions.stream()
            .collect(Collectors.groupingBy(Question::getDifficulty));
        
        List<Question> selectedQuestions = new ArrayList<>();
        
        // 난이도별 목표 개수 계산 (하:50%, 중:30%, 상:20%)
        int easyCount = (int) (totalCount * 0.5);
        int mediumCount = (int) (totalCount * 0.3);
        int hardCount = totalCount - easyCount - mediumCount;
        
        // 하급 문제 선택
        List<Question> easyQuestions = questionsByDifficulty.getOrDefault(
            Question.Difficulty.하, new ArrayList<>());
        selectedQuestions.addAll(selectRandomQuestions(easyQuestions, easyCount));
        
        // 중급 문제 선택
        List<Question> mediumQuestions = questionsByDifficulty.getOrDefault(
            Question.Difficulty.중, new ArrayList<>());
        selectedQuestions.addAll(selectRandomQuestions(mediumQuestions, mediumCount));
        
        // 상급 문제 선택
        List<Question> hardQuestions = questionsByDifficulty.getOrDefault(
            Question.Difficulty.상, new ArrayList<>());
        selectedQuestions.addAll(selectRandomQuestions(hardQuestions, hardCount));
        
        // 부족한 문제가 있을 경우 전체에서 랜덤 선택으로 보충
        while (selectedQuestions.size() < totalCount && selectedQuestions.size() < availableQuestions.size()) {
            Question randomQuestion = availableQuestions.get(
                ThreadLocalRandom.current().nextInt(availableQuestions.size()));
            if (!selectedQuestions.contains(randomQuestion)) {
                selectedQuestions.add(randomQuestion);
            }
        }
        
        return selectedQuestions;
    }
    
    /**
     * 리스트에서 랜덤하게 지정된 개수만큼 선택
     */
    private List<Question> selectRandomQuestions(List<Question> questions, int count) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        
        int actualCount = Math.min(count, shuffled.size());
        return shuffled.subList(0, actualCount);
    }
    
    /**
     * 문제 유형과 난이도에 따른 점수 계산
     */
    private int calculateQuestionPoints(Question question) {
        return switch (question.getDifficulty()) {
            case 하 -> question.isMultipleChoice() ? 3 : 5;  // 하급: 객관식 3점, 주관식 5점
            case 중 -> question.isMultipleChoice() ? 4 : 7;  // 중급: 객관식 4점, 주관식 7점
            case 상 -> question.isMultipleChoice() ? 5 : 10; // 상급: 객관식 5점, 주관식 10점
        };
    }

    /**
     * 시험지별 선택 단원 데이터 생성
     * 각 시험지에 2-5개의 단원을 랜덤 선택하여 연결
     */
    private void createExamSheetSelectedUnits(List<ExamSheet> examSheets) {
        log.info("시험지 선택 단원 데이터 생성 중...");
        
        List<ExamSheetSelectedUnit> examSheetSelectedUnits = new ArrayList<>();
        
        for (ExamSheet examSheet : examSheets) {
            // 해당 학년의 단원들을 조회
            List<Unit> availableUnits = unitRepository.findByGradeOrderByDisplayOrder(examSheet.getGrade());
            
            if (availableUnits.isEmpty()) {
                log.warn("학년 {}에 해당하는 단원이 없습니다. 시험지: {}", 
                        examSheet.getGrade(), examSheet.getExamName());
                continue;
            }
            
            // 시험지별 선택할 단원 개수 (2-5개)
            int unitCount = ThreadLocalRandom.current().nextInt(2, Math.min(6, availableUnits.size() + 1));
            
            // 랜덤하게 단원 선택
            List<Unit> selectedUnits = selectRandomUnits(availableUnits, unitCount);
            
            // 선택된 단원들을 시험지에 연결
            for (Unit unit : selectedUnits) {
                ExamSheetSelectedUnit examSheetSelectedUnit = ExamSheetSelectedUnit.builder()
                    .examSheet(examSheet)
                    .unit(unit)
                    .build();
                
                examSheetSelectedUnits.add(examSheetSelectedUnit);
            }
        }
        
        // 배치 저장
        if (!examSheetSelectedUnits.isEmpty()) {
            List<ExamSheetSelectedUnit> savedUnits = examSheetSelectedUnitRepository.saveAll(examSheetSelectedUnits);
            log.info("시험지 선택 단원 {}개 생성 완료", savedUnits.size());
        } else {
            log.warn("생성된 시험지 선택 단원이 없습니다. Unit 테이블에 데이터가 있는지 확인해주세요.");
        }
    }
    
    /**
     * 리스트에서 랜덤하게 지정된 개수만큼 단원 선택 (중복 없음)
     */
    private List<Unit> selectRandomUnits(List<Unit> units, int count) {
        if (units.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Unit> shuffled = new ArrayList<>(units);
        Collections.shuffle(shuffled);
        
        int actualCount = Math.min(count, shuffled.size());
        return shuffled.subList(0, actualCount);
    }
    
    /**
     * 시험 데이터 생성 (시험지별 1개씩)
     */
    private List<Exam> createExams(List<ExamSheet> examSheets) {
        log.info("시험 데이터 생성 중...");
        List<Exam> exams = new ArrayList<>();
        
        for (ExamSheet examSheet : examSheets) {
            String content = examSheet.getExamName() + " 수학 시험입니다. 15-25문제로 구성되어 있습니다.";
            
            Exam exam = Exam.builder()
                .examSheet(examSheet)
                .examName(examSheet.getExamName())
                .grade(examSheet.getGrade())
                .content(content)
                .qrCodeUrl("https://example.com/qr/" + UUID.randomUUID())
                .build();
            
            exams.add(exam);
        }
        
        List<Exam> savedExams = examRepository.saveAll(exams);
        log.info("시험 {}개 생성 완료", savedExams.size());
        return savedExams;
    }
    
    /**
     * 시험 제출 및 결과 데이터 생성
     */
    private void createExamSubmissionsAndResults(List<Student> students, List<Exam> exams) {
        log.info("시험 제출 및 결과 데이터 생성 중...");
        
        // 학년별 학생 그룹화
        Map<Integer, List<Student>> studentsByGrade = new HashMap<>();
        for (Student student : students) {
            LocalDate birthDate = student.getBirthDate();
            int grade = calculateGrade(birthDate);
            studentsByGrade.computeIfAbsent(grade, k -> new ArrayList<>()).add(student);
        }
        
        int totalSubmissions = 0;
        
        for (Exam exam : exams) {
            List<Student> gradeStudents = studentsByGrade.get(exam.getGrade());
            if (gradeStudents == null) continue;
            
            // 80-90% 제출률로 랜덤 선택
            double submissionRate = 0.8 + (random.nextDouble() * 0.1);
            int submissionCount = (int) (gradeStudents.size() * submissionRate);
            
            Collections.shuffle(gradeStudents);
            List<Student> submittingStudents = gradeStudents.subList(0, submissionCount);
            
            for (Student student : submittingStudents) {
                // 시험 제출 생성
                ExamSubmission submission = createExamSubmission(exam, student);
                ExamSubmission savedSubmission = examSubmissionRepository.save(submission);
                
                // 시험 결과 생성
                ExamResult result = createExamResult(savedSubmission, exam.getExamSheet());
                ExamResult savedResult = examResultRepository.save(result);
                
                // 문제별 채점 결과 생성 (question_result 테이블)
                createExamResultQuestions(savedResult, exam.getExamSheet());
                
                totalSubmissions++;
            }
        }
        
        log.info("시험 제출 및 결과 {}개 생성 완료", totalSubmissions);
    }
    
    /**
     * 시험 제출 생성
     */
    private ExamSubmission createExamSubmission(Exam exam, Student student) {
        // 제출일시는 시험 생성 후 1-30일 사이 랜덤
        LocalDateTime submittedAt = LocalDateTime.now()
            .minusDays(ThreadLocalRandom.current().nextInt(1, 31))
            .withHour(ThreadLocalRandom.current().nextInt(9, 17))
            .withMinute(ThreadLocalRandom.current().nextInt(0, 60));
        
        return ExamSubmission.builder()
            .exam(exam)
            .student(student)
            .submittedAt(submittedAt)
            .build();
    }
    
    /**
     * 시험 결과 생성
     */
    private ExamResult createExamResult(ExamSubmission submission, ExamSheet examSheet) {
        // 현실적인 점수 분포 생성 (정규분포 기반)
        double mean = 75.0; // 평균 75점
        double stdDev = 15.0; // 표준편차 15
        
        // 정규분포로 점수 생성 후 0-100 범위로 제한
        double rawScore = random.nextGaussian() * stdDev + mean;
        int totalScore = Math.max(0, Math.min(100, (int) Math.round(rawScore)));
        
        String comment = generateScoringComment(totalScore);
        
        return ExamResult.builder()
            .examSubmission(submission)
            .examSheet(examSheet)
            .totalScore(totalScore)
            .status(ExamResult.ResultStatus.COMPLETED)
            .scoringComment(comment)
            .version(1)
            .build();
    }
    
    /**
     * 학생 답안지 생성 (새로운 구조: 컨테이너 + 개별 문제)
     */
    private StudentAnswerSheet createAnswerSheet(ExamSubmission submission) {
        return StudentAnswerSheet.builder()
            .examSubmission(submission)
            .studentName(submission.getStudent().getName())
            .build();
    }
    
    /**
     * 개별 문제 답안 생성
     */
    private StudentAnswerSheetProblem createAnswerSheetProblem(StudentAnswerSheet answerSheet, Question question) {
        // 객관식/주관식에 따라 답안 생성
        StudentAnswerSheetProblem.StudentAnswerSheetProblemBuilder builder = StudentAnswerSheetProblem.builder()
            .studentAnswerSheet(answerSheet)
            .question(question);
        
        // 답안 내용 랜덤 생성
        if (question.isMultipleChoice()) {
            // 객관식: 1-5 중 랜덤 선택
            int selectedChoice = ThreadLocalRandom.current().nextInt(1, 6);
            builder.selectedChoice(selectedChoice);
        } else {
            // 주관식: 간단한 답안 텍스트
            String answerText = "학생이 작성한 주관식 답안 내용입니다.";
            builder.answerText(answerText);
        }
        
        return builder.build();
    }
    
    /**
     * 한국식 이름 생성
     */
    private String generateKoreanName() {
        String surname = KOREAN_SURNAMES[random.nextInt(KOREAN_SURNAMES.length)];
        String givenName = KOREAN_GIVEN_NAMES[random.nextInt(KOREAN_GIVEN_NAMES.length)];
        return surname + givenName;
    }
    
    /**
     * 전화번호 생성 (010-XXXX-XXXX 형식)
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
        int birthYear = switch (grade) {
            case 1 -> 2012; // 만 13세 (중1)
            case 2 -> 2011; // 만 14세 (중2)  
            case 3 -> 2010; // 만 15세 (중3)
            default -> 2012;
        };
        
        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 29);
        
        return LocalDate.of(birthYear, month, day);
    }
    
    /**
     * 생년월일로 학년 계산
     */
    private int calculateGrade(LocalDate birthDate) {
        int birthYear = birthDate.getYear();
        return switch (birthYear) {
            case 2012 -> 1;
            case 2011 -> 2;
            case 2010 -> 3;
            default -> 1;
        };
    }
    
    /**
     * 점수별 채점 코멘트 생성
     */
    private String generateScoringComment(int score) {
        if (score >= 90) {
            return "매우 우수한 성적입니다. 모든 문제를 정확하게 해결했습니다.";
        } else if (score >= 80) {
            return "우수한 성적입니다. 대부분의 문제를 잘 해결했습니다.";
        } else if (score >= 70) {
            return "양호한 성적입니다. 몇 가지 개념을 더 보완하면 좋겠습니다.";
        } else if (score >= 60) {
            return "보통 성적입니다. 기초 개념 학습이 더 필요합니다.";
        } else {
            return "기초 개념 학습이 많이 필요합니다. 추가 학습을 권장합니다.";
        }
    }
    
    /**
     * 문제별 채점 결과 생성 (새로운 구조에 맞춰 수정)
     */
    private void createExamResultQuestions(ExamResult examResult, ExamSheet examSheet) {
        // exam_sheet_question 테이블에서 해당 시험지의 실제 문제들을 조회
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository.findByExamSheetIdOrderBySeqNo(examSheet.getId());
        
        if (examSheetQuestions.isEmpty()) {
            log.warn("시험지 '{}'에 연결된 문제가 없어 문제별 채점 결과를 생성할 수 없습니다.", examSheet.getExamName());
            return;
        }
        
        // 1. 먼저 StudentAnswerSheet 컨테이너 생성
        StudentAnswerSheet answerSheet = createAnswerSheet(examResult.getExamSubmission());
        StudentAnswerSheet savedAnswerSheet = studentAnswerSheetRepository.save(answerSheet);
        
        // 2. 각 문제별로 StudentAnswerSheetProblem과 ExamResultQuestion 생성
        List<ExamResultQuestion> questionResults = new ArrayList<>();
        List<StudentAnswerSheetProblem> answerSheetProblems = new ArrayList<>();
        
        for (ExamSheetQuestion examSheetQuestion : examSheetQuestions) {
            Question question = examSheetQuestion.getQuestion();
            
            // 개별 문제 답안 생성
            StudentAnswerSheetProblem answerProblem = createAnswerSheetProblem(savedAnswerSheet, question);
            StudentAnswerSheetProblem savedAnswerProblem = studentAnswerSheetProblemRepository.save(answerProblem);
            answerSheetProblems.add(savedAnswerProblem);
            
            // 현실적인 점수 분포 생성
            boolean isCorrect = generateIsCorrect(examResult.getTotalScore());
            int questionMaxScore = question.getPoints() != null ? question.getPoints() : 10;
            int questionScore = generateQuestionScore(isCorrect, questionMaxScore);
            
            // 채점 방법 결정 (객관식은 자동, 주관식은 AI 보조)
            ExamResultQuestion.ScoringMethod scoringMethod = 
                Question.QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType()) ? 
                ExamResultQuestion.ScoringMethod.AUTO : 
                ExamResultQuestion.ScoringMethod.AI_ASSISTED;
            
            // AI 신뢰도 생성 (AI 채점인 경우)
            BigDecimal confidence = scoringMethod == ExamResultQuestion.ScoringMethod.AI_ASSISTED ?
                generateAiConfidence() : BigDecimal.ONE;
            
            String comment = generateQuestionComment(isCorrect, questionScore, questionMaxScore);
            
            ExamResultQuestion questionResult = ExamResultQuestion.builder()
                .examResult(examResult)
                .question(question)  // 문제 참조 추가
                .studentAnswerSheet(savedAnswerSheet)  // 컨테이너를 참조
                .isCorrect(isCorrect)
                .score(questionScore)
                .scoringMethod(scoringMethod)
                .scoringComment(comment)
                .confidenceScore(confidence)
                .build();
            
            questionResults.add(questionResult);
        }
        
        // 배치로 저장
        List<ExamResultQuestion> savedResults = questionResultRepository.saveAll(questionResults);
        
        // ExamResult의 총점 업데이트
        examResult.calculateAndUpdateTotalScore();
        examResultRepository.save(examResult);
        
        log.debug("문제별 채점 결과 {}개 생성 완료 (답안지 문제 {}개)", savedResults.size(), answerSheetProblems.size());
    }
    
    /**
     * 전체 점수를 기반으로 개별 문제 정답 여부 생성
     */
    private boolean generateIsCorrect(int totalScore) {
        // 전체 점수가 높을수록 개별 문제도 맞을 확률이 높음
        double correctProbability = Math.max(0.1, Math.min(0.9, totalScore / 100.0));
        return random.nextDouble() < correctProbability;
    }
    
    /**
     * 정답 여부에 따른 문제별 점수 생성
     */
    private int generateQuestionScore(boolean isCorrect, int maxScore) {
        if (isCorrect) {
            // 정답인 경우 만점 또는 거의 만점
            return random.nextDouble() < 0.8 ? maxScore : Math.max(1, maxScore - 1);
        } else {
            // 오답인 경우 0점 또는 부분점수
            double partialScoreChance = 0.3; // 30% 확률로 부분점수
            if (random.nextDouble() < partialScoreChance) {
                return ThreadLocalRandom.current().nextInt(1, Math.max(2, maxScore / 2));
            } else {
                return 0;
            }
        }
    }
    
    /**
     * AI 신뢰도 생성 (0.6 ~ 1.0 범위)
     */
    private BigDecimal generateAiConfidence() {
        double confidence = 0.6 + (random.nextDouble() * 0.4); // 0.6 ~ 1.0
        return BigDecimal.valueOf(confidence).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 문제별 채점 코멘트 생성
     */
    private String generateQuestionComment(boolean isCorrect, int score, int maxScore) {
        if (isCorrect && score == maxScore) {
            return "정답입니다. 완벽한 풀이입니다.";
        } else if (isCorrect && score < maxScore) {
            return "정답이지만 풀이 과정에서 소소한 실수가 있습니다.";
        } else if (score > 0) {
            return "부분점수를 받았습니다. 접근 방법은 좋으나 계산 실수가 있습니다.";
        } else {
            return "오답입니다. 문제를 다시 검토해보세요.";
        }
    }

}