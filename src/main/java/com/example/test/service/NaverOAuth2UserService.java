package com.example.test.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.example.test.entity.User;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class NaverOAuth2UserService extends DefaultOAuth2UserService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");

        if (response == null) {
            throw new OAuth2AuthenticationException("네이버 사용자 정보를 가져올 수 없습니다.");
        }

        String providerId = (String) response.get("id");
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String nickname = (String) response.get("nickname");

        User user = saveOrUpdateUser(providerId, email, name, nickname);

        return new DefaultOAuth2User(
                Collections.singleton(() -> user.getRole()),
                Map.of(
                        "id", providerId,
                        "username", user.getUsername()
                ),
                "username"
        );
    }


    private User saveOrUpdateUser(String providerId, String email, String name, String nickname) {
        // provider와 providerId로 사용자 찾기
        Optional<User> existingUser = userRepository.findByProviderAndProviderId("naver", providerId);

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트
            user = existingUser.get();
            user.setEmail(email);
            user.setNickname(nickname != null ? nickname : name);
        } else {
            // 새 사용자 생성
            user = new User();
            user.setProvider("naver");
            user.setProviderId(providerId);
            user.setUsername("naver_" + providerId); // 고유한 username 생성
            user.setEmail(email);
            user.setNickname(nickname != null ? nickname : name);
            // OAuth2 사용자는 password가 null
        }

        return userRepository.save(user);
    }

}