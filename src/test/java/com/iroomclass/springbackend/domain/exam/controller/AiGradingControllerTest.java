package com.iroomclass.springbackend.domain.exam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.config.AiServerConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * AI 채점 컨트롤러 통합 테스트
 */
@WebFluxTest(AiGradingController.class)
@TestPropertySource(properties = {
    "ai.server.base-url=http://localhost:8080",
    "ai.server.connect-timeout=30",
    "ai.server.response-timeout=60"
})
@DisplayName("AI 채점 컨트롤러 테스트")
class AiGradingControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private AiServerConfig aiServerConfig;
    
    @MockBean
    private WebClient aiServerWebClient;
    
    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();
        
        // AiServerConfig Mock 설정
        when(aiServerConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiServerConfig.getGradingPath()).thenReturn("/grading");
        when(aiServerConfig.getResponseTimeout()).thenReturn(60);
        when(aiServerConfig.getConnectTimeout()).thenReturn(30);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    @DisplayName("단일 제출물 채점 요청 API 테스트")
    void testGradeSubmission() throws Exception {
        // Given
        String submissionId = "12345";
        Map<String, Object> gradingRequest = Map.of(
                "exam_id", "exam_001",
                "answer_sheet_url", "https://example.com/answer_sheet.jpg",
                "answer_key", List.of(
                    Map.of("question", 1, "correct_answer", "A"),
                    Map.of("question", 2, "correct_answer", "B")
                )
        );
        
        Map<String, Object> expectedResponse = Map.of(
                "submission_id", submissionId,
                "status", "processing",
                "message", "채점 요청이 접수되었습니다"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.post()
                .uri("/api/ai/grading/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(gradingRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.submission_id").isEqualTo(submissionId)
                .jsonPath("$.status").isEqualTo("processing");
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/grading/" + submissionId);
        assertThat(recordedRequest.getHeader("Content-Type")).contains("application/json");
    }
    
    @Test
    @DisplayName("단일 제출물 채점 결과 조회 API 테스트")
    void testGetGradingResult() throws Exception {
        // Given
        String submissionId = "12345";
        Map<String, Object> mockGradingResult = Map.of(
                "submission_id", submissionId,
                "status", "completed",
                "score", 85.5,
                "total_questions", 20,
                "correct_answers", 17,
                "results", List.of(
                    Map.of("question", 1, "student_answer", "A", "correct_answer", "A", "is_correct", true),
                    Map.of("question", 2, "student_answer", "C", "correct_answer", "B", "is_correct", false)
                ),
                "processed_at", "2024-08-17T10:30:00Z"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockGradingResult))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/grading/" + submissionId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.submission_id").isEqualTo(submissionId)
                .jsonPath("$.status").isEqualTo("completed")
                .jsonPath("$.score").isEqualTo(85.5)
                .jsonPath("$.total_questions").isEqualTo(20);
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/grading/" + submissionId);
    }
    
    @Test
    @DisplayName("배치 채점 요청 API 테스트")
    void testGradeBatch() throws Exception {
        // Given
        Map<String, Object> batchRequest = Map.of(
                "exam_id", "exam_001",
                "submissions", List.of(
                    Map.of("submission_id", "sub_001", "answer_sheet_url", "https://example.com/sheet1.jpg"),
                    Map.of("submission_id", "sub_002", "answer_sheet_url", "https://example.com/sheet2.jpg")
                ),
                "answer_key", List.of(
                    Map.of("question", 1, "correct_answer", "A"),
                    Map.of("question", 2, "correct_answer", "B")
                )
        );
        
        Map<String, Object> expectedResponse = Map.of(
                "batch_id", "batch_123",
                "status", "processing",
                "total_submissions", 2,
                "message", "배치 채점 요청이 접수되었습니다"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.post()
                .uri("/api/ai/grading/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(batchRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.batch_id").isEqualTo("batch_123")
                .jsonPath("$.status").isEqualTo("processing")
                .jsonPath("$.total_submissions").isEqualTo(2);
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/grading/batch");
    }
    
    @Test
    @DisplayName("채점 통계 조회 API 테스트")
    void testGetGradingStats() throws Exception {
        // Given
        Map<String, Object> mockStats = Map.of(
                "total_processed", 1500,
                "success_rate", 0.97,
                "average_score", 78.5,
                "processing_time", Map.of(
                        "average", 2.3,
                        "min", 0.8,
                        "max", 15.2
                ),
                "status_distribution", Map.of(
                        "completed", 1455,
                        "processing", 25,
                        "failed", 20
                )
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockStats))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/grading/stats")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.total_processed").isEqualTo(1500)
                .jsonPath("$.success_rate").isEqualTo(0.97)
                .jsonPath("$.average_score").isEqualTo(78.5);
    }
    
    @Test
    @DisplayName("채점 상태별 제출물 조회 API 테스트")
    void testGetSubmissionsByStatus() throws Exception {
        // Given
        List<Map<String, Object>> mockSubmissions = List.of(
                Map.of(
                        "submission_id", "sub_001",
                        "status", "completed",
                        "score", 85.0,
                        "processed_at", "2024-08-17T10:30:00Z"
                ),
                Map.of(
                        "submission_id", "sub_002",
                        "status", "completed",
                        "score", 92.5,
                        "processed_at", "2024-08-17T10:35:00Z"
                )
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockSubmissions))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/grading/submissions")
                        .queryParam("status", "completed")
                        .queryParam("limit", 50)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Map.class)
                .hasSize(2);
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).contains("/grading/submissions");
        assertThat(recordedRequest.getPath()).contains("status=completed");
        assertThat(recordedRequest.getPath()).contains("limit=50");
    }
    
    @Test
    @DisplayName("채점 큐 상태 조회 API 테스트")
    void testGetQueueStatus() throws Exception {
        // Given
        Map<String, Object> mockQueueStatus = Map.of(
                "queue_length", 15,
                "processing_count", 3,
                "average_wait_time", 45.2,
                "estimated_completion", "2024-08-17T11:15:00Z",
                "worker_status", List.of(
                        Map.of("worker_id", "worker_1", "status", "busy", "current_task", "sub_123"),
                        Map.of("worker_id", "worker_2", "status", "idle", "current_task", null)
                )
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockQueueStatus))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/grading/queue/status")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.queue_length").isEqualTo(15)
                .jsonPath("$.processing_count").isEqualTo(3)
                .jsonPath("$.average_wait_time").isEqualTo(45.2);
    }
    
    @Test
    @DisplayName("AI 서버 오류 시 적절한 에러 응답 테스트")
    void testAiServerError() throws Exception {
        // Given
        String submissionId = "12345";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Submission not found"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/grading/" + submissionId)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    @DisplayName("잘못된 채점 요청 시 400 에러 테스트")
    void testBadGradingRequest() throws Exception {
        // Given
        String submissionId = "12345";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad Request: Missing required field"));
        
        Map<String, Object> invalidRequest = Map.of(
                "invalid_field", "invalid_value"
        );
        
        // When & Then
        webTestClient.post()
                .uri("/api/ai/grading/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidRequest))
                .exchange()
                .expectStatus().isBadRequest();
    }
}