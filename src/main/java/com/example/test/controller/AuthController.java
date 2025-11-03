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

    //로그인
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password"); //평문->암호화
        System.out.println("인증시작!");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        System.out.println("인증끝!");
        String token = jwtUtil.generateToken(username); //토큰 생성
        return Map.of("token", token);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userService.getUser(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 인증 정보가 유효하지 않습니다.");
        }

        String safeNickname = user.getNickname();
        if (safeNickname == null || safeNickname.trim().isEmpty()) {
            safeNickname = user.getUsername(); // 닉네임이 없으면 아이디(username)를 대신 사용
        }

        UserDto userDto = new UserDto(
                user.getUsername(),
                null,
                null,
                safeNickname, // 유효성이 보장된 닉네임(또는 username)
                user.getEmail()
        );

        return ResponseEntity.ok(userDto);
    }

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup (@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {

        // Spring Validation 결과 처리
        if(bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    err -> {
                        errors.put(err.getField(), err.getDefaultMessage());
                    }
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        // 비밀번호, 비밀번호 확인 일치 확인
        if(!userDto.getPassword().equals(userDto.getPasswordCheck())) {
            Map<String, String> error = new HashMap<>();
            error.put("passwordNotSame", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(error);
        }

        if(userDto.getPassword().length() < 8 || userDto.getPassword().length() > 16) {
            Map<String, String> error = new HashMap<>();
            error.put("passwordLengthError", "비밀번호는 8자 이상 16자 이하 입니다.");
            return ResponseEntity.badRequest().body(error);
        }

        // 아이디 중복 확인
        if(userService.isUsernameDuplicated(userDto.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("userIdDuplicated", "이미 사용중인 아이디입니다.");
            return ResponseEntity.badRequest().body(error);
        }

        // 아이디 중복 확인
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

        userService.signup(user);

        return ResponseEntity.ok(user);
    }





}