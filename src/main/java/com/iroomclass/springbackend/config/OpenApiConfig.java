package com.iroomclass.springbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 설정
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 정보 설정
     * 
     * @return OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:3055/api").description("로컬 개발 서버"),
                        new Server().url("https://dev-api.iroomclass.com/api").description("개발 서버"),
                        new Server().url("https://api.iroomclass.com/api").description("운영 서버")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 전역 공통 응답 커스터마이저 등록
     */
    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> {
            // 공통 응답 스키마 정의
            Schema<?> errorResponseSchema = new Schema<>()
                    .$ref("#/components/schemas/ErrorResponse");
            
            Schema<?> successResponseSchema = new Schema<>()
                    .$ref("#/components/schemas/SuccessResponse");
                    
            // 공통 에러 응답 Content 정의
            Content errorContent = new Content()
                    .addMediaType("application/json", new MediaType()
                            .schema(errorResponseSchema)
                            .addExamples("serverError", new Example()
                                    .summary("서버 내부 오류")
                                    .value("""
                                            {
                                              "result": "ERROR",
                                              "message": "서버 내부 오류가 발생했습니다",
                                              "data": null
                                            }
                                            """)
                            ));
            
            openApi.getPaths().values().forEach(pathItem -> 
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses() != null) {
                        // 500 에러 응답 추가 (기존에 없는 경우만)
                        if (!operation.getResponses().containsKey("500")) {
                            operation.getResponses().addApiResponse("500", 
                                new ApiResponse()
                                    .description("서버 내부 오류")
                                    .content(errorContent)
                            );
                        }
                    }
                })
            );
        };
    }

    /**
     * API 기본 정보 설정
     * 
     * @return Info 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("이룸클래스 관리 시스템 API")
                .description("""
                    이룸클래스 학원 관리 시스템의 백엔드 API입니다.
                    
                    주요 기능:
                    - 학생/교사 사용자 관리
                    - 시험지 생성 및 관리  
                    - AI 기반 답안지 인식 및 자동 채점
                    - 성적 분석 및 통계
                    - 학습 단원 관리
                    
                    기술 스택: Spring Boot 3.5.4, Java 21, MySQL, Redis, AWS S3
                    """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("개발팀")
                        .email("dev@iroomclass.com")
                        .url("https://github.com/iroomclass"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}