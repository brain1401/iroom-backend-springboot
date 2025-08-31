package com.iroomclass.springbackend.domain.exam.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * LaTeX 수식을 이미지로 변환하는 유틸리티
 */
@Slf4j
@Component
public class LatexRenderer {

    private static final Pattern LATEX_PATTERN = Pattern.compile("\\$(.*?)\\$");
    private static final Pattern DISPLAY_LATEX_PATTERN = Pattern.compile("\\$\\$(.*?)\\$\\$");

    /**
     * HTML 내용에서 LaTeX 수식을 이미지로 변환
     */
    public String renderLatexInHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }

        try {
            // 블록 수식 처리 ($$...$$)
            htmlContent = renderDisplayLatex(htmlContent);
            
            // 인라인 수식 처리 ($...$)
            htmlContent = renderInlineLatex(htmlContent);
            
            return htmlContent;
        } catch (Exception e) {
            log.error("LaTeX 렌더링 중 오류 발생: {}", e.getMessage(), e);
            return htmlContent; // 오류 발생 시 원본 반환
        }
    }

    /**
     * 블록 수식 렌더링 ($$...$$)
     */
    private String renderDisplayLatex(String htmlContent) {
        Matcher matcher = DISPLAY_LATEX_PATTERN.matcher(htmlContent);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String latex = matcher.group(1);
            try {
                String imageTag = renderLatexToImageTag(latex, 24, true);
                matcher.appendReplacement(result, imageTag);
            } catch (Exception e) {
                log.warn("블록 LaTeX 렌더링 실패: {}", latex, e);
                matcher.appendReplacement(result, "<span style='color: red;'>[수식 렌더링 실패: " + latex + "]</span>");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 인라인 수식 렌더링 ($...$)
     */
    private String renderInlineLatex(String htmlContent) {
        Matcher matcher = LATEX_PATTERN.matcher(htmlContent);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String latex = matcher.group(1);
            try {
                String imageTag = renderLatexToImageTag(latex, 16, false);
                matcher.appendReplacement(result, imageTag);
            } catch (Exception e) {
                log.warn("인라인 LaTeX 렌더링 실패: {}", latex, e);
                matcher.appendReplacement(result, "<span style='color: red;'>[수식 렌더링 실패: " + latex + "]</span>");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * LaTeX를 이미지 태그로 변환
     */
    private String renderLatexToImageTag(String latex, int fontSize, boolean isDisplay) throws IOException {
        // LaTeX 수식을 이미지로 변환
        TeXFormula formula = new TeXFormula(latex);
        
        // 수식 타입 설정 (인라인 또는 블록)
        int style = isDisplay ? TeXConstants.STYLE_DISPLAY : TeXConstants.STYLE_TEXT;
        
        TeXIcon icon = formula.createTeXIcon(style, fontSize);
        
        // 이미지 생성
        BufferedImage image = new BufferedImage(
            icon.getIconWidth(), 
            icon.getIconHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
        g2d.setColor(Color.BLACK);
        
        // 수식 그리기
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        
        // 이미지를 Base64로 인코딩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
        
        // 이미지 태그 생성
        String styleAttr = isDisplay ? "display: block; margin: 10px auto;" : "vertical-align: middle;";
        return String.format("<img src='data:image/png;base64,%s' alt='%s' style='%s'/>", 
            base64Image, latex, styleAttr);
    }
}
