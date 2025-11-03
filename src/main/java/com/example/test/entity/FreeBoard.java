package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FreeBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fTitle;

    @Column(columnDefinition = "TEXT")
    private String fContent;

    private String fFile; // 파일 경로 저장

    @CreationTimestamp
    private LocalDateTime fWriteTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user; // fNickname 대신 User 참조

    private Integer fView = 0;
    private Integer fLike = 0;

//    @OneToMany(mappedBy = "freeBoard", cascade = CascadeType.ALL)
//    private List<Comment> fComments = new ArrayList<>();
}
