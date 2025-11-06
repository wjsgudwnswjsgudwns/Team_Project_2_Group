package com.example.test.dto;

import com.example.test.entity.FreeComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class FreeCommentDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("fCommentContent")
    private String fCommentContent;

    @JsonProperty("username")
    private String username;

    @JsonProperty("parentId")
    private Long parentId;

    @JsonProperty("fCommentWriteTime")
    private LocalDateTime fCommentWriteTime;

    @JsonProperty("fCommentUpdateTime")
    private LocalDateTime fCommentUpdateTime;

    @JsonProperty("fCommentDeleted")
    private boolean fCommentDeleted;

    @JsonProperty("isAuthor")
    private boolean isAuthor; // 게시글 작성자 여부

    @JsonProperty("children")
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