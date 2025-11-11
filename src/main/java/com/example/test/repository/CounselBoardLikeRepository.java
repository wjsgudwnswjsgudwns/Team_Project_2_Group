package com.example.test.repository;

import com.example.test.entity.CounselBoard;
import com.example.test.entity.CounselBoardLike;
import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounselBoardLikeRepository extends JpaRepository<CounselBoardLike, Long> {

    boolean existsByUserAndCounselBoard(User user, CounselBoard counselBoard);

    Optional<CounselBoardLike> findByUserAndCounselBoard(User user, CounselBoard counselBoard);
}