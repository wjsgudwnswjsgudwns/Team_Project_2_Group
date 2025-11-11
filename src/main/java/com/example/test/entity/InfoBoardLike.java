package com.example.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "infoboard_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "infoboard_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoBoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infoboard_id", nullable = false)
    private InfoBoard infoBoard;

    public InfoBoardLike(User user, InfoBoard infoBoard) {
        this.user = user;
        this.infoBoard = infoBoard;
    }
}