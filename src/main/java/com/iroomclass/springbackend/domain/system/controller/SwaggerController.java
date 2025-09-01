package com.iroomclass.springbackend.domain.system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Swagger 문서 다운로드 컨트롤러
 * 
 * <p>OpenAPI 스펙 문서를 다양한 형식(JSON, YAML, YML)으로 다운로드할 수 있는 기능을 제공합니다.</p>
 */
@RestController
@RequestMapping("/swagger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Swagger 다운로드 API", description = "OpenAPI 스펙 문서 다운로드 API")
public class SwaggerController {

    private final OpenAPI openAPI;
    private final ObjectMapper objectMapper;
    
    /**
     * OpenAPI 스펙을 JSON 형식으로 다운로드
     * 
     * @return JSON 형식의 OpenAPI 스펙 문서
     */
    @Operation(
        summary = "JSON 형식 OpenAPI 스펙 다운로드",
        description = """
            OpenAPI 스펙 문서를 JSON 형식으로 다운로드합니다.
            
            다운로드되는 파일:
            - 파일명: api-spec.json
            - 형식: JSON
            - 내용: 전체 OpenAPI 3.x 스펙
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "JSON 파일 다운로드 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object", description = "OpenAPI 3.x 스펙")
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    @GetMapping(value = "/download/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> downloadJson() {
        try {
            log.info("OpenAPI 스펙 JSON 다운로드 요청");
            
            if (openAPI == null) {
                log.error("OpenAPI 객체가 null입니다");
                return ResponseEntity.internalServerError()
                    .body("{\"error\": \"OpenAPI 스펙을 찾을 수 없습니다\"}");
            }
            
            String jsonContent = objectMapper.writeValueAsString(openAPI);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"api-spec.json\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            
            log.info("OpenAPI 스펙 JSON 다운로드 성공");
            return ResponseEntity.ok()
                .headers(headers)
                .body(jsonContent);
                
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body("{\"error\": \"JSON 변환에 실패했습니다: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body("{\"error\": \"서버 내부 오류가 발생했습니다\"}");
        }
    }

    /**
     * OpenAPI 스펙을 YAML 형식으로 다운로드
     * 
     * @return YAML 형식의 OpenAPI 스펙 문서
     */
    @Operation(
        summary = "YAML 형식 OpenAPI 스펙 다운로드",
        description = """
            OpenAPI 스펙 문서를 YAML 형식으로 다운로드합니다.
            
            다운로드되는 파일:
            - 파일명: api-spec.yaml
            - 형식: YAML
            - 내용: 전체 OpenAPI 3.x 스펙
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "YAML 파일 다운로드 성공",
            content = @Content(
                mediaType = "application/x-yaml",
                schema = @Schema(type = "string", description = "OpenAPI 3.x 스펙 (YAML 형식)")
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    @GetMapping(value = "/download/yaml", produces = "application/x-yaml")
    public ResponseEntity<String> downloadYaml() {
        return downloadYamlInternal("api-spec.yaml");
    }

    /**
     * OpenAPI 스펙을 YML 형식으로 다운로드
     * 
     * @return YML 형식의 OpenAPI 스펙 문서 (YAML과 동일)
     */
    @Operation(
        summary = "YML 형식 OpenAPI 스펙 다운로드",
        description = """
            OpenAPI 스펙 문서를 YML 형식으로 다운로드합니다.
            
            다운로드되는 파일:
            - 파일명: api-spec.yml
            - 형식: YML (YAML과 동일)
            - 내용: 전체 OpenAPI 3.x 스펙
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "YML 파일 다운로드 성공",
            content = @Content(
                mediaType = "application/x-yaml",
                schema = @Schema(type = "string", description = "OpenAPI 3.x 스펙 (YML 형식)")
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    @GetMapping(value = "/download/yml", produces = "application/x-yaml")
    public ResponseEntity<String> downloadYml() {
        return downloadYamlInternal("api-spec.yml");
    }

    /**
     * YAML/YML 다운로드 내부 구현
     * 
     * @param filename 다운로드할 파일명
     * @return YAML 형식의 OpenAPI 스펙 문서
     */
    private ResponseEntity<String> downloadYamlInternal(String filename) {
        try {
            log.info("OpenAPI 스펙 YAML 다운로드 요청: {}", filename);
            
            if (openAPI == null) {
                log.error("OpenAPI 객체가 null입니다");
                return ResponseEntity.internalServerError()
                    .body("error: OpenAPI 스펙을 찾을 수 없습니다");
            }
            
            // YAMLMapper를 사용해서 YAML 형식으로 변환
            YAMLMapper yamlMapper = new YAMLMapper();
            String yamlContent = yamlMapper.writeValueAsString(openAPI);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/x-yaml");
            
            log.info("OpenAPI 스펙 YAML 다운로드 성공: {}", filename);
            return ResponseEntity.ok()
                .headers(headers)
                .body(yamlContent);
                
        } catch (JsonProcessingException e) {
            log.error("YAML 변환 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body("error: YAML 변환에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body("error: 서버 내부 오류가 발생했습니다");
        }
    }
}