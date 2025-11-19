package com.example.test.controller;

import com.example.test.dto.FindAccountDto;
import com.example.test.entity.User;
import com.example.test.service.EmailService;
import com.example.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class FindAccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============= 아이디 찾기 =============

    // 1단계: 이메일 확인 및 인증 코드 전송
    @PostMapping("/find-username/send-code")
    public ResponseEntity<?> sendUsernameVerificationCode(@RequestBody FindAccountDto dto) {
        try {
            Optional<User> user = userService.getUserByEmail(dto.getEmail());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "해당 이메일로 가입된 계정이 없습니다.")
                );
            }

            // OAuth2 사용자 체크
            if (user.get().getProvider() != null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", user.get().getProvider() + " 소셜 로그인 계정입니다.")
                );
            }

            emailService.sendUsernameVerificationCode(dto.getEmail());

            return ResponseEntity.ok(
                    Map.of("message", "인증 코드를 이메일로 전송했습니다.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "인증 코드 전송 중 오류가 발생했습니다.")
            );
        }
    }

    // 2단계: 인증 코드 확인 및 아이디 반환
    @PostMapping("/find-username/verify-code")
    public ResponseEntity<?> verifyUsernameCode(@RequestBody FindAccountDto dto) {
        try {
            Optional<User> user = userService.getUserByEmail(dto.getEmail());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "사용자를 찾을 수 없습니다.")
                );
            }

            // 인증 코드 검증
            if (!emailService.verifyCodeForUsername(dto.getEmail(), dto.getVerificationCode(), user.get().getUsername())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "인증 코드가 올바르지 않거나 만료되었습니다.")
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "인증이 완료되었습니다.",
                            "username", user.get().getUsername()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "인증 코드 확인 중 오류가 발생했습니다.")
            );
        }
    }

    // ============= 비밀번호 찾기 =============

    // 1단계: 이메일/아이디 확인 및 인증 코드 전송
    @PostMapping("/find-password/send-code")
    public ResponseEntity<?> sendPasswordResetCode(@RequestBody FindAccountDto dto) {
        try {
            System.out.println("비밀번호 찾기 요청 - username: " + dto.getUsername() + ", email: " + dto.getEmail());

            if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "아이디를 입력해주세요.")
                );
            }

            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "이메일을 입력해주세요.")
                );
            }

            Optional<User> user = userService.getUserByUsername(dto.getUsername());

            if (user.isEmpty()) {
                System.out.println("사용자를 찾을 수 없음: " + dto.getUsername());
                return ResponseEntity.status(404).body(
                        Map.of("error", "존재하지 않는 아이디입니다.")
                );
            }

            System.out.println("사용자 찾음: " + user.get().getUsername() + ", email: " + user.get().getEmail());

            if (user.get().getEmail() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "이메일 정보가 없는 계정입니다.")
                );
            }

            if (!user.get().getEmail().equals(dto.getEmail())) {
                System.out.println("이메일 불일치 - 입력: " + dto.getEmail() + ", DB: " + user.get().getEmail());
                return ResponseEntity.badRequest().body(
                        Map.of("error", "아이디와 이메일이 일치하지 않습니다.")
                );
            }

            // OAuth2 사용자 체크
            if (user.get().getProvider() != null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", user.get().getProvider() + " 소셜 로그인 계정입니다.")
                );
            }

            System.out.println("인증 코드 전송 시도: " + dto.getEmail());
            emailService.sendPasswordResetVerificationCode(dto.getEmail());
            System.out.println("인증 코드 전송 완료");

            return ResponseEntity.ok(
                    Map.of("message", "인증 코드를 이메일로 전송했습니다.")
            );

        } catch (Exception e) {
            System.err.println("비밀번호 찾기 에러 발생:");
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("error", "인증 코드 전송 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    // 2단계: 인증 코드 확인
    @PostMapping("/find-password/verify-code")
    public ResponseEntity<?> verifyPasswordResetCode(@RequestBody FindAccountDto dto) {
        try {
            Optional<User> user = userService.getUserByUsername(dto.getUsername());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "사용자를 찾을 수 없습니다.")
                );
            }

            if (!user.get().getEmail().equals(dto.getEmail())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "아이디와 이메일이 일치하지 않습니다.")
                );
            }

            // 인증 코드 검증
            if (!emailService.verifyCodeForPassword(dto.getEmail(), dto.getVerificationCode(), dto.getUsername())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "인증 코드가 올바르지 않거나 만료되었습니다.")
                );
            }

            return ResponseEntity.ok(
                    Map.of("message", "인증이 완료되었습니다. 새 비밀번호를 설정해주세요.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "인증 코드 확인 중 오류가 발생했습니다.")
            );
        }
    }

    // 3단계: 새 비밀번호 설정
    @PostMapping("/find-password/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody FindAccountDto dto) {
        try {
            // 이메일 인증 완료 여부 확인
            if (!emailService.isEmailVerified2(dto.getEmail(), dto.getUsername())) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "이메일 인증이 완료되지 않았습니다.")
                );
            }

            Optional<User> userOpt = userService.getUserByUsername(dto.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "사용자를 찾을 수 없습니다.")
                );
            }

            User user = userOpt.get();

            // 이메일 일치 확인
            if (!user.getEmail().equals(dto.getEmail())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "아이디와 이메일이 일치하지 않습니다.")
                );
            }

            // 비밀번호 유효성 검사
            String password = dto.getNewPassword();

            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "비밀번호는 8자 이상이어야 합니다.")
                );
            }

            boolean hasLetter = password.matches(".*[A-Za-z].*");
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");

            if (!(hasLetter && hasDigit && hasSpecial)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                );
            }

            if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                );
            }

            // 비밀번호 변경
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userService.signup(user); // save

            // 인증 상태 제거
            emailService.clearVerification(dto.getEmail());

            return ResponseEntity.ok(
                    Map.of("message", "비밀번호가 성공적으로 변경되었습니다.")
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("error", "비밀번호 재설정 중 오류가 발생했습니다.")
            );
        }
    }
}