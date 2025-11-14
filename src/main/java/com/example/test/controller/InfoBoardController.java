package com.example.test.controller;

import com.example.test.dto.InfoBoardDTO;
import com.example.test.dto.InfoBoardResponseDTO;
import com.example.test.entity.InfoBoard;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.InfoBoardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/infoboard")
public class InfoBoardController {

    @Autowired
    private InfoBoardService infoBoardService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestBody InfoBoardDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        InfoBoard board = infoBoardService.createPost(dto, username);
        return ResponseEntity.ok(InfoBoardResponseDTO.from(board));
    }

    @GetMapping
    public ResponseEntity<?> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InfoBoard> posts = infoBoardService.getPostList(page, size);
        Page<InfoBoardResponseDTO> dtoPage = posts.map(InfoBoardResponseDTO::from);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        InfoBoard board = infoBoardService.getPost(id);
        return ResponseEntity.ok(InfoBoardResponseDTO.from(board));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody InfoBoardDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        InfoBoard board = infoBoardService.updatePost(id, dto, username);
        return ResponseEntity.ok(InfoBoardResponseDTO.from(board));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        infoBoardService.deletePost(id, username);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }

    private String extractUsername(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        boolean isLiked = infoBoardService.toggleLike(id, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isLiked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }

    @GetMapping("/{id}/like/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.ok(Map.of("isLiked", false));
        }

        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        boolean isLiked = infoBoardService.isLikedByUser(id, username);

        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InfoBoardResponseDTO>> searchPosts(
            @RequestParam String searchType,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "iWriteTime"));
        Page<InfoBoardResponseDTO> result = infoBoardService.searchPosts(searchType, keyword, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/nearby")
    public ResponseEntity<?> getNearbyPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<InfoBoardResponseDTO> nearbyPosts = infoBoardService.getNearbyPosts(id, page, size);
        return ResponseEntity.ok(nearbyPosts);
    }
}