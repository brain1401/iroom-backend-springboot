package com.iroomclass.springbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정
 * 
 * URL 레벨 보안만 사용하여 단순하고 명확한 보안 정책 적용.
 * @PreAuthorize 등 메서드 레벨 보안은 제거하여 코드 복잡성 감소.
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
                        
                        // Swagger UI 관련 경로 허용 (개발/테스트용)
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

                        // 시스템 헬스체크는 공개 허용 (모니터링용)
                        .requestMatchers("/api/system/health", "/system/health").permitAll()
                        
                        // 단원 및 문제 조회는 공개 허용 (학습용)
                        .requestMatchers(HttpMethod.GET, "/api/unit/**", "/unit/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/question/**", "/question/**").permitAll()
                        
                        // 학생 로그인은 공개 허용
                        .requestMatchers("/api/user/login", "/user/login").permitAll()
                        
                        // 관리자 인증은 공개 허용
                        .requestMatchers("/api/admin/verify-credentials", "/admin/verify-credentials").permitAll()
                        
                        // 사용자 영역 - 현재는 로그인 로직과 분리되어 있음 (추후 통합 필요)
                        .requestMatchers("/api/user/**", "/user/**").permitAll()
                        
                        // 관리자 영역 - 현재는 로그인 로직과 분리되어 있음 (추후 통합 필요)
                        .requestMatchers("/api/admin/**", "/admin/**").permitAll()

                        // 나머지 모든 요청은 허용 (개발 단계에서 단순화)
                        .anyRequest().permitAll())
                
                // HTTP Basic 인증 활성화 (임시, JWT 구현 전까지)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // 추가해야 할 Bean 메서드 (클래스 맨 아래에 추가)
    /**
     * 비밀번호 암호화 도구
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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