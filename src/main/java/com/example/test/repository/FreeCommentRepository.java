package com.example.test.repository;

import com.example.test.entity.FreeComment;
import com.example.test.entity.FreeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeCommentRepository extends JpaRepository<FreeComment, Long> {

    // 특정 게시글의 최상위 댓글만 조회 (parent가 null인 것들)
    @Query("SELECT c FROM FreeComment c WHERE c.freeBoard = :freeBoard AND c.parent IS NULL ORDER BY c.fCommentWriteTime ASC")
    List<FreeComment> findTopLevelCommentsByFreeBoard(@Param("freeBoard") FreeBoard freeBoard);

    // 특정 게시글의 모든 댓글 개수
    long countByFreeBoard(FreeBoard freeBoard);
}