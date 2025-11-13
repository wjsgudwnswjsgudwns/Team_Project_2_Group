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

        // ✅ SecurityConfig와 동일한 경로 설정
        private static final List<String> EXCLUDE_URLS = List.of(
                "/api/auth/login",
                "/api/auth/signup",
                "/oauth2",
                "/login/oauth2"
        );

        // ✅ permitAll 경로 (인증 불필요)
        private static final List<String> PERMIT_ALL_URLS = List.of(
                "/api/ai",
                "/api/image",
                "/api/price",
                "/api/products",
                "/api/chat",
                "/api/freeboard",
                "/api/counselboard",
                "/api/infoboard"
        );

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String requestURI = request.getRequestURI();

            System.out.println("🔍 JWT 필터 체크: " + requestURI);

            // EXCLUDE_URLS: 완전히 필터를 건너뜀
            boolean isExcluded = EXCLUDE_URLS.stream()
                    .anyMatch(uri -> requestURI.equals(uri) || requestURI.startsWith(uri + "/"));

            // PERMIT_ALL_URLS: 필터를 건너뜀
            boolean isPermitAll = PERMIT_ALL_URLS.stream()
                    .anyMatch(uri -> requestURI.startsWith(uri));

            boolean shouldSkip = isExcluded || isPermitAll;
            System.out.println("🔍 필터 건너뛰기 여부: " + shouldSkip);

            return isExcluded || isPermitAll;
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

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                } catch (Exception e) {
                    System.out.println("JWT 인증 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ 토큰이 없습니다");  // ← 추가
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