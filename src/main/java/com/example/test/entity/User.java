package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String nickname;

    private String email;

    @CreationTimestamp
    private LocalDateTime createAccount;

    // OAuth2 제공자 (naver, google 등)
    private String provider;

    // OAuth2 제공자의 고유 ID
    private String providerId;

}
