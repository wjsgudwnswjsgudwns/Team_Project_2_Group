package com.example.test.chartai;

import lombok.Data;

import java.util.List;

@Data
public class AiPriceResponse {

    // candidates: Gemini가 생성한 답변 후보들
    // 보통 1개가 오지만, 여러 개의 답변을 생성할 수도 있음
    private List<Candidate> candidates;


    @Data
    public static class Candidate { // Candidate: 하나의 답변 후보

        private Content content;    // content: 실제 답변 내용

        @Data
        public static class Content {   //Content: 답변의 내용물

            private List<Part> parts;   // parts: 답변의 여러 부분들

            @Data
            public static class Part {  // Part: 답변의 한 조각 (텍스트)
                private String text;    // text: Gemini가 생성한 실제 텍스트 답변
                                        // 예: "2024-01: 720000원, 2024-02: 715000원..."
            }
        }
    }
}