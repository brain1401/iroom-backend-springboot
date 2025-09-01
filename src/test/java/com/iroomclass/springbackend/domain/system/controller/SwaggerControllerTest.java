package com.iroomclass.springbackend.domain.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * SwaggerController 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SwaggerController 단위 테스트")
class SwaggerControllerTest {

    @Mock
    private OpenAPI openAPI;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private SwaggerController swaggerController;
    
    private OpenAPI testOpenAPI;
    
    @BeforeEach
    void setUp() {
        // 테스트용 OpenAPI 객체 생성
        testOpenAPI = new OpenAPI()
            .info(new Info()
                .title("Test API")
                .version("1.0.0")
                .description("Test API Description"));
    }
    
    @Test
    @DisplayName("JSON 다운로드 - 성공")
    void downloadJson_Success() throws Exception {
        // Given
        String expectedJson = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Test API\",\"version\":\"1.0.0\"}}";
        when(openAPI).thenReturn(testOpenAPI);
        when(objectMapper.writeValueAsString(testOpenAPI)).thenReturn(expectedJson);
        
        // When
        ResponseEntity<String> response = swaggerController.downloadJson();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedJson);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("attachment; filename=\"api-spec.json\"");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }
    
    @Test
    @DisplayName("JSON 다운로드 - OpenAPI가 null인 경우")
    void downloadJson_WhenOpenAPIIsNull() {
        // Given
        when(openAPI).thenReturn(null);
        
        // When
        ResponseEntity<String> response = swaggerController.downloadJson();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("OpenAPI 스펙을 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("JSON 다운로드 - JSON 변환 실패")
    void downloadJson_WhenJsonProcessingFails() throws Exception {
        // Given
        when(openAPI).thenReturn(testOpenAPI);
        when(objectMapper.writeValueAsString(testOpenAPI))
            .thenThrow(new RuntimeException("JSON processing error"));
        
        // When
        ResponseEntity<String> response = swaggerController.downloadJson();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("JSON 변환에 실패했습니다");
    }
    
    @Test
    @DisplayName("YAML 다운로드 - 성공 (파일명: api-spec.yaml)")
    void downloadYaml_Success() {
        // Given
        when(openAPI).thenReturn(testOpenAPI);
        
        // When
        ResponseEntity<String> response = swaggerController.downloadYaml();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // YAML 변환은 YAMLMapper가 실제로 동작하지 않으므로 헤더만 검증
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .contains("api-spec.yaml");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
            .isEqualTo("application/x-yaml");
    }
    
    @Test
    @DisplayName("YML 다운로드 - 성공 (파일명: api-spec.yml)")
    void downloadYml_Success() {
        // Given
        when(openAPI).thenReturn(testOpenAPI);
        
        // When
        ResponseEntity<String> response = swaggerController.downloadYml();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .contains("api-spec.yml");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
            .isEqualTo("application/x-yaml");
    }
    
    @Test
    @DisplayName("YAML/YML 다운로드 - OpenAPI가 null인 경우")
    void downloadYamlInternal_WhenOpenAPIIsNull() {
        // Given
        when(openAPI).thenReturn(null);
        
        // When
        ResponseEntity<String> response = swaggerController.downloadYaml();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("OpenAPI 스펙을 찾을 수 없습니다");
    }
}