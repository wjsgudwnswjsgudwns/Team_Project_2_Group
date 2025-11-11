package com.example.test.controller;

import com.example.test.dto.CounselCommentDTO;
import com.example.test.dto.CounselCommentRequestDTO;
import com.example.test.jwt.JwtUtil;
import com.example.test.service.CounselCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/counselboard/{boardId}/comments")
public class CounselCommentController {

    @Autowired
    private CounselCommentService counselCommentService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Page<CounselCommentDTO>> getComments(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CounselCommentDTO> comments = counselCommentService.getComments(boardId, page, size);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CounselCommentDTO> createComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CounselCommentRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        CounselCommentDTO comment = counselCommentService.createComment(boardId, dto, username);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CounselCommentDTO> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CounselCommentRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        CounselCommentDTO comment = counselCommentService.updateComment(commentId, dto, username);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        String username = extractUsername(authHeader);
        counselCommentService.deleteComment(commentId, username);
        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long boardId) {
        long count = counselCommentService.getCommentCount(boardId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private String extractUsername(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }

    @GetMapping("/count/toplevel")
    public ResponseEntity<Map<String, Long>> getTopLevelCommentCount(@PathVariable Long boardId) {
        long count = counselCommentService.getTopLevelCommentCount(boardId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}