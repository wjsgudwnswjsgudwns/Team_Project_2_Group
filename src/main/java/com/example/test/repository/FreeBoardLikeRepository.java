package com.example.test.repository;

import com.example.test.entity.FreeBoard;
import com.example.test.entity.FreeBoardLike;
import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreeBoardLikeRepository extends JpaRepository<FreeBoardLike, Long> {

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    boolean existsByUserAndFreeBoard(User user, FreeBoard freeBoard);

    // 특정 사용자의 특정 게시글 좋아요 찾기
    Optional<FreeBoardLike> findByUserAndFreeBoard(User user, FreeBoard freeBoard);
}