package com.example.test.chartai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiPriceService {

    // RestTemplate: HTTP 요청을 보내는 도구 (Gemini API 호출용)
    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public List<PriceData> getPriceHistory(String productName) {
        try {
            // 1단계: Gemini에게 보낼 질문(프롬프트) 만들기
            String prompt = createPrompt(productName);

            // 2단계: Gemini API 호출해서 답변 받기
            String geminiResponse = callGeminiApi(prompt);

            // 3단계: Gemini의 답변을 파싱해서 가격 데이터로 변환
            return parsePriceData(geminiResponse);

        } catch (Exception e) {
            // 에러가 발생하면 로그에 기록하고 예외를 던짐
            log.error("Error getting price history for: " + productName, e);
            throw new RuntimeException("가격 데이터를 가져오는데 실패했습니다: " + e.getMessage());
        }
    }

    private String createPrompt(String productName) {
        LocalDate now = LocalDate.now();

        return String.format(
                "당신은 오늘 날짜 기준 최신 컴퓨터 하드웨어 가격 분석 전문가입니다.\n\n" +
                        "제품명: %s\n" +
                        "오늘 날짜: %s\n\n" +
                        "작업:\n" +
                        "1. '%s'가 실제로 존재하고 출시된 제품인지 확인\n" +
                        "2. 출시일 확인\n" +
                        "3. 출시 월부터 %s까지 매월 대략적인 시장 가격 추정\n\n" +
                        "중요:\n" +
                        "- 최신 제품이라도 출시되었다면 반드시 데이터 생성\n" +
                        "- 정말 존재하지 않는 제품만 '제품 정보를 찾을 수 없습니다' 출력\n" +
                        "- 출시 예정(미래 제품)만 '출시 예정 제품입니다' 출력\n\n" +
                        "출력 형식:\n" +
                        "YYYY-MM: 가격원\n\n" +
                        "예시:\n" +
                        "2025-05: 450000원\n" +
                        "2025-06: 445000원\n" +
                        "2025-07: 440000원\n" +
                        "2025-08: 438000원\n" +
                        "2025-09: 435000원\n" +
                        "2025-10: 432000원\n" +
                        "2025-11: 430000원" +
                        "참고하는 사이트 주소를 "
                ,
                productName,
                now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                productName,
                now.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
        );
    }

    // Gemini API를 실제로 호출하는 메서드
    private String callGeminiApi(String prompt) {

        // 1단계: 요청 객체 생성 (AiPriceRequest)
        AiPriceRequest request = new AiPriceRequest();

        // Content 객체 생성
        AiPriceRequest.Content content = new AiPriceRequest.Content();

        // Part 객체 생성하고 텍스트 넣기
        AiPriceRequest.Content.Part part = new AiPriceRequest.Content.Part();
        part.setText(prompt);  // 우리가 만든 질문을 넣음

        // Part를 Content에 넣기
        content.setParts(List.of(part));

        // Content를 Request에 넣기
        request.setContents(List.of(content));


        // 2단계: HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // JSON 형식으로 보낸다고 알려줌


        // 3단계: HttpEntity 생성 (헤더 + 바디)
        HttpEntity<AiPriceRequest> entity = new HttpEntity<>(request, headers);


        // 4단계: URL 생성 (properties에서 가져온 URL + API 키)
        String url = apiUrl + "?key=" + apiKey;

        // 5단계: POST 요청 보내고 응답 받기
        AiPriceResponse response = restTemplate.postForObject(
                url,                      // 요청 보낼 주소
                entity,                   // 보낼 데이터 (헤더 + 바디)
                AiPriceResponse.class     // 응답을 이 클래스로 변환해줘
        );


        // 6단계: 응답에서 텍스트 추출
        if (response != null &&
                response.getCandidates() != null &&
                !response.getCandidates().isEmpty()) {

            // 첫 번째 답변 후보의 텍스트를 꺼냄
            return response.getCandidates().get(0)   // 첫 번째 답변
                    .getContent()                         // 내용
                    .getParts()                           // 부분들
                    .get(0)                               // 첫 번째 부분
                    .getText();                           // 텍스트!
        }

        // 응답이 비어있으면 에러
        throw new RuntimeException("Gemini API 응답이 비어있습니다");
    }

    // Gemini가 생성한 텍스트 응답을 파싱해서 PriceData 리스트로 변환하는 메서드
    private List<PriceData> parsePriceData(String response) {

        log.info("=== Gemini 원본 응답 ===");
        log.info(response);
        log.info("======================");

        // 출시 예정 제품 확인
        if (response.contains("출시 예정") ||
                response.contains("출시되지 않은") ||
                response.contains("출시되지 않았습니다")) {
            log.info("출시 예정 제품: " + response);
            throw new RuntimeException("이 제품은 아직 출시되지 않았습니다.");
        }

        // 제품 정보 없음
        if (response.contains("찾을 수 없습니다") ||
                response.contains("존재하지 않")) {
            log.info("제품 정보 없음: " + response);
            throw new RuntimeException("제품 정보를 찾을 수 없습니다. 제품명을 확인해주세요.");
        }

        // 결과를 담을 빈 리스트 생성
        List<PriceData> priceDataList = new ArrayList<>();

        // 정규식(Regular Expression) 패턴 만들기
        // "YYYY-MM: 숫자원" 형태를 찾는 패턴
        // 예: "2024-01: 720000원", "2024-02: 715,000원" 등
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2})\\s*:\\s*([\\d,]+)원?");
        // (\\d{4}-\\d{2}) → 2024-01 같은 날짜 (그룹 1)
        // \\s*:\\s* → 앞뒤 공백 허용하는 콜론
        // ([\\d,]+) → 숫자와 쉼표 (그룹 2)
        // 원? → "원"이 있을 수도 없을 수도

        // Matcher: 패턴을 텍스트에서 찾는 도구
        Matcher matcher = pattern.matcher(response);


        int matchCount = 0;
        // while: 패턴과 일치하는 모든 항목을 찾을 때까지 반복
        while (matcher.find()) {
            matchCount++;

            // 그룹 1: 날짜 추출 (예: "2024-01")
            String month = matcher.group(1);

            // 그룹 2: 가격 추출하고 쉼표 제거 (예: "720,000" → "720000")
            String priceStr = matcher.group(2).replace(",", "");

            log.info("매칭 " + matchCount + ": month=" + month + ", price=" + priceStr);

            try {
                // 문자열을 숫자로 변환
                Double price = Double.parseDouble(priceStr);

                // PriceData 객체 만들어서 리스트에 추가
                priceDataList.add(new PriceData(month, price));

            } catch (NumberFormatException e) {
                // 숫자 변환 실패하면 경고 로그만 찍고 넘어감
                log.warn("가격 파싱 실패: " + priceStr);
            }
        }
        log.info("총 매칭 개수: " + matchCount);

        // 데이터가 없으면 에러
        if (priceDataList.isEmpty()) {
            log.warn("파싱된 가격 데이터가 없습니다. Gemini 응답: " + response);
            throw new RuntimeException("가격 데이터를 파싱할 수 없습니다.");
        }

        // 날짜순으로 정렬 (2023-11, 2023-12, 2024-01...)
        priceDataList.sort((a, b) -> a.getMonth().compareTo(b.getMonth()));

        log.info("파싱된 데이터 개수: " + priceDataList.size() + "개");
        return priceDataList;
    }
}
