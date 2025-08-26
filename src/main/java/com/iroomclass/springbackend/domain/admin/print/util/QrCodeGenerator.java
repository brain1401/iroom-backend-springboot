package com.iroomclass.springbackend.domain.admin.print.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * QR 코드 생성 유틸리티
 * 
 * 답안지용 QR 코드를 생성합니다.
 */
@Slf4j
@Component
public class QrCodeGenerator {

    private static final int QR_CODE_SIZE = 200;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * QR 코드 생성 (Base64 인코딩된 이미지 반환)
     * 
     * @param content QR 코드에 포함할 내용
     * @return Base64로 인코딩된 QR 코드 이미지
     */
    public String generateQrCodeBase64(String content) {
        try {
            log.info("QR 코드 생성 시작: content={}", content);
            
            // QR 코드 이미지 생성
            BufferedImage qrCodeImage = generateQrCodeImage(content);
            
            // 이미지를 Base64로 인코딩
            String base64Image = convertImageToBase64(qrCodeImage);
            
            log.info("QR 코드 생성 완료: content={}, imageSize={}x{}", 
                content, qrCodeImage.getWidth(), qrCodeImage.getHeight());
            
            return base64Image;
            
        } catch (Exception e) {
            log.error("QR 코드 생성 실패: content={}", content, e);
            return generateFallbackQrCode();
        }
    }

    /**
     * QR 코드 이미지 생성
     */
    private BufferedImage generateQrCodeImage(String content) throws WriterException {
        // QR 코드 생성 설정
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        // QR 코드 생성
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        // 이미지로 변환
        BufferedImage qrCodeImage = new BufferedImage(QR_CODE_SIZE, QR_CODE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = qrCodeImage.createGraphics();
        
        // 배경색 설정 (흰색)
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, QR_CODE_SIZE, QR_CODE_SIZE);
        
        // QR 코드 색상 설정 (검은색)
        graphics.setColor(Color.BLACK);
        
        // QR 코드 그리기
        for (int x = 0; x < QR_CODE_SIZE; x++) {
            for (int y = 0; y < QR_CODE_SIZE; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return qrCodeImage;
    }

    /**
     * 이미지를 Base64로 변환
     */
    private String convertImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, IMAGE_FORMAT, baos);
        byte[] imageBytes = baos.toByteArray();
        baos.close();
        
        return Base64.getEncoder().encodeToString(imageBytes);
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

    /**
     * 폴백 QR 코드 생성 (에러 시 사용)
     */
    private String generateFallbackQrCode() {
        try {
            // 간단한 폴백 QR 코드 생성
            BufferedImage fallbackImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = fallbackImage.createGraphics();
            
            // 빨간색 배경 (에러 표시)
            graphics.setColor(Color.RED);
            graphics.fillRect(0, 0, 100, 100);
            
            // 에러 텍스트
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Arial", Font.BOLD, 12));
            graphics.drawString("QR Error", 25, 50);
            
            graphics.dispose();
            
            return convertImageToBase64(fallbackImage);
            
        } catch (Exception e) {
            log.error("폴백 QR 코드 생성도 실패", e);
            // 최종 폴백: 1x1 픽셀 투명 이미지
            return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        }
    }
}
