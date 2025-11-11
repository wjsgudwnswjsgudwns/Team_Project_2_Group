package com.example.test.repository;

import com.example.test.entity.CounselBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CounselBoardRepository extends JpaRepository<CounselBoard, Long> {

    @Query("SELECT c FROM CounselBoard c JOIN FETCH c.user ORDER BY c.cWriteTime DESC")
    Page<CounselBoard> findAllWithUser(Pageable pageable);

    @Query("SELECT c FROM CounselBoard c WHERE c.cTitle LIKE %:keyword%")
    Page<CounselBoard> findByCTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM CounselBoard c WHERE c.cContent LIKE %:keyword%")
    Page<CounselBoard> findByCContentContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM CounselBoard c WHERE c.user.username LIKE %:keyword%")
    Page<CounselBoard> findByUsernameLike(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM CounselBoard c WHERE c.cTitle LIKE %:keyword% OR c.cContent LIKE %:keyword% OR c.user.username LIKE %:keyword%")
    Page<CounselBoard> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}