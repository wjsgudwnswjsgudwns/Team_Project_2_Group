package com.example.test.chartai;

import lombok.Data;

import java.util.List;

@Data
public class AiPriceRequest {

    // contents: Gemini에게 보낼 메시지 목록 (보통 하나만 보냄)
    private List<Content> contents;

    @Data
    public static class Content {

        // 우리는 텍스트만 사용할 예정
        private List<Part> parts;   // parts: 메시지의 여러 부분들 (텍스트, 이미지 등)

        // Part: 메시지의 한 조각 (우리는 텍스트만 사용)
        @Data
        public static class Part {
            private String text;
            // text: 실제로 Gemini에게 보낼 질문이나 명령
            // 예: "RTX 4070의 가격을 알려줘"
        }
    }
}
