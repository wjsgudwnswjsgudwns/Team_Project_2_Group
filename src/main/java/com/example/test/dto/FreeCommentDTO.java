package com.example.test.dto;

import com.example.test.entity.FreeComment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class FreeCommentDTO {
    private Long id;
    private String fCommentContent;
    private String username;
    private Long parentId;
    private LocalDateTime fCommentWriteTime;
    private LocalDateTime fCommentUpdateTime;
    private boolean fCommentDeleted;
    private boolean isAuthor; // 게시글 작성자 여부
    private List<FreeCommentDTO> children = new ArrayList<>();

    public static FreeCommentDTO from(FreeComment comment, String boardAuthor) {
        FreeCommentDTO dto = new FreeCommentDTO();
        dto.setId(comment.getId());
        dto.setFCommentContent(comment.getFCommentContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setFCommentWriteTime(comment.getFCommentWriteTime());
        dto.setFCommentUpdateTime(comment.getFCommentUpdateTime());
        dto.setFCommentDeleted(comment.isFCommentDeleted());
        dto.setAuthor(comment.getUser().getUsername().equals(boardAuthor));

        // 자식 댓글들도 재귀적으로 변환
        dto.setChildren(
                comment.getChildren().stream()
                        .map(child -> FreeCommentDTO.from(child, boardAuthor))
                        .collect(Collectors.toList())
        );

        return dto;
    }
}