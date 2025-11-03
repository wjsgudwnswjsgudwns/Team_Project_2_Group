package com.example.test.repository;

import com.example.test.entity.FreeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {

    @Query("SELECT f FROM FreeBoard f JOIN FETCH f.user ORDER BY f.fWriteTime DESC")
    Page<FreeBoard> findAllWithUser(Pageable pageable);

    @Query("SELECT f FROM FreeBoard f WHERE f.fTitle LIKE %:keyword% OR f.fContent LIKE %:keyword%")
    Page<FreeBoard> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
