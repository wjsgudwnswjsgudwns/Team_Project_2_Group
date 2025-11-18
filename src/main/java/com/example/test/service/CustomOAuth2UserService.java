package com.example.test.service;

import com.example.test.entity.User;
import com.example.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Provider로부터 사용자 정보를 받아옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        System.out.println("=== OAuth2 로그인 시작 ===");
        System.out.println("Provider: " + registrationId);
        System.out.println("Attributes: " + attributes);

        // 핵심 정보를 추출
        String provider = registrationId;
        String providerId = null;
        String email = null;
        String name = null;
        String nickname = null;

        if ("naver".equalsIgnoreCase(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response == null) {
                throw new OAuth2AuthenticationException("네이버 사용자 정보를 가져올 수 없습니다.");
            }
            providerId = Objects.toString(response.get("id"), null);
            email = (String) response.get("email");
            name = (String) response.get("name");
            nickname = (String) response.get("nickname");

            if (email == null || email.isEmpty()) {
                email = "naver_" + providerId + "@noemail.local";
            }
        } else if ("google".equalsIgnoreCase(registrationId)) {
            providerId = Objects.toString(attributes.get("sub"), null);
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            nickname = name;

            if (email == null || email.isEmpty()) {
                email = "google_" + providerId + "@noemail.local";
            }
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth provider: " + registrationId);
        }

        if (providerId == null) {
            throw new OAuth2AuthenticationException("providerId를 확인할 수 없습니다.");
        }

        final String finalProvider = provider;
        final String finalProviderId = providerId;
        final String finalEmail = email;
        final String finalName = name;
        final String finalNickname = nickname;

        // provider+providerId 우선 조회
        Optional<User> optUser = userRepository.findByProviderAndProviderId(finalProvider, finalProviderId);
        User user = optUser.orElseGet(() -> {
            System.out.println("새 유저 생성 중...");
            User newUser = new User();
            newUser.setProvider(finalProvider);
            newUser.setProviderId(finalProviderId);
            newUser.setUsername(finalProvider + "_" + finalProviderId);
            newUser.setEmail(finalEmail);

            String resolvedNickname = finalNickname;
            if (resolvedNickname == null || resolvedNickname.isEmpty()) {
                resolvedNickname = finalName;
            }
            if (resolvedNickname == null || resolvedNickname.isEmpty()) {
                resolvedNickname = newUser.getUsername();
            }
            newUser.setNickname(resolvedNickname);

            newUser.setRole("ROLE_USER");

            try {
                User savedUser = userRepository.save(newUser);
                System.out.println("새 유저 저장 완료: " + savedUser.getUsername());
                return savedUser;
            } catch (Exception e) {
                System.err.println("새 유저 저장 실패: " + e.getMessage());
                throw new RuntimeException("새 유저 저장 실패: " + e.getMessage(), e);
            }
        });

        // 이미 있던 유저이면 업데이트
        boolean changed = false;
        if (user.getEmail() == null && finalEmail != null) {
            user.setEmail(finalEmail);
            changed = true;
        }
        if ((user.getNickname() == null || user.getNickname().isEmpty())
                && (finalNickname != null || finalName != null)) {
            String newNickname = finalNickname != null ? finalNickname : finalName;
            if(newNickname != null) {
                user.setNickname(newNickname);
                changed = true;
            }
        }
        if (user.getProvider() == null) {
            user.setProvider(finalProvider);
            changed = true;
        }
        if (user.getProviderId() == null) {
            user.setProviderId(finalProviderId);
            changed = true;
        }
        if (changed) {
            user = userRepository.save(user);
            System.out.println("✅ 기존 유저 정보 업데이트 완료");
        }

        // 핵심 수정: 원본 attributes를 복사하지 않고 완전히 새로운 Map 생성
        // 이렇게 해야 구글의 "sub" 같은 원본 키가 nameAttributeKey를 방해하지 않음
        Map<String, Object> returnedAttrs = new HashMap<>();
        returnedAttrs.put("username", user.getUsername()); // "google_116743118941825001243"
        returnedAttrs.put("email", user.getEmail());
        returnedAttrs.put("nickname", user.getNickname());
        returnedAttrs.put("role", user.getRole());
        returnedAttrs.put("providerId", user.getProviderId());
        returnedAttrs.put("provider", user.getProvider());

        System.out.println("=== returnedAttrs 생성 완료 ===");
        System.out.println("username (저장된 값): " + returnedAttrs.get("username"));
        System.out.println("email: " + returnedAttrs.get("email"));
        System.out.println("nickname: " + returnedAttrs.get("nickname"));
        System.out.println("role: " + returnedAttrs.get("role"));
        System.out.println("============================");

        // nameAttributeKey를 "username"으로 설정하여 principal.getName()이 user.getUsername()을 반환하도록 함
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                returnedAttrs,
                "username" // 이제 principal.getName()은 "google_116743118941825001243"을 반환함
        );
    }
}