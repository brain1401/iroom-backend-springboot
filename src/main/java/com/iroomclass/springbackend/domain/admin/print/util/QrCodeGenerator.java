package com.iroomclass.springbackend.domain.admin.print.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * QR 코드 생성 유틸리티
 * 
 * 답안지용 QR 코드를 생성합니다.
 */
@Slf4j
@Component
public class QrCodeGenerator {

    /**
     * QR 코드 생성 (Base64 인코딩된 이미지 반환)
     * 
     * @param content QR 코드에 포함할 내용
     * @return Base64로 인코딩된 QR 코드 이미지
     */
    public String generateQrCodeBase64(String content) {
        try {
            // TODO: 실제 QR 코드 생성 라이브러리 구현
            // 현재는 Mock 데이터 반환
            log.info("QR 코드 생성: content={}", content);
            
            // Mock QR 코드 이미지 (1x1 픽셀 투명 PNG)
            String mockQrCode = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
            
            return mockQrCode;
        } catch (Exception e) {
            log.error("QR 코드 생성 실패: content={}", content, e);
            return null;
        }
    }

    /**
     * QR 코드 URL 생성
     * 
     * @param examId 시험 ID
     * @param documentId 문서 ID
     * @return QR 코드 URL
     */
    public String generateQrCodeUrl(Long examId, Long documentId) {
        return String.format("https://iroomclass.com/exam/%d/document/%d/qr", examId, documentId);
    }
}
