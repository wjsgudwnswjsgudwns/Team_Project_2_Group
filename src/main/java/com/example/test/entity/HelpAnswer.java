package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class HelpAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String answer; // 문의 답변

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "help_id")
    private Help help; // 문의

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin; // 관리자

    @CreationTimestamp
    private LocalDateTime answeredDate; // 답변 작성 시간

}
