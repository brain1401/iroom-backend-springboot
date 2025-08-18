package com.iroomclass.springbackend.domain.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.iroomclass.springbackend.domain.system.dto.EchoDto;
import com.iroomclass.springbackend.domain.system.dto.GreetingDto;
import com.iroomclass.springbackend.domain.system.dto.SystemHealthDto;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 시스템 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

    private final DataSource dataSource;
    private final RestTemplate restTemplate;

    private static final String AI_SERVER_URL = "http://localhost:8000/health";

    /**
     * 데이터베이스 헬스체크 수행
     * 
     * @return 데이터베이스 헬스체크 결과
     */
    private SystemHealthDto.ServiceHealthDto checkDatabaseHealth() {
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection()) {
            // 간단한 쿼리로 데이터베이스 연결 확인
            if (connection.isValid(5)) { // 5초 타임아웃
                long responseTime = System.currentTimeMillis() - startTime;
                return SystemHealthDto.ServiceHealthDto.up("데이터베이스 연결 정상", responseTime);
            } else {
                return SystemHealthDto.ServiceHealthDto.down("데이터베이스 연결 실패");
            }
        } catch (Exception e) {
            log.error("데이터베이스 헬스체크 실패: {}", e.getMessage());
            return SystemHealthDto.ServiceHealthDto.down("데이터베이스 연결 오류: " + e.getMessage());
        }
    }

    /**
     * AI 서버 헬스체크 수행
     * 
     * @return AI 서버 헬스체크 결과
     */
    private SystemHealthDto.ServiceHealthDto checkAiServerHealth() {
        long startTime = System.currentTimeMillis();
        
        try {
            restTemplate.getForObject(AI_SERVER_URL, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            return SystemHealthDto.ServiceHealthDto.up("AI 서버 연결 정상", responseTime);
        } catch (Exception e) {
            log.error("AI 서버 헬스체크 실패: {}", e.getMessage());
            return SystemHealthDto.ServiceHealthDto.down("AI 서버 연결 오류: " + e.getMessage());
        }
    }

    /**
     * 시스템 헬스체크 수행
     * 
     * @return 헬스체크 결과
     */
    public SystemHealthDto checkHealth() {
        Map<String, SystemHealthDto.ServiceHealthDto> services = new HashMap<>();
        
        // 애플리케이션 자체 상태 (항상 UP)
        services.put("application", SystemHealthDto.ServiceHealthDto.up("Spring Boot 애플리케이션 정상", 0L));
        
        // 데이터베이스 헬스체크
        SystemHealthDto.ServiceHealthDto dbHealth = checkDatabaseHealth();
        services.put("database", dbHealth);
        
        // AI 서버 헬스체크
        SystemHealthDto.ServiceHealthDto aiHealth = checkAiServerHealth();
        services.put("aiServer", aiHealth);
        
        // 전체 상태 판단 (하나라도 DOWN이면 전체 DOWN)
        boolean isAllUp = services.values().stream()
                .allMatch(service -> "UP".equals(service.status()));
        
        if (isAllUp) {
            return SystemHealthDto.up("모든 서비스가 정상적으로 작동중입니다", services);
        } else {
            return SystemHealthDto.down("일부 서비스에 문제가 있습니다", services);
        }
    }

    /**
     * 인사 메시지 생성
     * 
     * @param name 이름
     * @return 인사 메시지 DTO
     */
    public GreetingDto generateGreeting(String name) {
        return new GreetingDto(name, "안녕하세요, " + name + "님!");
    }

    /**
     * 에코 메시지 생성
     * 
     * @param originalMessage 원본 메시지
     * @return 에코 메시지 DTO
     */
    public EchoDto generateEcho(String originalMessage) {
        String echoMessage = "Echo: " + originalMessage;
        return new EchoDto(originalMessage, echoMessage, LocalDateTime.now());
    }
}
