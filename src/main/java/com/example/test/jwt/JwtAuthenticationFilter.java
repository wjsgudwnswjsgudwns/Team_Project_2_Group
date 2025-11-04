package com.example.test.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	//요청이 들어올때 마다 한번씩 실행되는 필터->요청이 들어올때 마다 JWT 검증을 수행
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		 
		String token = parseJwt(request);
		
		if (token != null && !token.isEmpty()) {
			try {
				String username = jwtUtil.extractUsername(token); //토큰을 사용하는 사용자 이름
				
				//토큰 인증 객체 생성
				UsernamePasswordAuthenticationToken authenticationToken = 
						new UsernamePasswordAuthenticationToken(username, null, null);
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				//토큰 인증 객체에 추가로 사용자 정보를 담기 -> 클라이언트의 ip주소, 세션 id
				
				//Security에 토큰 인증 객체를 저장->스프링 시큐리티에게 인증 정보를 제공
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);				
				
			} catch (Exception e) {
				System.out.println("JWT 인증 실패:" + e.getMessage());
			}
		}	
		//요청->인증 정보 확인->컨트롤러 전달 or 다음 필터
		filterChain.doFilter(request, response);
		
	}
	
	//request 객체 내의 헤더에서 token을 추출
	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}
		
		return null;
	}



}