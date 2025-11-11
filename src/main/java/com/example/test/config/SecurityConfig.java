package com.example.test.config;

import java.util.List;
import java.util.Map;

import com.example.test.entity.User;
import com.example.test.jwt.JwtAuthenticationFilter;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.CustomOAuth2UserService;
import com.example.test.service.UserService;
import com.example.test.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(
            JwtAuthenticationFilter authenticationFilter,
            CustomOAuth2UserService oAuth2UserService,
            JwtUtil jwtUtil,
            UserService userService,
            UserRepository userRepository
    ) {
        this.authenticationFilter = authenticationFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;

        System.out.println("🔧 SecurityConfig 생성자 호출");
        System.out.println("🔧 oAuth2UserService: " + oAuth2UserService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/ai/**",
                                "/api/image/**",
                                "/api/price/**",
                                "/api/products/**",
                                "/api/cart/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> {
                            System.out.println("🔍 userInfoEndpoint 설정 중...");
                            System.out.println("🔍 oAuth2UserService: " + oAuth2UserService);
                            userInfo.userService(oAuth2UserService);
                        })
                        .successHandler((request, response, authentication) -> {
                            try {
                                var principal = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();

                                System.out.println("=== OAuth2 Success Handler ===");
                                System.out.println("Principal name (getName()): " + principal.getName());
                                System.out.println("All Attributes keys: " + principal.getAttributes().keySet());

                                // ✅ CustomOAuth2UserService에서 이미 처리된 경우 (attributes에 username이 있음)
                                if (principal.getAttributes().containsKey("username")) {
                                    System.out.println("✅ CustomOAuth2UserService에서 처리된 사용자");
                                    String username = (String) principal.getAttributes().get("username");

                                    // DB에서 username으로 조회
                                    User user = userRepository.findByUsername(username)
                                            .orElseThrow(() -> new RuntimeException("DB에서 사용자를 찾을 수 없습니다: " + username));

                                    System.out.println("✅ DB 조회 성공:");
                                    System.out.println("  - username: " + user.getUsername());
                                    System.out.println("  - role: " + user.getRole());
                                    System.out.println("  - email: " + user.getEmail());

                                    // JWT 생성
                                    String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                                    System.out.println("✅ JWT 생성 완료: " + token.substring(0, 20) + "...");

                                    response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
                                    return;
                                }

                                // ✅ CustomOAuth2UserService를 거치지 않은 경우 (구글 등)
                                System.out.println("✅ successHandler에서 직접 처리");

                                // providerId와 provider 추출
                                String tempProviderId = principal.getName();
                                String tempRegistrationId = null;
                                String tempEmail = null;
                                String tempName = null;

                                // OAuth2 제공자 식별
                                Object issuerObj = principal.getAttributes().get("iss");
                                String issuer = issuerObj != null ? issuerObj.toString() : null;

                                if (issuer != null && issuer.contains("google")) {
                                    tempRegistrationId = "google";
                                    tempEmail = (String) principal.getAttributes().get("email");
                                    tempName = (String) principal.getAttributes().get("name");
                                } else if (principal.getAttributes().containsKey("response")) {
                                    tempRegistrationId = "naver";
                                    Map<String, Object> naverResponse = (Map<String, Object>) principal.getAttributes().get("response");
                                    tempProviderId = String.valueOf(naverResponse.get("id"));
                                    tempEmail = (String) naverResponse.get("email");
                                    tempName = (String) naverResponse.get("name");
                                }

                                if (tempRegistrationId == null) {
                                    throw new RuntimeException("OAuth2 제공자를 식별할 수 없습니다.");
                                }

                                final String providerId = tempProviderId;
                                final String registrationId = tempRegistrationId;
                                final String email = tempEmail;
                                final String name = tempName;

                                System.out.println("✅ providerId 추출: " + providerId);
                                System.out.println("✅ provider 식별: " + registrationId);
                                System.out.println("✅ email: " + email);
                                System.out.println("✅ name: " + name);

                                // ✅ DB에서 사용자 조회 또는 생성
                                User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                                        .orElseGet(() -> {
                                            System.out.println("🆕 새 사용자 생성 중...");
                                            User newUser = new User();
                                            newUser.setProvider(registrationId);
                                            newUser.setProviderId(providerId);
                                            newUser.setUsername(registrationId + "_" + providerId);
                                            newUser.setEmail(email != null ? email : registrationId + "_" + providerId + "@noemail.local");
                                            newUser.setNickname(name != null ? name : registrationId + "_" + providerId);
                                            newUser.setRole("ROLE_USER");

                                            User savedUser = userRepository.save(newUser);
                                            System.out.println("✅ 새 사용자 저장 완료: " + savedUser.getUsername());
                                            return savedUser;
                                        });

                                System.out.println("✅ DB 조회/생성 성공:");
                                System.out.println("  - username: " + user.getUsername());
                                System.out.println("  - role: " + user.getRole());
                                System.out.println("  - email: " + user.getEmail());

                                // ✅ JWT 생성
                                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                                System.out.println("✅ JWT 생성 완료: " + token.substring(0, 20) + "...");

                                response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);

                            } catch (Exception e) {
                                System.err.println("❌ JWT 발급/리다이렉트 중 오류 발생: " + e.getMessage());
                                e.printStackTrace();
                                response.sendRedirect("http://localhost:3000/login?error=internal_oauth_error");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            System.err.println("❌ OAuth2 로그인 실패: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("http://localhost:3000/login?error=oauth2");
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}"
                            );
                        })
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}