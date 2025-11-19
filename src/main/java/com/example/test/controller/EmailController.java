package com.example.test.controller;

import com.example.test.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * 이메일 인증 코드 발송
     * POST /api/email/send-code
     */
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String purpose = body.get("purpose"); // "SIGNUP", "FIND_ID", "FIND_PASSWORD", "CHANGE_EMAIL"

        try {
            emailService.sendVerificationEmail(email, purpose);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증 코드가 발송되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "이메일 발송 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 인증 코드 확인
     * POST /api/email/verify-code
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String purpose = body.get("purpose");

        boolean isValid = emailService.verifyCode(email, code, purpose);

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증이 완료되었습니다."
            ));
        } else {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "인증 코드가 올바르지 않거나 만료되었습니다."
            ));
        }
    }

}