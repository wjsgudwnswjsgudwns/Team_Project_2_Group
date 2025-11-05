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
                    당신은 PC 부품 견적 전문가로서, 오늘(%8$s) 기준의 최신 세대 제품을 추천해야 합니다.
                
                    **매우 중요한 제품명 작성 규칙:**
                    1. 제품명은 반드시 "브랜드명 + 전체 모델명 + 세부 스펙"을 모두 포함해야 합니다.
                    2. 약칭이나 시리즈명만 쓰지 말고, 다나와에서 검색 가능한 정확한 풀네임을 사용하세요.
                    3. 2025년에 출시되어 현재 판매 중인 제품도 선택하세요.
                    4. 단종되었거나 품절된 제품은 절대 추천하지 마세요.
                
                    **올바른 제품명 작성 예시:**
                    ❌ 잘못된 예시: "ASUS ROG STRIX X870E"
                    ✅ 올바른 예시: "ASUS ROG STRIX X870E-E GAMING WIFI"
                    
                    ❌ 잘못된 예시: "Seasonic PRIME TX-1200"
                    ✅ 올바른 예시: "Seasonic PRIME TX-1200 80PLUS Platinum ATX 3.0"
                    
                    ❌ 잘못된 예시: "Lian Li O11 Dynamic"
                    ✅ 올바른 예시: "Lian Li O11 Dynamic EVO XL (블랙)"
                
                    **제품명 검증 필수:**
                    추천하기 전에 반드시 https://prod.danawa.com/info/?로 url이 시작하는지 확인하세요.
                    다음 제품들은 예시일 뿐이며, 실제 판매 중인 최신 제품으로 교체하세요.
                            
                    **2024-2025년 현재 판매 중인 제품 예시:**
                    
                    **CPU (정확한 풀네임):**
                    - AMD Ryzen 9 9950X (16코어)
                    - AMD Ryzen 7 9700X (8코어)
                    - AMD Ryzen 7 7800X3D
                    - Intel Core i9-14900K (24코어)
                    - Intel Core i7-14700K (20코어)
                    - Intel Core i5-14600K (14코어)
                    
                    **GPU (정확한 풀네임):**
                    - NVIDIA GeForce RTX 5090 D6X 32GB
                    - NVIDIA GeForce RTX 5080 D6X 16GB
                    - NVIDIA GeForce RTX 4070 Ti SUPER D6X 16GB
                    - NVIDIA GeForce RTX 4060 Ti D6X 8GB
                    - AMD Radeon RX 7900 XTX D6 24GB
                    - AMD Radeon RX 7800 XT D6 16GB
                    
                    **메인보드 (정확한 풀네임):**
                    - ASUS ROG STRIX X870E-E GAMING WIFI
                    - MSI MAG Z790 TOMAHAWK MAX WIFI DDR5
                    - GIGABYTE B650 AORUS ELITE AX V2
                    - ASRock B650M PG Riptide WIFI
                    
                    **RAM (정확한 풀네임):**
                    - G.SKILL Trident Z5 RGB DDR5-6000 CL30 32GB (16Gx2)
                    - G.SKILL Trident Z5 RGB DDR5-7200 CL34 32GB (16Gx2)
                    - CORSAIR VENGEANCE DDR5-6000 CL30 32GB (16Gx2)
                    - TeamGroup T-Force Delta RGB DDR5-6000 CL30 32GB (16Gx2)
                    
                    **SSD (정확한 풀네임):**
                    - Samsung 990 PRO M.2 NVMe 2TB
                    - WD BLACK SN850X NVMe M.2 1TB
                    - Crucial T700 M.2 NVMe 2TB
                    - SK hynix Platinum P41 M.2 NVMe 2TB
                    
                    **파워 (정확한 풀네임):**
                    - Seasonic VERTEX GX-850 ATX 3.0 (850W)
                    - Corsair RM850x (2024) ATX 3.1 (850W)
                    - MSI MAG A850GL PCIE5 ATX 3.0 (850W)
                    - Thermaltake Toughpower GF3 850W ATX 3.0
                    
                    **쿨러 (정확한 풀네임):**
                    - DeepCool AK620 ZERO DARK
                    - Noctua NH-D15S chromax.black
                    - be quiet! Dark Rock Pro 5
                    - Thermalright Peerless Assassin 120 SE
                    
                    **케이스 (정확한 풀네임):**
                    - Lian Li O11 Dynamic EVO XL (블랙)
                    - Fractal Design North XL (차콜 블랙)
                    - NZXT H9 Flow (화이트)
                    - Corsair 5000D AIRFLOW (블랙)
                
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