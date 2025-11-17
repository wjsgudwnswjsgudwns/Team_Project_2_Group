package com.example.test.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class CompatibilityController {

    @Value("${mistral.api.key}")
    private String mistralApiKey;

    @Value("${mistral.api.url}")
    private String mistralApiUrl;

    @PostMapping("/checkAll")
    public ResponseEntity<?> checkAllCompatibility(@RequestBody Map<String, String> parts) {
        String cpu = parts.get("cpu");
        String mainboard = parts.get("mainboard");
        String memory = parts.get("memory");
        String gpu = parts.get("gpu");
        String pcCase = parts.get("case");
        String power = parts.get("power");

        // ✅ 프롬프트 간결화
        String combinedPrompt = String.format(
                "다음 PC 부품 호환성을 체크하세요. 각 항목은 반드시 '번호. 결과 - 이유 한 문장'으로만 답변하세요.\n\n" +
                        "부품:\n" +
                        "CPU: %s\n" +
                        "메인보드: %s\n" +
                        "메모리: %s\n" +
                        "GPU: %s\n" +
                        "케이스: %s\n" +
                        "파워: %s\n\n" +
                        "체크 항목 (각 1줄로만 답변):\n" +
                        "1. CPU와 메모리: 호환됨/호환안됨/조건부호환 중 선택 + 이유\n" +
                        "2. CPU와 메인보드: 호환됨/호환안됨/조건부호환 중 선택 + 이유\n" +
                        "3. 메인보드와 메모리: 호환됨/호환안됨/조건부호환 중 선택 + 이유\n" +
                        "4. 케이스와 메인보드: 장착가능/장착불가 중 선택 + 이유\n" +
                        "5. 케이스와 GPU: 장착가능/장착불가 중 선택 + 이유\n" +
                        "6. 케이스와 파워: 호환됨/호환안됨 중 선택 + 이유",
                cpu, mainboard, memory, gpu, pcCase, power
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mistralApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "mistral-large-latest");
        body.put("messages", List.of(
                // ✅ 시스템 프롬프트 강화
                Map.of("role", "system", "content",
                        "너는 PC 부품 호환성 전문가다. " +
                                "각 항목을 '번호. 결과 - 이유'로 1줄씩만 답변해라. " +
                                "절대 장황하게 설명하지 말고, 핵심만 짧게 말해라. " +
                                "호환안됨이면 대체품 1개만 간단히 추천해라."),
                Map.of("role", "user", "content", combinedPrompt)
        ));
        body.put("max_tokens", 500); // ✅ 토큰 수 줄임 (1000 → 500)
        body.put("temperature", 0.3); // ✅ 더 결정적이고 간결한 답변

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    mistralApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            String fullAnswer = extractAnswer(response.getBody());
            List<Map<String, String>> results = parseResponse(fullAnswer);

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "호환성 체크 실패: " + e.getMessage()));
        }
    }

    private String extractAnswer(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "응답 파싱 오류: " + e.getMessage();
        }
    }

    // ✅ AI 응답을 파싱해서 각 항목별로 분리
    private List<Map<String, String>> parseResponse(String fullAnswer) {
        List<Map<String, String>> results = new ArrayList<>();

        String[] questions = {
                "CPU와 메모리 호환성",
                "CPU와 메인보드 호환성",
                "메모리와 메인보드 호환성",
                "케이스와 메인보드 장착",
                "케이스와 GPU 장착",
                "케이스와 파워 호환성"
        };

        // 응답을 줄 단위로 분리
        String[] lines = fullAnswer.split("\n");

        int currentIndex = 0;
        StringBuilder currentAnswer = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 숫자로 시작하는 줄이면 새로운 항목
            if (line.matches("^\\d+\\..*") || line.matches("^\\d+\\).*")) {
                // 이전 답변 저장
                if (currentAnswer.length() > 0 && currentIndex > 0) {
                    results.add(Map.of(
                            "question", questions[currentIndex - 1],
                            "answer", currentAnswer.toString().trim()
                    ));
                }

                // 새로운 답변 시작
                currentAnswer = new StringBuilder();
                currentAnswer.append(line.replaceFirst("^\\d+[\\.\\)]\\s*", "")).append(" ");
                currentIndex++;
            } else {
                // 현재 항목에 추가
                currentAnswer.append(line).append(" ");
            }
        }

        // 마지막 항목 저장
        if (currentAnswer.length() > 0 && currentIndex > 0) {
            results.add(Map.of(
                    "question", questions[currentIndex - 1],
                    "answer", currentAnswer.toString().trim()
            ));
        }

        // 파싱 실패 시 전체 응답 반환
        if (results.isEmpty()) {
            for (String question : questions) {
                results.add(Map.of(
                        "question", question,
                        "answer", "파싱 실패 - 전체 응답: " + fullAnswer
                ));
            }
        }

        // 누락된 항목 채우기
        while (results.size() < questions.length) {
            results.add(Map.of(
                    "question", questions[results.size()],
                    "answer", "응답 없음"
            ));
        }

        return results;
    }
}