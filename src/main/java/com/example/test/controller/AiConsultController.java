package com.example.test.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AiConsultController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @PostMapping("/consult")
    public ResponseEntity<?> consult(@RequestBody Map<String, Object> requestBody) {
        try {
            Map<String, Object> formData = (Map<String, Object>) requestBody.get("formData");
            String today = LocalDate.now().toString();

            String prompt = String.format("""
                    당신은 오늘 날짜로 활동하는 PC 견적 전문가입니다. 
                    오늘 날짜는 %8$s입니다.
                    
                    사용자 요구사항:
                    - 사용 용도: %1$s
                    - 예산: %2$s만 원 ~ %3$s만 원
                    - CPU 선호: %4$s
                    - GPU 선호: %5$s
                    - 메인보드: %6$s
                    - 메모리: %7$s
                    
                    **역할:**
                    당신은 PC 부품 견적 전문가로서, 오늘(%8$s) 기준의 최신 세대 제품을 검색하여 예산에 맞는 추천해야 합니다.
                
                    **매우 중요한 제품명 작성 규칙:**
                    1. 제품명은 반드시 "브랜드명 + 전체 모델명"을 모두 포함해야 합니다.
                    2. 2025년에 출시되어 현재 판매 중인 제품도 검색하세요.
                    3. 단종되었거나 품절된 제품은 다른 비슷한 제품으로 재검색하세요.
                    4. 제품명이 정확히 일치하지 않는 경우 추천하지 마세요.
                    5. 가장 중요한 규칙입니다. 노트북의 부품, 또는 완성된 조립형 PC의 부품을 추천하지 마세요.
                    6. 모든 부품 가격의 총합이 %3$s의 오차범위 5퍼센트를 넘기면 안됩니다.
                    
                    7. 만약 https://prod.danawa.com/info/?의 링크를 불러오지 못하고 https://search.danawa.com/의 링크를 불러오는 경우
                    리스트의 최상단에 있는 정보를 추천하세요.
                    
                    **제품명 예시**
                    이는 예시일 뿐이며 실제 제품을 추천해야합니다.
                    좋은 예시 : NVIDIA GeForce RTX 5060 D6X 12GB
                    잘못된 예시 : NVIDIA GeForce RTX 5060 D6X 12GB (블랙웰 아키텍처, 192bit 메모리 버스, 12GB GDDR6X)
                    
                    
                    **포함할 부품 (각 1개씩):**
                    - CPU
                    - 그래픽카드
                    - 메인보드
                    - 메모리
                    - 하드디스크 (SSD)
                    - 파워서플라이
                    - 쿨러
                    - 케이스
                
                    **응답 형식 (JSON만):**
                    [
                       {
                         "제품종류": "CPU",
                         "제품이미지": "",
                         "제품명": "AMD Ryzen 9 9950X (16코어)",
                         "가격": "",
                         "판매사이트링크": ""
                       },
                       {
                         "제품종류": "그래픽카드",
                         "제품이미지": "",
                         "제품명": "NVIDIA GeForce RTX 4070 Ti SUPER D6X 16GB",
                         "가격": "",
                         "판매사이트링크": ""
                       }
                    ]
                
                    **최종 확인사항:**
                    - 모든 제품명은 다나와에서 바로 검색 가능한 정확한 풀네임이어야 합니다.
                    - 약칭, 시리즈명만 사용 금지.
                    - 단종 제품, 루머 제품 절대 금지.
                    - 제품 간 호환성 확인 필수.
                    - 병목현상이 없도록 균형잡힌 구성.
                    
                    코드 블록(```) 사용 금지, 설명문 금지, 오직 JSON 배열만 반환하세요.
                    """,
                    formData.get("usage"),
                    formData.get("minBudget"),
                    formData.get("maxBudget"),
                    formData.get("cpu"),
                    formData.get("gpu"),
                    formData.get("mainboard"),
                    formData.get("memory"),
                    today
            );

            // Gemini API 요청
            Map<String, Object> content = Map.of(
                    "parts", List.of(Map.of("text", prompt))
            );
            Map<String, Object> body = Map.of("contents", List.of(content));

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map<String, Object> response =
                    restTemplate.exchange(
                            geminiApiUrl + "?key=" + geminiApiKey,
                            HttpMethod.POST, entity, Map.class
                    ).getBody();

            // 응답 파싱
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
                resultText = response.toString();
            }

            // 코드블록 제거
            resultText = resultText.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // JSON 배열 부분만 추출
            if (resultText != null) {
                int start = resultText.indexOf('[');
                int end = resultText.lastIndexOf(']');
                if (start != -1 && end != -1 && end > start) {
                    resultText = resultText.substring(start, end + 1);
                }
            }

            return ResponseEntity.ok(Map.of("result", resultText));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Gemini API 호출 실패: " + e.getMessage()));
        }
    }
}