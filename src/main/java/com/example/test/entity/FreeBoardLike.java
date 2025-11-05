package com.example.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "freeboard_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "freeboard_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FreeBoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_id", nullable = false)
    private FreeBoard freeBoard;

    public FreeBoardLike(User user, FreeBoard freeBoard) {
        this.user = user;
        this.freeBoard = freeBoard;
    }
}