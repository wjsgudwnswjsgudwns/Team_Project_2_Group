package com.example.test.chatbot;

import lombok.Data;

@Data
public class ChatRequest { // 사용자로부터 받을 요청 DTO
    private String message; // 질문
}
