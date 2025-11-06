package com.example.test.controller;

import com.example.test.dto.FreeCommentDTO;
import com.example.test.dto.FreeCommentRequestDTO;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.FreeCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/freeboard/{boardId}/comments")
public class FreeCommentController {

    @Autowired
    private FreeCommentService freeCommentService;

    @Autowired
    private JwtUtil jwtUtil;

    // 댓글 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<FreeCommentDTO>> getComments(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FreeCommentDTO> comments = freeCommentService.getComments(boardId, page, size);
        return ResponseEntity.ok(comments);
    }

    // 댓글 작성
    @PostMapping
    public ResponseEntity<FreeCommentDTO> createComment(
            @PathVariable Long boardId,
            @Valid @RequestBody FreeCommentRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        FreeCommentDTO comment = freeCommentService.createComment(boardId, dto, username);
        return ResponseEntity.ok(comment);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<FreeCommentDTO> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody FreeCommentRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        FreeCommentDTO comment = freeCommentService.updateComment(commentId, dto, username);
        return ResponseEntity.ok(comment);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        freeCommentService.deleteComment(commentId, username);
        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    // 댓글 개수 조회
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long boardId) {
        long count = freeCommentService.getCommentCount(boardId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private String extractUsername(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }
}