package com.iroomclass.spring_backend.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTP 보안 설정
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 설정 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 활성화
                .cors(Customizer.withDefaults())

                // 세션 정책: STATELESS (JWT 사용 시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight 사전요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger UI 관련 경로 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                // context-path 적용 경로 추가 허용
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/api/v3/api-docs/**",
                                "/api/swagger-resources/**",
                                "/api/webjars/**")
                        .permitAll()

                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // 공개 API 경로 (필요 시 추가)
                        .requestMatchers("/api/public/**", "/api/test/**").permitAll()
                        .requestMatchers("/public/**", "/test/**").permitAll()

                        // 시스템 API 공개 허용
                        .requestMatchers("/api/system/**").permitAll()
                        .requestMatchers("/system/**").permitAll()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // localhost 전용 허용
        configuration.addAllowedOriginPattern("http://localhost");
        configuration.addAllowedOriginPattern("https://localhost");
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("https://localhost:*");
        configuration.addAllowedOriginPattern("http://127.0.0.1");
        configuration.addAllowedOriginPattern("https://127.0.0.1");
        configuration.addAllowedOriginPattern("http://127.0.0.1:*");
        configuration.addAllowedOriginPattern("https://127.0.0.1:*");
        configuration.addAllowedOriginPattern("http://[::1]");
        configuration.addAllowedOriginPattern("https://[::1]");
        configuration.addAllowedOriginPattern("http://[::1]:*");
        configuration.addAllowedOriginPattern("https://[::1]:*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}