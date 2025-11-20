package com.example.test.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 정적 리소스 경로 추가 (가장 먼저 체크)
    private static final List<String> STATIC_RESOURCES = List.of(
            "/",
            "/index.html",
            "/favicon.ico",
            "/manifest.json",
            "/robots.txt",
            "/static/",
            "/assets/"
    );

    // SecurityConfig와 동일한 경로 설정
    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/me",
            "/api/account",
            "/api/email",
            "/oauth2",
            "/login/oauth2"
    );

    // permitAll 경로 (인증 불필요)
    private static final List<String> PERMIT_ALL_URLS = List.of(
            "/api/ai",
            "/api/image",
            "/api/price",
            "/api/products",
            "/api/cart",
            "/api/chat",
            "/api/user/profile",
            "/api/home2/recent",
            "/api/help/submit",
            "/api/help/guest"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("🔍 JWT 필터 체크: " + method + " " + requestURI);

        // ✅ 1. 정적 리소스 체크 (최우선)
        boolean isStaticResource = STATIC_RESOURCES.stream()
                .anyMatch(uri -> requestURI.equals(uri) || requestURI.startsWith(uri))
                || requestURI.endsWith(".js")
                || requestURI.endsWith(".css")
                || requestURI.endsWith(".png")
                || requestURI.endsWith(".jpg")
                || requestURI.endsWith(".svg")
                || requestURI.endsWith(".ico")
                || requestURI.endsWith(".json")
                || requestURI.endsWith(".woff")
                || requestURI.endsWith(".woff2")
                || requestURI.endsWith(".ttf");

        if (isStaticResource) {
            System.out.println("✅ 정적 리소스 - 필터 건너뜀");
            return true;
        }

        // ✅ 2. EXCLUDE_URLS: 완전히 필터를 건너뜀
        boolean isExcluded = EXCLUDE_URLS.stream()
                .anyMatch(uri -> requestURI.equals(uri) || requestURI.startsWith(uri + "/"));

        if (isExcluded) {
            System.out.println("✅ EXCLUDE_URLS - 필터 건너뜀");
            return true;
        }

        // ✅ 3. PERMIT_ALL_URLS: 필터를 건너뜀
        boolean isPermitAll = PERMIT_ALL_URLS.stream()
                .anyMatch(uri -> requestURI.startsWith(uri));

        if (isPermitAll) {
            System.out.println("✅ PERMIT_ALL_URLS - 필터 건너뜀");
            return true;
        }

        // ✅ 4. 게시판 GET 요청은 필터 건너뜀
        if ("GET".equals(method) && (
                requestURI.startsWith("/api/freeboard") ||
                        requestURI.startsWith("/api/counselboard") ||
                        requestURI.startsWith("/api/infoboard") ||
                        requestURI.startsWith("/api/help/")
        )) {
            System.out.println("✅ 게시판 GET 요청 - 필터 건너뜀");
            return true;
        }

        System.out.println("🔐 JWT 필터 실행 대상");
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseJwt(request);

        System.out.println("🔍 토큰 존재 여부: " + (token != null));

        if (token != null && !token.isEmpty()) {
            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                System.out.println("✅ JWT 인증 성공: " + username + " (" + role + ")");

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (Exception e) {
                System.out.println("❌ JWT 인증 실패: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ 토큰이 없습니다 - 익명 사용자로 처리");
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}