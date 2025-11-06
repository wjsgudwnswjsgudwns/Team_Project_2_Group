package com.example.test.config;

import java.util.List;

import com.example.test.entity.User;
import com.example.test.jwt.JwtAuthenticationFilter;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.NaverOAuth2UserService;
import com.example.test.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private NaverOAuth2UserService naverOAuth2UserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/image/**").permitAll()  // 이미지 검색 API
                        .requestMatchers("/api/price/**").permitAll()  // ✅ 가격 비교 API 추가

                        // ⭐ 댓글 관련 경로 수정
                        .requestMatchers(HttpMethod.GET, "/api/freeboard/**").permitAll() // 게시글/댓글 조회는 누구나
                        .requestMatchers(HttpMethod.POST, "/api/freeboard/*/comments").authenticated() // 댓글 작성은 인증 필요
                        .requestMatchers(HttpMethod.PUT, "/api/freeboard/*/comments/*").authenticated() // 댓글 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/freeboard/*/comments/*").authenticated() // 댓글 삭제
                        .requestMatchers("/api/freeboard/**").authenticated() // 나머지 게시판 기능

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(naverOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();
                            User user = userService.getUser(username).orElseThrow();
                            String token = jwtUtil.generateToken(username, user.getRole());
                            response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
                        })
                )
                // ✅ 추가: JWT 인증 실패 시 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"JWT token expired or invalid\"}");
                            }
                        })
                )
                // ✅ 추가: 세션 사용 안 함 (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}