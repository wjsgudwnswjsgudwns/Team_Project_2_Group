package com.example.test.controller;

import java.util.Map;

import com.example.test.entity.User;
import com.example.test.jwt.JwtUtil;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    //회원가입
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody Map<String, String> body) {
        System.out.println("회원가입요청!!");
        String username = body.get("username");
        String password = passwordEncoder.encode(body.get("password")); //평문->암호화
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user); //회원가입완료
        return Map.of("message", "회원가입 성공!");
    }

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
    public Map<String, String> me(@RequestHeader("Authorization") String authHeader) {
        System.out.println("로그인 사용자 정보 요청");
        String token = authHeader.replace("Bearer ", ""); //헤더에서 토큰 정보만 추출
        String username = jwtUtil.extractUsername(token);

        return Map.of("username", username);
    }





}