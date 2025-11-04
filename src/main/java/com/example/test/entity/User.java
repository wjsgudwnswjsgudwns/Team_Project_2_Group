    package com.example.test.entity;

    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    import org.hibernate.annotations.CreationTimestamp;

    import java.time.LocalDateTime;

    @Entity
    @Getter
    @Setter
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true)
        private String username; // 아이디

        private String password; // 비밀번호

        @Column(unique = true)
        private String nickname; // 닉네임

        private String email; // 이메일

        @CreationTimestamp
        private LocalDateTime createAccount; // 계정 생성일

        private String role; // 역할

        // OAuth2 제공자 (naver, google 등)
        private String provider;

        // OAuth2 제공자의 고유 ID
        private String providerId;

    }
