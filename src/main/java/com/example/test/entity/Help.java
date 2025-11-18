package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Help {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문의 제목
    private String title;

    // 문의 내용
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정으로 성능 최적화
    @JoinColumn(name = "user_id")
    private User user; // 유저 정보

    // 비회원 문의 정보
    private String name;        // 이름
    private String email;       // 이메일
    private String phone;       // 휴대폰 번호

    @CreationTimestamp
    private LocalDateTime inquiryDate; // 문의 접수 시간
    private boolean isAnswered = false; // 답변 완료 여부 (기본값: false)

    @OneToOne(mappedBy = "help", cascade = CascadeType.ALL, orphanRemoval = true)
    private HelpAnswer helpAnswer;
}
