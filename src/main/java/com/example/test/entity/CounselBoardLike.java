package com.example.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "counselboard_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "counselboard_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselBoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselboard_id", nullable = false)
    private CounselBoard counselBoard;

    public CounselBoardLike(User user, CounselBoard counselBoard) {
        this.user = user;
        this.counselBoard = counselBoard;
    }
}