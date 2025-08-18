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
                        new Server().url("http://localhost:3055/api").description("개발 서버"),
                        new Server().url("https://api.example.com/api").description("운영 서버")))
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
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                if (operation.getResponses() != null && !operation.getResponses().containsKey("500")) {
                    operation.getResponses().addApiResponse("500", new ApiResponse().description("서버 오류"));
                }
            }));
        };
    }

    /**
     * API 기본 정보 설정
     * 
     * @return Info 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("Spring Backend API")
                .description("Spring Boot 3.x 기반 백엔드 API 문서")
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