package com.example.test.controller;

import com.example.test.dto.GuestHelpDto;
import com.example.test.dto.HelpDto;
import com.example.test.entity.Help;
import com.example.test.entity.User;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.HelpService;
import com.example.test.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/help")
public class HelpController {

    @Autowired
    private HelpService helpService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // 문의 등록
    @PostMapping("/submit")
    public ResponseEntity<?> submitHelp (@Valid @RequestBody HelpDto helpDto, BindingResult bindingResult, @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        String username = null;
    try {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                username = null;
            }
        }

        if (username != null) {
            helpService.createHelpForMember(helpDto, username);
        } else {
            helpService.createHelpForGuest(helpDto);
        }
        return ResponseEntity.ok("문의가 정상적으로 접수되었습니다.");
    } catch (Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 나의 문의
    @GetMapping("/my")
    public ResponseEntity<?> getMyHelp(@RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            List<Help> helps = helpService.getUserHelps(username);
            return ResponseEntity.ok(helps);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 특정 문의 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> helpDetail (@PathVariable("id") Long id) {
        try {
            Help help = helpService.getHelp(id);
            return ResponseEntity.ok(help);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 모든 문의 글 조회
    @GetMapping("/admin/list")
    public ResponseEntity<?> adminHelpList (@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            String role = user.getRole();

            if (!"ROLE_ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }

            List<Help> helps = helpService.getAllHelps();
            return ResponseEntity.ok(helps);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 답변 상태 업데이트
    @PutMapping("/admin/{id}/answer")
    public ResponseEntity<?> updateAnswerSatus (@PathVariable("id") Long id, @RequestHeader("Authorization") String authHeader, @RequestParam boolean isAnswered) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            String role = user.getRole();

            if (!"ROLE_ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }

            helpService.updateAnswerStatus(id, isAnswered);

            return ResponseEntity.ok("답변 상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 문의 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHelp (@PathVariable("id") Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            String role = user.getRole();

            Help help = helpService.getHelp(id);

            boolean isAdmin = "ROLE_ADMIN".equals(role);
            boolean isOwner = false;

            // 회원 문의인 경우
            if (help.getUser() != null) {
                isOwner = help.getUser().getUsername().equals(username);
            }

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }

            return ResponseEntity.ok("문의가 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 비회원 문의 내역 조회
    @PostMapping("/guest/inquiry")
    public ResponseEntity<?> guestInquiry(@Valid @RequestBody GuestHelpDto helpDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("이름과 휴대폰 번호를 정확히 입력해주세요.");
        }
        try {
            List<Help> helps = helpService.getGuestHelps(helpDto.getName(), helpDto.getPhone());

            if (helps.isEmpty()) {
                return ResponseEntity.status(404).body("해당 정보로 등록된 문의가 없습니다.");
            }

            return ResponseEntity.ok(helps);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("조회 중 오류가 발생했습니다.");
        }
    }

    // 비회원 문의 삭제
    @DeleteMapping("/guest/{id}")
    public ResponseEntity<?> deleteGuestHelp(@PathVariable("id") Long id, @RequestBody GuestHelpDto helpDto) {

        try {
            // 본인 확인
            boolean canDelete = helpService.canGuestDeleteHelp(id, helpDto.getName(), helpDto.getPhone());

            if (!canDelete) {
                return ResponseEntity.status(403).body("권한이 없습니다. 이름과 전화번호를 확인해주세요.");
            }

            helpService.deleteHelp(id);
            return ResponseEntity.ok("문의가 삭제되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 중 오류가 발생했습니다.");
        }
    }

}
