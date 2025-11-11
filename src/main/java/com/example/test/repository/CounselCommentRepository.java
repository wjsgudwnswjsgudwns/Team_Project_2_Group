package com.example.test.repository;

import com.example.test.entity.CounselComment;
import com.example.test.entity.CounselBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CounselCommentRepository extends JpaRepository<CounselComment, Long> {

    @Query("SELECT c FROM CounselComment c WHERE c.counselBoard = :counselBoard AND c.parent IS NULL ORDER BY c.cCommentWriteTime ASC")
    Page<CounselComment> findTopLevelCommentsByCounselBoard(@Param("counselBoard") CounselBoard counselBoard, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CounselComment c WHERE c.counselBoard = :counselBoard AND c.parent IS NULL")
    long countTopLevelCommentsByCounselBoard(@Param("counselBoard") CounselBoard counselBoard);

    long countByCounselBoard(CounselBoard counselBoard);
}