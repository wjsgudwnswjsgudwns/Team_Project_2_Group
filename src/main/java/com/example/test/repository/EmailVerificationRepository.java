package com.example.test.repository;

import com.example.test.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndVerificationCodeAndPurpose(
            String email, String verificationCode, String purpose
    );

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email, String purpose
    );

    // 만료된 인증 코드 삭제
    void deleteByExpiresAtBefore(LocalDateTime now);
}