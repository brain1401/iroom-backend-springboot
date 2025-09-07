package com.iroomclass.springbackend.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UUIDv7Generator 테스트 클래스
 */
class UUIDv7GeneratorTest {

    @Test
    @DisplayName("UUIDv7 생성 테스트")
    void generateUUIDv7() {
        // given & when
        UUID uuid = UUIDv7Generator.generate();
        
        // then
        assertThat(uuid).isNotNull();
        assertThat(UUIDv7Generator.isUUIDv7(uuid)).isTrue();
        System.out.println("Generated UUID v7: " + uuid);
    }
    
    @Test
    @DisplayName("UUIDv7 문자열 생성 테스트")
    void generateUUIDv7String() {
        // given & when
        String uuidStr = UUIDv7Generator.generateString();
        
        // then
        assertThat(uuidStr).isNotNull();
        assertThat(uuidStr).hasSize(36); // UUID 표준 문자열 길이
        assertThat(uuidStr).contains("-");
        System.out.println("Generated UUID v7 String: " + uuidStr);
    }
    
    @Test
    @DisplayName("UUIDv7 고유성 테스트")
    void generateUniqueUUIDs() {
        // given
        Set<UUID> uuids = new HashSet<>();
        int count = 1000;
        
        // when
        for (int i = 0; i < count; i++) {
            uuids.add(UUIDv7Generator.generate());
        }
        
        // then
        assertThat(uuids).hasSize(count); // 모든 UUID가 고유해야 함
    }
    
    @Test
    @DisplayName("UUIDv7 시간 순서 테스트")
    void timeOrderedUUIDs() throws InterruptedException {
        // given
        UUID uuid1 = UUIDv7Generator.generate();
        Thread.sleep(10); // 10ms 대기
        UUID uuid2 = UUIDv7Generator.generate();
        
        // when
        long timestamp1 = UUIDv7Generator.extractTimestamp(uuid1);
        long timestamp2 = UUIDv7Generator.extractTimestamp(uuid2);
        
        // then
        assertThat(timestamp2).isGreaterThan(timestamp1);
        System.out.println("UUID1: " + uuid1 + " (timestamp: " + timestamp1 + ")");
        System.out.println("UUID2: " + uuid2 + " (timestamp: " + timestamp2 + ")");
    }
    
    @Test
    @DisplayName("UUID 바이트 변환 테스트")
    void bytesConversion() {
        // given
        UUID original = UUIDv7Generator.generate();
        
        // when
        byte[] bytes = UUIDv7Generator.toBytes(original);
        UUID restored = UUIDv7Generator.fromBytes(bytes);
        
        // then
        assertThat(bytes).hasSize(16); // UUID는 16바이트
        assertThat(restored).isEqualTo(original);
    }
}