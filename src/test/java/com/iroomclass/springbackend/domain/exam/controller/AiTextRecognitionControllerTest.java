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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * AI 텍스트 인식 컨트롤러 통합 테스트
 */
@WebFluxTest(AiTextRecognitionController.class)
@TestPropertySource(properties = {
    "ai.server.base-url=http://localhost:8080",
    "ai.server.connect-timeout=30",
    "ai.server.response-timeout=60"
})
@DisplayName("AI 텍스트 인식 컨트롤러 테스트")
class AiTextRecognitionControllerTest {
    
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
        when(aiServerConfig.getTextRecognitionPath()).thenReturn("/text-recognition");
        when(aiServerConfig.getResponseTimeout()).thenReturn(60);
        when(aiServerConfig.getConnectTimeout()).thenReturn(30);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    @DisplayName("답안지 텍스트 인식 API 테스트")
    void testRecognizeAnswerSheet() throws Exception {
        // Given
        String expectedResponse = """
            {
                "recognized_answers": [
                    {"question_number": 1, "answer": "A", "confidence": 0.95}
                ],
                "metadata": {
                    "processing_time": 1.234,
                    "model_version": "v1.0",
                    "image_quality": "good"
                },
                "message": "인식 성공"
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json"));
        
        MockMultipartFile testFile = new MockMultipartFile(
                "file", 
                "test-answer-sheet.jpg", 
                "image/jpeg", 
                "fake image content".getBytes()
        );
        
        // When & Then
        webTestClient.post()
                .uri("/api/ai/text-recognition/answer-sheet")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", testFile.getResource()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/text-recognition/answer-sheet");
        assertThat(recordedRequest.getHeader("Content-Type")).contains("multipart/form-data");
    }
    
    @Test
    @DisplayName("메트릭 조회 API 테스트")
    void testGetMetrics() throws Exception {
        // Given
        Map<String, Object> mockMetrics = Map.of(
                "total_processed", 1000,
                "success_rate", 0.98,
                "average_processing_time", 1.5,
                "current_load", 0.3
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockMetrics))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/text-recognition/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.total_processed").isEqualTo(1000)
                .jsonPath("$.success_rate").isEqualTo(0.98);
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/text-recognition/metrics");
    }
    
    @Test
    @DisplayName("대시보드 정보 조회 API 테스트")
    void testGetDashboard() throws Exception {
        // Given
        Map<String, Object> mockDashboard = Map.of(
                "active_sessions", 15,
                "queue_length", 3,
                "system_status", "healthy",
                "uptime", "72h 15m"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDashboard))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/text-recognition/dashboard")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.active_sessions").isEqualTo(15)
                .jsonPath("$.system_status").isEqualTo("healthy");
    }
    
    @Test
    @DisplayName("헬스 체크 API 테스트")
    void testCheckHealth() throws Exception {
        // Given
        Map<String, Object> mockHealth = Map.of(
                "status", "UP",
                "timestamp", "2024-08-17T10:30:00Z",
                "services", Map.of(
                        "text_recognition", "UP",
                        "database", "UP"
                )
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockHealth))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/text-recognition/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
    
    @Test
    @DisplayName("캐시 무효화 API 테스트")
    void testInvalidateCache() throws Exception {
        // Given
        Map<String, Object> mockResponse = Map.of(
                "status", "success",
                "message", "Cache invalidated successfully",
                "timestamp", "2024-08-17T10:30:00Z"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .setHeader("Content-Type", "application/json"));
        
        // When & Then
        webTestClient.delete()
                .uri("/api/ai/text-recognition/cache/invalidate")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("success");
        
        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath()).isEqualTo("/text-recognition/cache/invalidate");
    }
    
    @Test
    @DisplayName("AI 서버 오류 시 적절한 에러 응답 테스트")
    void testAiServerError() throws Exception {
        // Given - AI 서버가 500 에러 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/text-recognition/metrics")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    @DisplayName("AI 서버 응답 없음 시 타임아웃 처리 테스트")
    void testAiServerTimeout() throws Exception {
        // Given - AI 서버가 응답하지 않음 (응답 지연)
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setBodyDelay(70, java.util.concurrent.TimeUnit.SECONDS)); // 타임아웃보다 긴 지연
        
        // When & Then
        webTestClient.get()
                .uri("/api/ai/text-recognition/metrics")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}