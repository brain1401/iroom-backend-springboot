package com.iroomclass.springbackend.domain.exam.service;

import java.util.UUID;

import com.iroomclass.springbackend.domain.exam.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * QR 코드 자동 생성 서비스
 * 
 * 시험지 문서 생성 시 ANSWER_SHEET 타입에 자동으로 QR 코드를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeGenerationService {

    private final QrCodeGenerator qrCodeGenerator;

    /**
     * 답안지용 QR 코드 생성
     * 
     * @param examSheetId 시험지 ID
     * @return Base64로 인코딩된 QR 코드 이미지
     */
    public String generateAnswerSheetQrCode(UUID examSheetId) {
        try {
            log.info("답안지 QR 코드 생성 시작: examSheetId={}", examSheetId);
            
            // 시험별 고유 QR 코드 내용 생성 (짧게)
            String qrContent = String.format("E%d", examSheetId);
            
            // QR 코드 생성
            String qrCodeBase64 = qrCodeGenerator.generateQrCodeBase64(qrContent);
            
            log.info("답안지 QR 코드 생성 완료: examSheetId={}, qrContent={}", examSheetId, qrContent);
            
            return qrCodeBase64;
            
        } catch (Exception e) {
            log.error("답안지 QR 코드 생성 실패: examSheetId={}", examSheetId, e);
            throw new RuntimeException("QR 코드 생성에 실패했습니다", e);
        }
    }

    /**
     * 시험별 고유 QR 코드 URL 생성
     * 
     * @param examSheetId 시험지 ID
     * @return QR 코드 URL (Base64 데이터 URL)
     */
    public String generateAnswerSheetQrCodeUrl(UUID examSheetId) {
        String qrCodeBase64 = generateAnswerSheetQrCode(examSheetId);
        return "data:image/png;base64," + qrCodeBase64;
    }
}
