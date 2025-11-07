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
                    
                    [역할]
                    당신은 PC 부품 견적 전문가입니다.
                    오늘(%8$s) 기준으로 **최신 세대의 개별 PC 부품을 검색 및 추천**해야 합니다.
                    완성형 PC(조립/본체/데스크탑)는 절대 검색하거나 언급하지 마세요.
                
                    [0순위: 절대 금지 규칙 (검색 및 추천 단계 모두 적용)]
                    [검색 대상 강제]
                        - 검색은 반드시 아래 8개 카테고리 각각에 대해 개별적으로 수행해야 합니다:
                        ▶ CPU, M/B(메인보드), RAM(메모리), VGA(그래픽카드), SSD, PSU(파워), CASE(케이스), COOLER(쿨러)
                        - **“PC”, “본체”, “조립PC”, “데스크탑”** 등 완성형 제품 관련 키워드는 절대 검색하지 마세요.
                    
                    [판매 상태 필터]
                       - 검색된 제품이 **‘단종’, ‘품절’, ‘일시 품절’, ‘단종 예정’** 상태일 경우 즉시 제외합니다.
                       - 반드시 **‘현재 판매 중’**이며 **‘재고 있음’**으로 확인된 제품만 유효합니다.
                       - 위 조건을 만족하지 않으면 **즉시 동일 카테고리 내에서 대체 제품을 재검색**하세요.
                               
                    [1순위: 견적 구성 규칙]
                    [병목현상및 호환성 지침]
                        병목 형상이나, 제품간의 호환성을 반드시 체크하여 추천하세요.
                    [제품명 정합성]
                        - 각 제품명은 반드시 “브랜드명 + 전체 모델명”을 정확히 포함해야 합니다.
                        - 약칭, 오탈자, 일부 누락된 모델명은 모두 부적합으로 간주합니다.
                    [예산 규칙] 
                        - 총 견적 합계는 예산(%3$s) 기준 +5퍼센트 오차 범위를 초과할 수 없습니다.
                    
                    [2순위: 검색 및 최신성 지침]
                    [최신성]
                       - 오늘(%8$s) 기준으로 **가장 최근에 출시되어 활발히 판매 중인 세대**의 제품을 기준으로 추천하세요.
                       - 구형 세대(예: 이전 세대 CPU/VGA)는 절대 추천하지 않습니다.
                    [검색 결과 처리]
                         - 상세 페이지 접근이 불가능할 경우, **‘현재 판매 중’ 표시가 있는 인기순 최상단 제품**을 선택합니다.
                         - 모든 추천 항목에 “현재 판매 중”임을 다시 한 번 검증 후 출력하세요.
                    

                    [추가 검색 지침] 견적에 포함되어야 할 필수 카테고리는 CPU, 메인보드(M/B), 메모리(RAM), 그래픽카드(VGA), SSD(저장장치), 파워서플라이(PSU), PC 케이스, 쿨러이며, 각 카테고리별로 검색을 진행해야 합니다.
                    
                    - 만약 위 조건을 모두 만족하는 제품을 찾을 수 없다면,
                    “해당 카테고리에서 현재 판매 중인 적합한 제품을 찾을 수 없음”이라고 명시하고 재검색을 시도합니다.
                    
                    ────────────────────────────
                    【목표】
                    - 오직 **현역 세대의 개별 부품 견적**만을 구성합니다.
                    - 완성형 제품, 단종 제품, 품절 제품은 존재하지 않는 것으로 간주합니다.
                    ────────────────────────────
                    
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
                
                    견적 예시를 최대 5개 불러와줘. 
                
                    **응답 형식 (JSON만):**
                    [
                    {
                     "견적번호": 1,
                     "견적명": "가성비 구성",
                     "총예상가격": "120만원",
                     "부품목록": [
                       {
                         "제품종류": "CPU",
                         "제품이미지": "",
                         "제품명": "AMD Ryzen 5 7600X (6코어)",
                         "가격": "",
                         "판매사이트링크": ""
                       },
                       {
                         "제품종류": "그래픽카드",
                         "제품이미지": "",
                         "제품명": "NVIDIA GeForce RTX 4060 D6 8GB",
                         "가격": "",
                         "판매사이트링크": ""
                       }
                       // ... 나머지 6개 부품
                     ]
                    },
                    {
                     "견적번호": 2,
                     "견적명": "균형 구성",
                     "총예상가격": "160만원",
                     "부품목록": [
                       // 8개 부품
                     ]
                    }
                    // ... 견적 3, 4, 5
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