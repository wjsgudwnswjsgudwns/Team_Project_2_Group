package com.example.test.repository;

import java.util.Optional;

import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디로 찾기
	public Optional<User> findByUsername(String username);

    // 닉네임으로 찾기
    public Optional<User> findByNickname(String nickname);

    // OAuth2용 메서드 추가
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
