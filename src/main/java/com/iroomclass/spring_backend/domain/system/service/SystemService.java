package com.iroomclass.spring_backend.domain.system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.iroomclass.spring_backend.domain.system.dto.EchoDto;
import com.iroomclass.spring_backend.domain.system.dto.GreetingDto;
import com.iroomclass.spring_backend.domain.system.dto.SystemHealthDto;

import java.time.LocalDateTime;

/**
 * 시스템 관련 서비스
 */
@Service
@RequiredArgsConstructor
public class SystemService {

    /**
     * 시스템 헬스체크 수행
     * 
     * @return 헬스체크 결과
     */
    public SystemHealthDto checkHealth() {
        // 실제로는 여기서 데이터베이스 연결, 외부 서비스 상태 등을 확인할 수 있음
        return SystemHealthDto.up("서버가 정상적으로 작동중입니다");
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
