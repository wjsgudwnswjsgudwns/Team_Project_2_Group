package com.example.test.chatbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }

    public String getCompletion(String userMessage) {
        try {
            String today = LocalDate.now().toString();

            // 시스템 프롬프트 추가
            String systemPrompt = String.format("""
                    당신은 오늘 날짜로 활동하는 PC 견적 전문가입니다. 
                    오늘 날짜는 %1$s입니다.
                    
                    중요 규칙:
                    1. 현재는 2025년 11월입니다. AMD Ryzen 9000 시리즈(9800X3D, 9900X 등)는 이미 출시된 제품입니다.
                    2. 답변은 최대한 간결하게 작성하세요.
                    3. 중고 PC 가격 확인 상담 질문에는 중고 제품임을 인지하고 대답하세요.
                    4. 중고 PC 가격 확인 상담 질문에는 "추천" 또는 "비추천" 또는 "적당"으로 시작하고, 이유만 1-2줄로 설명하세요.
                    5. PC 부품 호환성 검사 서비스에는 "호환됩니다" 또는 "호환되지 않습니다"로 시작하고, 핵심 이유만 1-2줄로 설명하세요.
                    """,
                    today
            );

            // 메시지 리스트 생성 (시스템 프롬프트 + 사용자 메시지)
            List<GeminiRequest.Content> contents = new ArrayList<>();

            // 시스템 프롬프트
            GeminiRequest.Part systemPart = new GeminiRequest.Part(systemPrompt);
            GeminiRequest.Content systemContent = new GeminiRequest.Content(Collections.singletonList(systemPart));
            contents.add(systemContent);

            // 사용자 메시지
            GeminiRequest.Part userPart = new GeminiRequest.Part(userMessage);
            GeminiRequest.Content userContent = new GeminiRequest.Content(Collections.singletonList(userPart));
            contents.add(userContent);

            GeminiRequest request = new GeminiRequest(contents);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            // API 호출
            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeminiResponse.class
            );

            // 응답 파싱
            if (response.getBody() != null
                    && response.getBody().getCandidates() != null
                    && !response.getBody().getCandidates().isEmpty()) {
                return response.getBody().getCandidates().get(0)
                        .getContent()
                        .getParts()
                        .get(0)
                        .getText();
            }

            return "응답을 받지 못했습니다.";

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }
}