package com.example.test.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiConsultController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @PostMapping("/consult")
    public ResponseEntity<?> consult(@RequestBody Map<String, Object> requestBody) {
        try {
            Map<String, Object> formData = (Map<String, Object>) requestBody.get("formData");

            // 🔹 Prompt 생성
            String prompt = String.format("""
                사용자가 입력한 PC 견적 요구사항:
                - 사용 용도: %s
                - 예산: %s만 원 ~ %s만 원
                - CPU 선호: %s
                - GPU 선호: %s
                - 메인보드: %s
                - 메모리: %s

                위 요구사항에 맞는 PC 견적 구성 (CPU, GPU, 메인보드, 메모리, SSD, 케이스, 파워)을 제안하고,
                각 부품의 추천 이유를 간단히 설명해주세요.
                결과는 아레 형식으로 부탁
                {
                    '제품종류': '제품명',
            """,
                    formData.get("usage"),
                    formData.get("minBudget"),
                    formData.get("maxBudget"),
                    formData.get("cpu"),
                    formData.get("gpu"),
                    formData.get("mainboard"),
                    formData.get("memory")
            );

            // 🔹 Gemini API 요청 본문
            Map<String, Object> content = Map.of(
                    "parts", List.of(Map.of("text", prompt))
            );

            Map<String, Object> body = Map.of("contents", List.of(content));

            // 🔹 요청 전송
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map<String, Object> response =
                    restTemplate.exchange(
                            geminiApiUrl + "?key=" + geminiApiKey,
                            HttpMethod.POST, entity, Map.class
                    ).getBody();

            // 🔹 응답 파싱
            String resultText = "";
            try {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        resultText = (String) parts.get(0).get("text");
                    }
                }
            } catch (Exception e) {
                resultText = response.toString(); // fallback
            }

            // 🔹 클라이언트로 전달
            return ResponseEntity.ok(Map.of("result", resultText));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Gemini API 호출 실패: " + e.getMessage()));
        }
    }
}
