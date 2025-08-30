package com.iroomclass.springbackend.common;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUIDv7 생성기
 * 
 * <p>
 * 시간 순서대로 정렬 가능한 UUID를 생성합니다.
 * MySQL의 GENERATE_UUIDV7() 함수와 동일한 결과를 생성합니다.
 * </p>
 * 
 * <p>
 * UUIDv7 구조:
 * </p>
 * <ul>
 * <li>48비트: 유닉스 타임스탬프 (밀리초)</li>
 * <li>12비트: 버전 7 + 랜덤</li>
 * <li>62비트: 변형 + 랜덤</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 1.0
 */
public class UUIDv7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * UUIDv7 생성
     * 
     * @return 새로운 UUIDv7
     */
    public static UUID generate() {
        long timestampMs = System.currentTimeMillis();

        // 랜덤 바이트 생성
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        // 타임스탬프를 상위 48비트에 배치
        long mostSigBits = (timestampMs << 16) |
                (0x7000L | ((randomBytes[0] & 0x0F) << 8) | (randomBytes[1] & 0xFF));

        // 나머지 64비트 구성
        long leastSigBits = ((long) (0x80 | (randomBytes[2] & 0x3F)) << 56) |
                ((long) (randomBytes[3] & 0xFF) << 48) |
                ((long) (randomBytes[4] & 0xFF) << 40) |
                ((long) (randomBytes[5] & 0xFF) << 32) |
                ((long) (randomBytes[6] & 0xFF) << 24) |
                ((long) (randomBytes[7] & 0xFF) << 16) |
                ((long) (randomBytes[8] & 0xFF) << 8) |
                (randomBytes[9] & 0xFF);

        return new UUID(mostSigBits, leastSigBits);
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
        // UUID 버전 확인 (상위 4비트가 7인지)
        return ((uuid.getMostSignificantBits() >>> 12) & 0xF) == 7;
    }

    /**
     * 현재 시간으로부터 상대적 시간차이를 가진 UUIDv7 생성 (테스트용)
     * 
     * @param offsetMs 현재 시간으로부터의 오프셋 (밀리초)
     * @return 지정된 시간의 UUIDv7
     */
    public static UUID generateWithOffset(long offsetMs) {
        long timestampMs = System.currentTimeMillis() + offsetMs;

        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long mostSigBits = (timestampMs << 16) |
                (0x7000L | ((randomBytes[0] & 0x0F) << 8) | (randomBytes[1] & 0xFF));

        long leastSigBits = ((long) (0x80 | (randomBytes[2] & 0x3F)) << 56) |
                ((long) (randomBytes[3] & 0xFF) << 48) |
                ((long) (randomBytes[4] & 0xFF) << 40) |
                ((long) (randomBytes[5] & 0xFF) << 32) |
                ((long) (randomBytes[6] & 0xFF) << 24) |
                ((long) (randomBytes[7] & 0xFF) << 16) |
                ((long) (randomBytes[8] & 0xFF) << 8) |
                (randomBytes[9] & 0xFF);

        return new UUID(mostSigBits, leastSigBits);
    }
}