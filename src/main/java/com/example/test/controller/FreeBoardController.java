package com.example.test.controller;

import com.example.test.dto.FreeBoardDTO;
import com.example.test.dto.FreeBoardResponseDTO;
import com.example.test.entity.FreeBoard;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.FreeBoardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/freeboard")
public class FreeBoardController {

    @Autowired
    private FreeBoardService freeBoardService;

    @Autowired
    private JwtUtil jwtUtil;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestBody FreeBoardDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        FreeBoard board = freeBoardService.createPost(dto, username);
        return ResponseEntity.ok(FreeBoardResponseDTO.from(board));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<?> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FreeBoard> posts = freeBoardService.getPostList(page, size);

        // Entity -> DTO 변환
        Page<FreeBoardResponseDTO> dtoPage = posts.map(FreeBoardResponseDTO::from);

        return ResponseEntity.ok(dtoPage);
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        FreeBoard board = freeBoardService.getPost(id);
        return ResponseEntity.ok(board);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody FreeBoardDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        FreeBoard board = freeBoardService.updatePost(id, dto, username);
        return ResponseEntity.ok(board);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        freeBoardService.deletePost(id, username);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }

    private String extractUsername(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }

    // 좋아요 토글 API (POST /api/freeboard/{id}/like)
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        boolean isLiked = freeBoardService.toggleLike(id, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isLiked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }

    // 좋아요 여부 확인 API (GET /api/freeboard/{id}/like/status)
    @GetMapping("/{id}/like/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        boolean isLiked = freeBoardService.isLikedByUser(id, username);

        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }

    // 검색 API (GET /api/freeboard/search)
    @GetMapping("/search")
    public ResponseEntity<Page<FreeBoardResponseDTO>> searchPosts(
            @RequestParam String searchType,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fWriteTime"));
        Page<FreeBoardResponseDTO> result = freeBoardService.searchPosts(searchType, keyword, pageable);

        return ResponseEntity.ok(result);
    }
    
}
