package com.example.test.config;

import java.util.List;

import com.example.test.entity.User;
import com.example.test.jwt.JwtAuthenticationFilter;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.CustomOAuth2UserService;
import com.example.test.service.UserService;
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

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private CustomOAuth2UserService oAuth2UserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

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
                // ✅ 세션 사용하지 않음 (JWT)
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
                                "/api/products/**"

                        ).permitAll()
                        .requestMatchers("/api/admin/**", "/api/cart/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            var principal = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                            String email = (String) principal.getAttributes().get("email");

                            User user = userService.getUserByEmail(email)
                                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

                            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                            response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("http://localhost:3000/login?error=oauth2");
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // ✅ 핵심: 인증 실패 시 401만 반환 (리다이렉트 절대 안함)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 항상 401 반환 (리다이렉트 하지 않음)
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