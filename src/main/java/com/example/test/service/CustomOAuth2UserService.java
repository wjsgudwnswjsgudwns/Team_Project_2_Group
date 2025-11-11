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
            OAuth2User oAuth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "naver" or "google"
            Map<String, Object> attributes = oAuth2User.getAttributes();

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
                email = (String) response.get("email"); // null 가능
                name = (String) response.get("name");
                nickname = (String) response.get("nickname");

                if (email == null || email.isEmpty()) {
                    email = "naver_" + providerId + "@noemail.local"; // fallback
                }
            } else if ("google".equalsIgnoreCase(registrationId)) {
                providerId = Objects.toString(attributes.get("sub"), null);
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");

                if (email == null || email.isEmpty()) {
                    throw new OAuth2AuthenticationException("구글에서 이메일을 제공하지 않았습니다.");
                }
            } else {
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth provider: " + registrationId);
            }

            if (providerId == null) {
                throw new OAuth2AuthenticationException("providerId를 확인할 수 없습니다.");
            }

            // --- 여기서부터가 핵심: 람다에서 참조할 final 복사본 생성 ---
            final String finalProvider = provider;
            final String finalProviderId = providerId;
            final String finalEmail = email;
            final String finalName = name;
            final String finalNickname = nickname;

            // provider+providerId 우선 조회, 없으면 이메일로 시도
            Optional<User> optUser = userRepository.findByProviderAndProviderId(finalProvider, finalProviderId);
            User user = optUser.orElseGet(() -> {
                // 이 블록에서 final 변수들만 사용 -> 람다 규칙 위반 없음
                User newUser = new User();
                newUser.setProvider(finalProvider);
                newUser.setProviderId(finalProviderId);
                newUser.setUsername(finalProvider + "_" + finalProviderId);
                newUser.setEmail(finalEmail);
                newUser.setNickname(finalNickname != null ? finalNickname : (finalName != null ? finalName : newUser.getUsername()));
                newUser.setRole("ROLE_USER");
                return userRepository.save(newUser);
            });

            // 이미 있던 유저이면 업데이트 (선택 사항)
            boolean changed = false;
            if (user.getEmail() == null && finalEmail != null) {
                user.setEmail(finalEmail);
                changed = true;
            }
            if ((user.getNickname() == null || user.getNickname().isEmpty())
                    && (finalNickname != null || finalName != null)) {
                user.setNickname(finalNickname != null ? finalNickname : finalName);
                changed = true;
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
            }

            // DefaultOAuth2User에 전달할 attributes 구성
            Map<String, Object> returnedAttrs = new HashMap<>();
            returnedAttrs.put("id", user.getProviderId());
            returnedAttrs.put("username", user.getUsername());
            returnedAttrs.put("email", user.getEmail());
            returnedAttrs.put("nickname", user.getNickname());

            // nameAttributeKey: 네이버의 경우 우리가 만든 username 사용, 구글은 email 사용해도 되지만
            // 통일을 위해 여기서는 "username"을 사용.
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                    returnedAttrs,
                    "username"
            );
        }
    }