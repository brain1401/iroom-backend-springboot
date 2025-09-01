package com.iroomclass.springbackend.domain.exam.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.iroomclass.springbackend.config.AiServerConfig;
import com.iroomclass.springbackend.domain.exam.dto.answer.AiServerResponse;
import com.iroomclass.springbackend.domain.exam.dto.answer.RecognizedAnswer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 이미지 인식 서비스
 * 
 * 답안 이미지에서 텍스트를 추출하는 AI 서비스를 제공합니다.
 * 실제 AI 서버와 연동하여 OCR 기능을 수행합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiImageRecognitionServiceNew {

    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;
    
    private final AiServerConfig aiServerConfig;

    /**
     * 이미지에서 텍스트 추출
     * 
     * @param imageFilePath 이미지 파일 경로
     * @return AI 인식 결과 (텍스트, 신뢰도)
     */
    public AiRecognitionResult recognizeTextFromImage(String imageFilePath) {
        log.info("AI 이미지 인식 시작: 이미지 경로={}", imageFilePath);

        try {
            // 이미지 파일을 바이트 배열로 읽기
            File imageFile = new File(imageFilePath);
            if (!imageFile.exists()) {
                log.warn("이미지 파일이 존재하지 않음: {}", imageFilePath);
                return createFailureResult("이미지 파일을 찾을 수 없습니다");
            }

            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            
            // 단일 이미지 처리를 위해 리스트로 감싸서 기존 메서드 활용
            List<RecognizedAnswer> answers = recognizeAnswersFromImageBytes(imageBytes, imageFile.getName());
            
            if (answers.isEmpty()) {
                return createFailureResult("인식된 텍스트가 없습니다");
            }
            
            // 첫 번째 답안의 텍스트와 신뢰도 반환
            RecognizedAnswer firstAnswer = answers.get(0);
            log.info("AI 이미지 인식 완료: 인식 텍스트={}, 신뢰도={}", 
                    firstAnswer.recognizedAnswer(), firstAnswer.confidenceScore());

            return AiRecognitionResult.builder()
                    .recognizedText(firstAnswer.recognizedAnswer())
                    .confidenceScore(firstAnswer.confidenceScore())
                    .build();

        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패: 이미지 경로={}, 오류={}", imageFilePath, e.getMessage());
            return createFailureResult("이미지 파일 읽기에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 이미지 인식 실패: 이미지 경로={}, 오류={}", imageFilePath, e.getMessage());
            return createFailureResult("AI 이미지 인식에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 답안지 전체에서 모든 문제의 답안을 인식
     * 
     * @param answerSheetImageUrls 답안지 이미지 URL 목록 (실제로는 파일 경로로 사용)
     * @return 인식된 답안 목록 (문제 번호별)
     */
    public List<RecognizedAnswer> recognizeAnswersFromSheet(List<String> answerSheetImageUrls) {
        log.info("답안지 전체 인식 시작: 이미지 개수={}", answerSheetImageUrls.size());

        List<RecognizedAnswer> allAnswers = new ArrayList<>();

        // 각 이미지에서 답안 인식
        for (int imageIndex = 0; imageIndex < answerSheetImageUrls.size(); imageIndex++) {
            String imagePath = answerSheetImageUrls.get(imageIndex);
            log.info("이미지 {} 처리 중: {}", imageIndex + 1, imagePath);

            try {
                // 이미지 파일을 바이트 배열로 읽기
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    log.warn("이미지 파일이 존재하지 않음: {}", imagePath);
                    continue;
                }

                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                
                // 해당 이미지에서 인식된 답안들
                List<RecognizedAnswer> imageAnswers = recognizeAnswersFromImageBytes(imageBytes, imageFile.getName());
                allAnswers.addAll(imageAnswers);
                
            } catch (IOException e) {
                log.error("이미지 파일 읽기 실패: {}, 오류={}", imagePath, e.getMessage());
                continue;
            } catch (Exception e) {
                log.error("이미지 인식 실패: {}, 오류={}", imagePath, e.getMessage());
                continue;
            }
        }

        // 문제 번호별로 정렬
        allAnswers.sort(Comparator.comparing(RecognizedAnswer::questionNumber));

        log.info("답안지 전체 인식 완료: 총 {}개 답안 인식", allAnswers.size());
        return allAnswers;
    }

    /**
     * 바이트 배열 이미지에서 답안 인식 (실제 AI 서버 호출)
     * 
     * @param imageBytes 이미지 바이트 배열
     * @param fileName 파일명
     * @return 인식된 답안 목록
     */
    private List<RecognizedAnswer> recognizeAnswersFromImageBytes(byte[] imageBytes, String fileName) {
        try {
            log.info("AI 서버 호출 시작: 파일명={}, 크기={}바이트", fileName, imageBytes.length);
            
            // Multipart 요청 구성
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            }).header("Content-Type", determineContentType(fileName));

            // AI 서버 호출
            AiServerResponse response = aiServerWebClient
                .post()
                .uri(aiServerConfig.getTextRecognitionPath() + "/answer-sheet")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AiServerResponse.class)
                .timeout(Duration.ofSeconds(aiServerConfig.getResponseTimeout()))
                .block();

            if (response == null) {
                log.warn("AI 서버 응답이 null입니다");
                return new ArrayList<>();
            }

            List<RecognizedAnswer> recognizedAnswers = response.toRecognizedAnswers();
            log.info("AI 서버 응답 처리 완료: {}개 답안 인식", recognizedAnswers.size());
            
            return recognizedAnswers;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 호출 실패: HTTP Status={}, 응답 본문={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 이미지 인식 중 예상치 못한 오류: {}", e.getMessage());
            throw new RuntimeException("AI 이미지 인식 실패: " + e.getMessage());
        }
    }

    /**
     * 파일 확장자로부터 Content-Type 결정
     * 
     * @param fileName 파일명
     * @return Content-Type 문자열
     */
    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (lowerFileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (lowerFileName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    /**
     * 실패 결과 생성
     * 
     * @param message 실패 메시지
     * @return 실패 결과
     */
    private AiRecognitionResult createFailureResult(String message) {
        return AiRecognitionResult.builder()
                .recognizedText(message)
                .confidenceScore(0.0)
                .build();
    }

    /**
     * AI 인식 결과 클래스
     */
    public static class AiRecognitionResult {
        private final String recognizedText;
        private final double confidenceScore;

        public AiRecognitionResult(String recognizedText, double confidenceScore) {
            this.recognizedText = recognizedText;
            this.confidenceScore = confidenceScore;
        }

        public String getRecognizedText() {
            return recognizedText;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String recognizedText;
            private double confidenceScore;

            public Builder recognizedText(String recognizedText) {
                this.recognizedText = recognizedText;
                return this;
            }

            public Builder confidenceScore(double confidenceScore) {
                this.confidenceScore = confidenceScore;
                return this;
            }

            public AiRecognitionResult build() {
                return new AiRecognitionResult(recognizedText, confidenceScore);
            }
        }
    }
}