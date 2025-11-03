package com.example.test.repository;

import java.util.Optional;

import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	public Optional<User> findByUsername(String username);

    public Optional<User> findByNickname(String nickname);
}
