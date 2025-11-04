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

    // 제목으로 검색 - @Query로 명시
    @Query("SELECT f FROM FreeBoard f WHERE f.fTitle LIKE %:keyword%")
    Page<FreeBoard> findByFTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    // 내용으로 검색 - @Query로 명시
    @Query("SELECT f FROM FreeBoard f WHERE f.fContent LIKE %:keyword%")
    Page<FreeBoard> findByFContentContaining(@Param("keyword") String keyword, Pageable pageable);

    // 작성자로 검색
    @Query("SELECT f FROM FreeBoard f WHERE f.user.username LIKE %:keyword%")
    Page<FreeBoard> findByUsernameLike(@Param("keyword") String keyword, Pageable pageable);

    // 전체 검색 (제목 + 내용 + 작성자) - JPQL은 엔티티 필드명 그대로
    @Query("SELECT f FROM FreeBoard f WHERE f.fTitle LIKE %:keyword% OR f.fContent LIKE %:keyword% OR f.user.username LIKE %:keyword%")
    Page<FreeBoard> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
