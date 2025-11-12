package com.example.test.chatbot;

import lombok.Data;
import java.util.List;

@Data
public class GeminiResponse { // Gemini API로부터 받을 응답 DTO
    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private Content content;
    }

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }
}