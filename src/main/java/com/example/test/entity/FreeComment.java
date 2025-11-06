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
public class FreeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fCommentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_id", nullable = false)
    private FreeBoard freeBoard;

    // 대댓글을 위한 부모 댓글 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FreeComment parent;

    // 자식 댓글들
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeComment> children = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime fCommentWriteTime;

    private LocalDateTime fCommentUpdateTime;

    // 삭제 여부 (soft delete)
    private boolean fCommentDeleted = false;

    @PreUpdate
    public void preUpdate() {
        this.fCommentUpdateTime = LocalDateTime.now();
    }
}