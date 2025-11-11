package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CounselBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cTitle;

    @Column(columnDefinition = "TEXT")
    private String cContent;

    @Column(columnDefinition = "LONGTEXT")
    private String cFile;

    @CreationTimestamp
    private LocalDateTime cWriteTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer cView = 0;
    private Integer cLike = 0;

    @OneToMany(mappedBy = "counselBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CounselBoardLike> likes = new ArrayList<>();
}