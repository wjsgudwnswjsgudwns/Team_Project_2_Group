package com.example.test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiSummarizeService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 해외 뉴스 기사를 한글로 요약 (RSS 피드용)
     */
    public String summarizeAndTranslate(String title, String content, String link, String imageUrl) {
        try {
            String prompt = createTranslationPrompt(title, content, link);
            String response = callGeminiApi(prompt);
            String extractedText = extractTextFromResponse(response);
            return formatWithImage(extractedText, link, imageUrl, "해외 뉴스");
        } catch (Exception e) {
            System.err.println("Gemini 요약 실패: " + e.getMessage());
            return generateFallbackSummary(title, content, link, imageUrl, "해외 뉴스");
        }
    }

    /**
     * 해외 뉴스 번역용 프롬프트
     */
    private String createTranslationPrompt(String title, String content, String link) {
        return String.format("""
            다음은 컴퓨터 하드웨어 관련 영문 뉴스 기사입니다.
            이 기사를 한국어로 요약해주세요.
            
                ［요구사항］
                1. 제목을 한국어로 정확하게 번역해주세요 (영어를 포함하지 마세요)
                2. 핵심 내용을 3-5개의 문단으로 요약해주세요
                3. 각 문단은 2-3문장으로 구성하고, 반드시 명확하게 구분해주세요
                4. 한 문단에 모든 내용을 몰아넣지 말고, 주제별로 나누어 작성해주세요
                5. 기술적인 용어는 원문 그대로 유지하되 간단한 설명을 괄호 안에 추가해주세요
                6. 독자가 이해하기 쉽게 작성해주세요
                
                ［출력 형식］
                반드시 다음 형식을 정확히 따라주세요:
                
                TITLE: [번역된 제목]
                
                CONTENT:
                [첫 번째 문단 - 주요 내용 소개]
                {{PARAGRAPH}}
                [두 번째 문단 - 세부 정보 1]
                {{PARAGRAPH}}
                [세 번째 문단 - 세부 정보 2]
                
                ⚠️ 중요:
                - TITLE:과 CONTENT: 태그를 반드시 포함해주세요
                - 각 문단은 반드시 {{PARAGRAPH}}로 구분해주세요
                - 모든 내용을 한 문단에 몰아넣지 마세요
                - 최소 3개의 문단으로 나누어주세요
                - 제목에는 제품명 등 명사를 제외한 나머지는 영어를 포함하지 마세요
            
            ---
            
            【원문 제목】
            %s
            
            【원문 내용】
            %s
            """, title, content.substring(0, Math.min(content.length(), 2000)));
    }

    /**
     * Gemini API 호출
     */
    private String callGeminiApi(String prompt) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }

    /**
     * Gemini 응답에서 텍스트 추출
     */
    private String extractTextFromResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode candidates = root.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.path("content");
            JsonNode parts = content.path("parts");

            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText();
            }
        }

        throw new Exception("응답에서 텍스트를 추출할 수 없습니다.");
    }

    /**
     * 해외 뉴스 포맷 (이미지 포함)
     */
    private String formatWithImage(String aiResponse, String link, String imageUrl, String sourceType) {
        try {
            String title = "";
            String content = "";

            if (aiResponse.contains("TITLE:") && aiResponse.contains("CONTENT:")) {
                String[] parts = aiResponse.split("CONTENT:");
                title = parts[0].replace("TITLE:", "").trim();
                content = parts.length > 1 ? parts[1].trim() : "";
            } else {
                String[] lines = aiResponse.split("\n", 2);
                title = lines[0].replace("#", "").trim();
                content = lines.length > 1 ? lines[1].trim() : "";
            }

            content = content.replace("{{PARAGRAPH}}", "</div><br><div></div><br><div>");
            content = "<div>" + content + "</div>";

            StringBuilder result = new StringBuilder();

            result.append("<div><h2>").append(title).append("</h2></div>");
            result.append("<div></div><br>");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                result.append("<div><img src='").append(imageUrl)
                        .append("' alt='뉴스 이미지' style='max-width: 100%; height: auto;'></div>");
                result.append("<div></div><br>");
            }

            result.append(content);
            result.append("<div></div><br><br><br><br>");
            result.append("<p style='text-align: center; color: #ff7f00;'>해당 글은 직접 쓴 글이 아니며 ai로 요약된 기사입니다</p> ");
            result.append("<div style='padding: 10px; background-color: rgba(100, 100, 100, 0.2); border-left: 3px solid #10b981;'>");
            result.append("📌 출처: ").append(sourceType).append("<br>");
            result.append("🔗 원문 링크: <a href='").append(link)
                    .append("' target='_blank' style='color: #10b981;'>").append(link).append("</a>");
            result.append("</div>");

            return result.toString();

        } catch (Exception e) {
            System.err.println("포맷팅 실패: " + e.getMessage());
            return formatSimple(aiResponse, link, imageUrl, sourceType);
        }
    }

    /**
     * 간단한 폴백 포맷
     */
    private String formatSimple(String aiResponse, String link, String imageUrl, String sourceType) {
        StringBuilder result = new StringBuilder();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            result.append("<div><img src='").append(imageUrl)
                    .append("' alt='뉴스 이미지' style='max-width: 100%; height: auto;'></div>");
            result.append("<div></div><br>");
        }

        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append("<div>").append(line).append("</div>");
                result.append("<div></div><br>");
            }
        }

        result.append("<div style='padding: 10px; background-color: rgba(100, 100, 100, 0.2);'>");
        result.append("📌 출처: ").append(sourceType).append("<br>");
        result.append("🔗 원문: <a href='").append(link).append("' target='_blank'>")
                .append(link).append("</a></div>");

        return result.toString();
    }

    /**
     * Gemini 실패 시 기본 요약 생성
     */
    private String generateFallbackSummary(String title, String content, String link, String imageUrl, String sourceType) {
        StringBuilder result = new StringBuilder();

        result.append("<div><h2>").append(title).append("</h2></div>");
        result.append("<div></div><br>");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            result.append("<div><img src='").append(imageUrl)
                    .append("' alt='뉴스 이미지' style='max-width: 100%; height: auto;'></div>");
            result.append("<div></div><br>");
        }

        String shortContent = content.substring(0, Math.min(content.length(), 300));
        result.append("<div>").append(shortContent).append("...</div>");
        result.append("<div></div><br>");
        result.append("<div>⚠️ 자동 번역이 실패하여 원문을 표시합니다.</div>");
        result.append("<div></div><br>");
        result.append("<div style='padding: 10px; background-color: rgba(100, 100, 100, 0.2);'>");
        result.append("📌 출처: ").append(sourceType).append("<br>");
        result.append("🔗 원문: <a href='").append(link).append("' target='_blank'>")
                .append(link).append("</a></div>");

        return result.toString();
    }

}