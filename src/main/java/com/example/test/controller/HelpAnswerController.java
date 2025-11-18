package com.example.test.controller;

import com.example.test.dto.HelpAnswerDto;
import com.example.test.entity.User;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.HelpAnswerService;
import com.example.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/help")
public class HelpAnswerController {

    @Autowired
    private HelpAnswerService helpAnswerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // 답변 작성
    @PostMapping("/{id}/answer")
    public ResponseEntity<?> createAnswer(@PathVariable("id") Long helpId,
                                          @RequestBody HelpAnswerDto dto,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userService.getUserByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!"ROLE_ADMIN".equals(user.getRole())) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }

            helpAnswerService.createAnswer(helpId, dto, username);
            return ResponseEntity.ok("답변이 등록되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 답변 삭제
    @DeleteMapping("/{id}/answer")
    public ResponseEntity<?> deleteAnswer(@PathVariable("id") Long helpId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userService.getUserByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!"ROLE_ADMIN".equals(user.getRole())) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }

            helpAnswerService.deleteAnswer(helpId);
            return ResponseEntity.ok("답변이 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}