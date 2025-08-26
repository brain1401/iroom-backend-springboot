package com.iroomclass.springbackend.domain.admin.print.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Schema(description = "인쇄 요청")
public class PrintRequest {

    @NotNull(message = "시험 ID는 필수입니다")
    @Schema(description = "시험 ID", example = "1")
    private Long examId;

    @NotEmpty(message = "인쇄할 문서 타입을 선택해주세요")
    @Schema(description = "인쇄할 문서 타입 목록", example = "[\"QUESTION_PAPER\", \"ANSWER_KEY\"]")
    private List<String> documentTypes;

    @Schema(description = "파일명 (선택사항)", example = "1학년중간고사_문제지")
    private String fileName;
}
