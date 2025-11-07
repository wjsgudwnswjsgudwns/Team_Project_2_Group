package com.example.test.repository;

import com.example.test.entity.FreeComment;
import com.example.test.entity.FreeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeCommentRepository extends JpaRepository<FreeComment, Long> {

    // 특정 게시글의 최상위 댓글만 조회 (페이징 적용)
    @Query("SELECT c FROM FreeComment c WHERE c.freeBoard = :freeBoard AND c.parent IS NULL ORDER BY c.fCommentWriteTime ASC")
    Page<FreeComment> findTopLevelCommentsByFreeBoard(@Param("freeBoard") FreeBoard freeBoard, Pageable pageable);

    // 특정 게시글의 최상위 댓글 개수만 카운트
    @Query("SELECT COUNT(c) FROM FreeComment c WHERE c.freeBoard = :freeBoard AND c.parent IS NULL")
    long countTopLevelCommentsByFreeBoard(@Param("freeBoard") FreeBoard freeBoard);

    // 특정 게시글의 모든 댓글 개수 (대댓글 포함) - 표시용
    long countByFreeBoard(FreeBoard freeBoard);
}