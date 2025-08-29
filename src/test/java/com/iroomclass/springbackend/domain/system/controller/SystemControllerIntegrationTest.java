package com.iroomclass.springbackend.domain.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.domain.system.dto.EchoRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 시스템 컨트롤러 통합 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("시스템 컨트롤러 통합 테스트")
class SystemControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("헬스체크 엔드포인트 - 성공")
    void healthCheck_Success() throws Exception {
        setUp();
        
        mockMvc.perform(get("/system/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value(anyOf(equalTo("UP"), equalTo("DOWN"))))
                .andExpect(jsonPath("$.data.timestamp").exists())
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    @DisplayName("인사 메시지 엔드포인트 - 기본 파라미터")
    void greeting_WithDefaultParameter_Success() throws Exception {
        setUp();
        
        mockMvc.perform(get("/system/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("인사 메시지 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("World"))
                .andExpect(jsonPath("$.data.message").value("안녕하세요, World님!"));
    }

    @Test
    @DisplayName("인사 메시지 엔드포인트 - 커스텀 이름")
    void greeting_WithCustomName_Success() throws Exception {
        setUp();
        
        String customName = "홍길동";
        
        mockMvc.perform(get("/system/hello")
                .param("name", customName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("인사 메시지 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value(customName))
                .andExpect(jsonPath("$.data.message").value("안녕하세요, " + customName + "님!"));
    }

    @Test
    @DisplayName("에코 엔드포인트 - 정상적인 메시지")
    void echo_WithValidMessage_Success() throws Exception {
        setUp();
        
        String testMessage = "Hello, World!";
        EchoRequestDto request = new EchoRequestDto(testMessage);
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("에코 메시지 처리 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.originalMessage").value(testMessage))
                .andExpect(jsonPath("$.data.echoMessage").value("Echo: " + testMessage))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    @DisplayName("에코 엔드포인트 - 한글 메시지")
    void echo_WithKoreanMessage_Success() throws Exception {
        setUp();
        
        String koreanMessage = "안녕하세요, 한글 테스트입니다!";
        EchoRequestDto request = new EchoRequestDto(koreanMessage);
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.originalMessage").value(koreanMessage))
                .andExpect(jsonPath("$.data.echoMessage").value("Echo: " + koreanMessage));
    }

    @Test
    @DisplayName("에코 엔드포인트 - 빈 메시지로 validation 에러 테스트")
    void echo_WithEmptyMessage_ValidationError() throws Exception {
        setUp();
        
        EchoRequestDto request = new EchoRequestDto("");
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("입력")))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("에코 엔드포인트 - null 메시지로 validation 에러 테스트")
    void echo_WithNullMessage_ValidationError() throws Exception {
        setUp();
        
        String requestBody = "{\"message\": null}";
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("입력")));
    }

    @Test
    @DisplayName("에코 엔드포인트 - 잘못된 JSON 형식")
    void echo_WithInvalidJson_BadRequest() throws Exception {
        setUp();
        
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트 - 404 에러")
    void nonExistentEndpoint_NotFound() throws Exception {
        setUp();
        
        mockMvc.perform(get("/system/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 HTTP 메서드 - Method Not Allowed")
    void wrongHttpMethod_MethodNotAllowed() throws Exception {
        setUp();
        
        mockMvc.perform(post("/system/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("긴 메시지 처리 - 성능 테스트")
    void echo_WithLongMessage_Success() throws Exception {
        setUp();
        
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longMessage.append("Performance test message ");
        }
        
        EchoRequestDto request = new EchoRequestDto(longMessage.toString());
        
        mockMvc.perform(post("/system/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("SUCCESS"));
    }
}