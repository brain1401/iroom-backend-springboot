package com.iroomclass.springbackend.common;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUIDv7 생성기
 * 
 * <p>
 * JUG(Java UUID Generator) 라이브러리를 사용하여
 * 시간 순서대로 정렬 가능한 UUID v7을 생성합니다.
 * </p>
 * 
 * <p>
 * UUIDv7 구조 (RFC-9562):
 * </p>
 * <ul>
 * <li>48비트: 유닉스 타임스탬프 (밀리초)</li>
 * <li>12비트: 버전 7 + 랜덤</li>
 * <li>62비트: 변형 + 랜덤</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2.0
 */
public class UUIDv7Generator {

    /**
     * JUG의 UUID v7 생성기 인스턴스
     * Thread-safe하므로 싱글톤으로 사용
     */
    private static final TimeBasedEpochRandomGenerator GENERATOR = Generators.timeBasedEpochRandomGenerator();

    /**
     * UUIDv7 생성
     * 
     * @return 새로운 UUIDv7
     */
    public static UUID generate() {
        return GENERATOR.generate();
    }

    /**
     * UUID를 바이너리 배열로 변환
     * 
     * @param uuid 변환할 UUID
     * @return 16바이트 바이너리 배열
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * 바이너리 배열을 UUID로 변환
     * 
     * @param bytes 16바이트 바이너리 배열
     * @return UUID
     */
    public static UUID fromBytes(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("바이트 배열의 길이는 16이어야 합니다");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * UUIDv7에서 타임스탬프 추출
     * 
     * @param uuidv7 UUIDv7 UUID
     * @return 생성 시점의 타임스탬프 (밀리초)
     */
    public static long extractTimestamp(UUID uuidv7) {
        // UUID v7의 상위 48비트가 타임스탬프
        return uuidv7.getMostSignificantBits() >>> 16;
    }

    /**
     * UUIDv7 생성 (문자열 형태)
     * 
     * @return UUIDv7 문자열
     */
    public static String generateString() {
        return generate().toString();
    }

    /**
     * UUIDv7 검증
     * 
     * @param uuid 검증할 UUID
     * @return UUIDv7인지 여부
     */
    public static boolean isUUIDv7(UUID uuid) {
        // UUID 버전 확인 (버전 필드가 7인지)
        // UUID v7의 경우 version 필드(12-15번째 비트)가 0111(7)이어야 함
        int version = (int) ((uuid.getMostSignificantBits() >> 12) & 0x0f);
        return version == 7;
    }

    /**
     * 현재 시간으로부터 상대적 시간차이를 가진 UUIDv7 생성 (테스트용)
     * 
     * <p>
     * 참고: JUG 라이브러리는 커스텀 타임스탬프를 직접 지원하지 않으므로
     * 이 메서드는 일반 UUID v7을 생성하되, 시간 오프셋은 무시됩니다.
     * 실제 테스트에서는 Thread.sleep()을 사용하거나
     * 다른 방법으로 시간차를 만들어야 합니다.
     * </p>
     * 
     * @param offsetMs 현재 시간으로부터의 오프셋 (밀리초) - 무시됨
     * @return 현재 시간 기준의 UUIDv7
     * @deprecated JUG 라이브러리 제약으로 인해 offset이 적용되지 않음
     */
    @Deprecated
    public static UUID generateWithOffset(long offsetMs) {
        // JUG 라이브러리는 현재 시간 기준으로만 생성 가능
        // 커스텀 타임스탬프 지원 안 함
        return generate();
    }
}