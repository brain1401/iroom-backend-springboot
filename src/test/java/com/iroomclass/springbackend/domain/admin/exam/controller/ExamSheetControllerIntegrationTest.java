package com.iroomclass.springbackend.domain.admin.exam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionReplaceRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionSelectionRequest;
import com.iroomclass.springbackend.common.UUIDv7Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * ExamSheetController 통합 테스트
 * 
 * 시험지 관리 API의 전체 엔드포인트를 테스트합니다.
 * 실제 데이터베이스와 연동하여 비즈니스 로직을 검증합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ExamSheetController 통합 테스트")
@Transactional
class ExamSheetControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final String BASE_URL = "/admin/exam-sheets";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("시험지 생성 테스트")
    class CreateExamSheetTest {

        @Test
        @DisplayName("정상적인 시험지 생성 - 성공")
        void createExamSheet_Success() throws Exception {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "통합테스트 시험지",
                    1, // 1학년
                    20, // 총 문제 수
                    15, // 객관식 문제 수
                    5,  // 주관식 문제 수
                    List.of(UUIDv7Generator.generate(), UUIDv7Generator.generate()) // 단원 ID 목록
            );

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheetId").exists())
                    .andExpect(jsonPath("$.data.examName").value("통합테스트 시험지"))
                    .andExpect(jsonPath("$.data.grade").value(1))
                    .andExpect(jsonPath("$.data.totalQuestions").value(20));
        }

        @Test
        @DisplayName("잘못된 학년으로 시험지 생성 - 실패")
        void createExamSheet_InvalidGrade_Fail() throws Exception {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "잘못된 학년 시험지",
                    5, // 잘못된 학년 (1-3만 허용)
                    10,
                    5,
                    5,
                    List.of(UUIDv7Generator.generate())
            );

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("빈 단원 목록으로 시험지 생성 - 실패")
        void createExamSheet_EmptyUnits_Fail() throws Exception {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "빈 단원 시험지",
                    1,
                    10,
                    5,
                    5,
                    List.of() // 빈 단원 목록
            );

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("시험지 목록 조회 테스트")
    class GetExamSheetsTest {

        @Test
        @DisplayName("전체 시험지 목록 조회 - 성공")
        void getAllExamSheets_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/all")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheets").isArray())
                    .andExpect(jsonPath("$.data.totalCount").exists());
        }

        @Test
        @DisplayName("학년별 시험지 목록 조회 - 성공")
        void getExamSheetsByGrade_Success() throws Exception {
            int testGrade = 2;

            mockMvc.perform(get(BASE_URL + "/grade/{grade}", testGrade)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheets").isArray())
                    .andExpect(jsonPath("$.data.totalCount").exists());
        }

        @Test
        @DisplayName("잘못된 학년으로 목록 조회 - 실패")
        void getExamSheetsByGrade_InvalidGrade_Fail() throws Exception {
            int invalidGrade = 5;

            mockMvc.perform(get(BASE_URL + "/grade/{grade}", invalidGrade)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }

        @Test
        @DisplayName("필터링된 시험지 목록 조회 - 성공")
        void getFilteredExamSheets_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search")
                            .param("grade", "1")
                            .param("minQuestions", "10")
                            .param("maxQuestions", "30")
                            .param("examNameKeyword", "테스트")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "desc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("시험지 필터링 조회 성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheets").isArray())
                    .andExpect(jsonPath("$.data.totalCount").exists());
        }

        @Test
        @DisplayName("날짜 범위 필터링 시험지 조회 - 성공")
        void getFilteredExamSheets_DateRange_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search")
                            .param("createdAfter", "2024-01-01")
                            .param("createdBefore", "2024-12-31")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("시험지 상세 조회 테스트")
    class GetExamSheetDetailTest {

        @Test
        @DisplayName("존재하는 시험지 상세 조회 - 성공")
        void getExamSheetDetail_Success() throws Exception {
            // 테스트용 UUID 생성 (일반적으로 존재할 것으로 예상되는 ID)
            UUID examSheetId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheetId").value(examSheetId))
                    .andExpect(jsonPath("$.data.examName").exists())
                    .andExpect(jsonPath("$.data.grade").exists())
                    .andExpect(jsonPath("$.data.units").isArray())
                    .andExpect(jsonPath("$.data.questions").isArray());
        }

        @Test
        @DisplayName("존재하지 않는 시험지 상세 조회 - 실패")
        void getExamSheetDetail_NotFound_Fail() throws Exception {
            UUID nonExistentId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.result").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("시험지 미리보기 테스트")
    class GetExamSheetPreviewTest {

        @Test
        @DisplayName("시험지 미리보기 조회 - 성공")
        void getExamSheetPreview_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}/preview", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("시험지 미리보기 조회 성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheetId").value(examSheetId))
                    .andExpect(jsonPath("$.data.examSheetName").exists())
                    .andExpect(jsonPath("$.data.grade").exists())
                    .andExpect(jsonPath("$.data.totalQuestions").exists())
                    .andExpect(jsonPath("$.data.multipleChoiceCount").exists())
                    .andExpect(jsonPath("$.data.subjectiveCount").exists())
                    .andExpect(jsonPath("$.data.totalPoints").exists())
                    .andExpect(jsonPath("$.data.questions").isArray())
                    .andExpect(jsonPath("$.data.statistics").exists());
        }

        @Test
        @DisplayName("존재하지 않는 시험지 미리보기 - 실패")
        void getExamSheetPreview_NotFound_Fail() throws Exception {
            UUID nonExistentId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}/preview", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("문제 선택 및 관리 테스트")
    class QuestionManagementTest {

        @Test
        @DisplayName("선택 가능한 문제 목록 조회 - 성공")
        void getSelectableQuestions_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}/selectable-questions", examSheetId)
                            .param("unitId", "1")
                            .param("difficulty", "중")
                            .param("questionType", "MULTIPLE_CHOICE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("선택 가능한 문제 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.questions").isArray())
                    .andExpect(jsonPath("$.data.totalCount").exists());
        }

        @Test
        @DisplayName("시험지 문제 관리 현황 조회 - 성공")
        void getExamSheetQuestionManagement_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();

            mockMvc.perform(get(BASE_URL + "/{examSheetId}/question-management", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("시험지 문제 관리 현황 조회 성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheetId").value(examSheetId))
                    .andExpect(jsonPath("$.data.currentQuestionCount").exists())
                    .andExpect(jsonPath("$.data.targetQuestionCount").exists())
                    .andExpect(jsonPath("$.data.multipleChoiceCount").exists())
                    .andExpect(jsonPath("$.data.subjectiveCount").exists())
                    .andExpect(jsonPath("$.data.totalPoints").exists())
                    .andExpect(jsonPath("$.data.questions").isArray());
        }
    }

    @Nested
    @DisplayName("문제 추가/제거 테스트")
    class AddRemoveQuestionTest {

        @Test
        @DisplayName("시험지에 문제 추가 - 성공")
        void addQuestionToExamSheet_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    UUIDv7Generator.generate(), // 문제 ID
                    5,  // 배점
                    1   // 순서
            );

            mockMvc.perform(post(BASE_URL + "/{examSheetId}/questions", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("문제가 시험지에 추가되었습니다"))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.examSheetId").value(examSheetId));
        }

        @Test
        @DisplayName("시험지에서 문제 제거 - 성공")
        void removeQuestionFromExamSheet_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            UUID questionId = UUIDv7Generator.generate();

            mockMvc.perform(delete(BASE_URL + "/{examSheetId}/questions/{questionId}",
                            examSheetId, questionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("문제가 시험지에서 제거되었습니다"))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("존재하지 않는 문제 제거 - 실패")
        void removeNonExistentQuestion_Fail() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            UUID nonExistentQuestionId = UUIDv7Generator.generate();

            mockMvc.perform(delete(BASE_URL + "/{examSheetId}/questions/{questionId}",
                            examSheetId, nonExistentQuestionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("문제 교체 테스트")
    class ReplaceQuestionTest {

        @Test
        @DisplayName("시험지 문제 교체 - 성공")
        void replaceQuestionInExamSheet_Success() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            QuestionReplaceRequest request = new QuestionReplaceRequest(
                    UUIDv7Generator.generate(), // 기존 문제 ID
                    UUIDv7Generator.generate(), // 새 문제 ID
                    10, // 새 배점
                    "더 적절한 난이도로 교체" // 교체 사유
            );

            mockMvc.perform(put(BASE_URL + "/{examSheetId}/questions/replace", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("시험지 문제 교체 성공"))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("동일한 문제로 교체 - 실패")
        void replaceSameQuestion_Fail() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            UUID sameQuestionId = UUIDv7Generator.generate();
            QuestionReplaceRequest request = new QuestionReplaceRequest(
                    sameQuestionId, // 기존 문제 ID
                    sameQuestionId, // 동일한 문제 ID로 교체 (금지)
                    5,
                    "동일한 문제"
            );

            mockMvc.perform(put(BASE_URL + "/{examSheetId}/questions/replace", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("입력 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("시험지 생성 - 필수 필드 누락")
        void createExamSheet_MissingRequiredFields_Fail() throws Exception {
            String invalidJson = """
                    {
                        "examName": "",
                        "grade": null,
                        "totalQuestions": -1
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }

        @Test
        @DisplayName("문제 추가 - 잘못된 JSON 형식")
        void addQuestion_InvalidJson_Fail() throws Exception {
            UUID examSheetId = UUIDv7Generator.generate();
            String invalidJson = "{ invalid json }";

            mockMvc.perform(post(BASE_URL + "/{examSheetId}/questions", examSheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    class ErrorHandlingTest {

        @Test
        @DisplayName("존재하지 않는 엔드포인트 - 404")
        void nonExistentEndpoint_NotFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/nonexistent")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 HTTP 메서드 - Method Not Allowed")
        void wrongHttpMethod_MethodNotAllowed() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("잘못된 Content-Type - Unsupported Media Type")
        void wrongContentType_UnsupportedMediaType() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("plain text content"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}