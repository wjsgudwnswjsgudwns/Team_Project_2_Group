package com.example.test.controller;

import com.example.test.dto.MyPageDTO;
import com.example.test.dto.UserUpdateDTO;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.MyPageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 비밀번호 확인 (본인 인증)
     * POST /api/mypage/verify-password
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String username = extractUsername(authHeader);
        String password = body.get("password");

        try {
            boolean isValid = myPageService.verifyPassword(username, password);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "인증 성공"
                ));
            } else {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "비밀번호가 일치하지 않습니다."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 마이페이지 정보 조회
     * GET /api/mypage/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> getMyPageInfo(@RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);

        try {
            MyPageDTO dto = myPageService.getMyPageInfo(username);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 회원정보 수정
     * PUT /api/mypage/update
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateDTO dto) {

        String username = extractUsername(authHeader);

        try {
            myPageService.updateUserInfo(username, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원정보가 수정되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 회원 탈퇴
     * DELETE /api/mypage/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String username = extractUsername(authHeader);
        String password = body.get("password");

        try {
            myPageService.deleteAccount(username, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원 탈퇴가 완료되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * Authorization 헤더에서 username 추출
     */
    private String extractUsername(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }
}