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

        List<Map<String, String>> prompts = List.of(
                Map.of("question", "CPU와 메모리 호환성",
                        "prompt", String.format("%s와 %s는 호환되나요? 호환됨/호환안됨/조건부호환 중 하나로 시작하고, 호환 안 되면 %s와 호환되는 메모리 제품을 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", cpu, memory, cpu)),
                Map.of("question", "CPU와 메인보드 호환성",
                        "prompt", String.format("%s와 %s는 호환되나요? 호환됨/호환안됨/조건부호환 중 하나로 시작하고, 호환 안 되면 %s와 호환되는 메인보드 제품을 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", cpu, mainboard, cpu)),
                Map.of("question", "메모리와 메인보드 호환성",
                        "prompt", String.format("%s와 %s는 호환되나요? 호환됨/호환안됨/조건부호환 중 하나로 시작하고, 호환 안 되면 %s와 호환되는 메모리 제품을 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", mainboard, memory, mainboard)),
                Map.of("question", "케이스와 메인보드 장착",
                        "prompt", String.format("%s 케이스에 %s 메인보드가 장착되나요? 장착가능/장착불가 중 하나로 시작하고, 장착 안 되면 %s 메인보드와 호환되는 케이스를 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", pcCase, mainboard, mainboard)),
                Map.of("question", "케이스와 GPU 장착",
                        "prompt", String.format("%s 케이스에 %s 그래픽카드가 장착되나요? 장착가능/장착불가 중 하나로 시작하고, 장착 안 되면 %s에 맞는 그래픽카드나 케이스를 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", pcCase, gpu, pcCase)),
                Map.of("question", "케이스와 파워 호환성",
                        "prompt", String.format("%s 케이스와 %s 파워는 호환되나요? 호환됨/호환안됨 중 하나로 시작하고, 호환 안 되면 %s 케이스와 호환되는 파워를 1개 추천해주세요. 총 2줄 이내로 답변해주세요.", pcCase, power, pcCase))
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mistralApiKey);

        List<Map<String, String>> results = new ArrayList<>();

        for (Map<String, String> item : prompts) {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "mistral-large-latest");
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "너는 PC 부품 호환성 전문가야. 반드시 '호환됨/호환안됨/조건부호환/장착가능/장착불가' 중 하나로 시작해. 호환되지 않으면 구체적인 대체 제품명을 1개만 추천하고, 총 2줄 이내로 간단히 답변해."),
                    Map.of("role", "user", "content", item.get("prompt"))
            ));
            body.put("max_tokens", 150);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        mistralApiUrl,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                String answer = extractAnswer(response.getBody());
                results.add(Map.of(
                        "question", item.get("question"),
                        "answer", answer
                ));
            } catch (Exception e) {
                results.add(Map.of(
                        "question", item.get("question"),
                        "error", e.getMessage()
                ));
            }
        }

        return ResponseEntity.ok(results);
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
}