package com.example.test.repository;

import com.example.test.entity.InfoComment;
import com.example.test.entity.InfoBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoCommentRepository extends JpaRepository<InfoComment, Long> {

    @Query("SELECT i FROM InfoComment i WHERE i.infoBoard = :infoBoard AND i.parent IS NULL ORDER BY i.iCommentWriteTime ASC")
    Page<InfoComment> findTopLevelCommentsByInfoBoard(@Param("infoBoard") InfoBoard infoBoard, Pageable pageable);

    @Query("SELECT COUNT(i) FROM InfoComment i WHERE i.infoBoard = :infoBoard AND i.parent IS NULL")
    long countTopLevelCommentsByInfoBoard(@Param("infoBoard") InfoBoard infoBoard);

    long countByInfoBoard(InfoBoard infoBoard);
}