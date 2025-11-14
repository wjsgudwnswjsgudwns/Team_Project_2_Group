package com.example.test.controller;

import java.util.HashMap;
import java.util.Map;

import com.example.test.dto.UserDto;
import com.example.test.entity.User;
import com.example.test.jwt.JwtUtil;
import com.example.test.repository.UserRepository;
import com.example.test.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    //아이디 중복 확인

    //로그인
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        System.out.println("인증시작!");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        System.out.println("인증끝!");

        User user = userService.getUser(username).orElseThrow();
        String token = jwtUtil.generateToken(username, user.getRole());

        return Map.of("token", token, "role", user.getRole());
    }

    // 사용자 정보 - role 추가
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userService.getUser(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 인증 정보가 유효하지 않습니다.");
        }

        String safeNickname = user.getNickname();
        if (safeNickname == null || safeNickname.trim().isEmpty()) {
            safeNickname = user.getUsername();
        }

        // ✅ role 정보를 포함한 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("nickname", safeNickname);
        response.put("email", user.getEmail());
        response.put("role", user.getRole()); // 추가

        return ResponseEntity.ok(response);
    }

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup (@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    err -> {
                        errors.put(err.getField(), err.getDefaultMessage());
                    }
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        String password = userDto.getPassword();

        // 영문, 숫자, 특수문자 각각 포함 여부 확인
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");

        // 조건 불충족 시 오류 반환
        if (!(hasLetter && hasDigit && hasSpecial)) {
            return ResponseEntity.badRequest().body(
                    Map.of("passwordComplexity", "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
            );
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body(
                    Map.of("passwordLengthError", "비밀번호는 8자 이상이어야 합니다.")
            );
        }

        if(!userDto.getPassword().equals(userDto.getPasswordCheck())) {
            Map<String, String> error = new HashMap<>();
            error.put("passwordNotSame", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(error);
        }

        if(userService.isUsernameDuplicated(userDto.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("userIdDuplicated", "이미 사용중인 아이디입니다.");
            return ResponseEntity.badRequest().body(error);
        }

        if(userService.isNicknameDuplicated(userDto.getNickname())) {
            Map<String, String> error = new HashMap<>();
            error.put("nicknameDuplicated", "이미 사용중인 닉네임입니다.");
            return ResponseEntity.badRequest().body(error);
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setNickname(userDto.getNickname());
        user.setEmail(userDto.getEmail());

        if ("admin1".equals(userDto.getUsername()) || "admin2".equals(userDto.getUsername())) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }

        userService.signup(user);

        return ResponseEntity.ok(user);
    }
}