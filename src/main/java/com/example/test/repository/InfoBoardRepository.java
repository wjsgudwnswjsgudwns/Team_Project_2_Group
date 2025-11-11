package com.example.test.repository;

import com.example.test.entity.InfoBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InfoBoardRepository extends JpaRepository<InfoBoard, Long> {

    @Query("SELECT i FROM InfoBoard i JOIN FETCH i.user ORDER BY i.iWriteTime DESC")
    Page<InfoBoard> findAllWithUser(Pageable pageable);

    @Query("SELECT i FROM InfoBoard i WHERE i.iTitle LIKE %:keyword%")
    Page<InfoBoard> findByITitleContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM InfoBoard i WHERE i.iContent LIKE %:keyword%")
    Page<InfoBoard> findByIContentContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM InfoBoard i WHERE i.user.username LIKE %:keyword%")
    Page<InfoBoard> findByUsernameLike(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM InfoBoard i WHERE i.iTitle LIKE %:keyword% OR i.iContent LIKE %:keyword% OR i.user.username LIKE %:keyword%")
    Page<InfoBoard> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}