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
public class InfoBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String iTitle;

    @Column(columnDefinition = "TEXT")
    private String iContent;

    @Column(columnDefinition = "LONGTEXT")
    private String iFile;

    @CreationTimestamp
    private LocalDateTime iWriteTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer iView = 0;
    private Integer iLike = 0;

    @OneToMany(mappedBy = "infoBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfoBoardLike> likes = new ArrayList<>();
}