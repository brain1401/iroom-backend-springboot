package com.iroomclass.springbackend.domain.auth.repository;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 학생 레포지토리
 * 
 * <p>
 * 학생 엔티티에 대한 데이터 접근을 담당하는 레포지토리입니다.
 * 학생 인증을 위한 3-factor 조회 메서드를 제공합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * 이름과 전화번호로 학생 조회
     * 
     * @param name 학생 이름
     * @param phone 학생 전화번호
     * @return 조건에 일치하는 학생 (Optional)
     */
    Optional<Student> findByNameAndPhone(String name, String phone);
    
    /**
     * 이름, 전화번호, 생년월일로 학생 조회
     * 
     * <p>
     * 학생 인증을 위한 3-factor 검증에 사용됩니다.
     * 세 개의 조건이 모두 일치하는 학생을 조회합니다.
     * </p>
     * 
     * @param name 학생 이름
     * @param phone 학생 전화번호
     * @param birthDate 학생 생년월일
     * @return 조건에 일치하는 학생 (Optional)
     */
    Optional<Student> findByNameAndPhoneAndBirthDate(String name, String phone, LocalDate birthDate);
    
    // REMOVED: Repository default methods moved to Service layer to fix transaction boundary issues
    // - findOrCreateStudent() -> moved to StudentAuthService.findOrCreateStudent()
    // - upsertStudent() -> handled by StudentAuthService.upsertStudent()
    
    /**
     * 특정 학년의 시험에 제출한 적이 있는 모든 학생 조회
     * 
     * <p>
     * 해당 학년의 시험을 한 번이라도 제출한 적이 있는 모든 학생을 조회합니다.
     * 시험 제출자 현황 분석에서 해당 학년의 "전체 학생 수"를 파악하기 위해 사용됩니다.
     * </p>
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년에서 시험을 제출한 적이 있는 학생 목록
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN ExamSubmission es ON s.id = es.student.id " + 
           "JOIN es.exam e " +
           "WHERE e.grade = :grade " +
           "ORDER BY s.name")
    List<Student> findStudentsByGrade(@Param("grade") Integer grade);
    
    /**
     * 특정 시험에 제출하지 않은 학생들 조회 (해당 학년 기준)
     * 
     * <p>
     * 해당 학년에서 시험을 제출한 적이 있지만, 특정 시험에는 제출하지 않은 학생들을 조회합니다.
     * </p>
     * 
     * @param grade 학년 (1, 2, 3)
     * @param examId 특정 시험 ID
     * @return 해당 시험에 미제출한 학생 목록
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN ExamSubmission es ON s.id = es.student.id " +
           "JOIN es.exam e " +
           "WHERE e.grade = :grade " +
           "AND s.id NOT IN (" +
           "  SELECT es2.student.id FROM ExamSubmission es2 " +
           "  WHERE es2.exam.id = :examId" +
           ") " +
           "ORDER BY s.name")
    List<Student> findStudentsNotSubmittedToExam(@Param("grade") Integer grade, @Param("examId") java.util.UUID examId);
}