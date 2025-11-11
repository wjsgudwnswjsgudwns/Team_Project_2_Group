package com.example.test.dto;

import com.example.test.entity.InfoComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class InfoCommentDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("iCommentContent")
    private String iCommentContent;

    @JsonProperty("username")
    private String username;

    @JsonProperty("parentId")
    private Long parentId;

    @JsonProperty("iCommentWriteTime")
    private LocalDateTime iCommentWriteTime;

    @JsonProperty("iCommentUpdateTime")
    private LocalDateTime iCommentUpdateTime;

    @JsonProperty("iCommentDeleted")
    private boolean iCommentDeleted;

    @JsonProperty("isAuthor")
    private boolean isAuthor;

    @JsonProperty("children")
    private List<InfoCommentDTO> children = new ArrayList<>();

    public static InfoCommentDTO from(InfoComment comment, String boardAuthor) {
        InfoCommentDTO dto = new InfoCommentDTO();
        dto.setId(comment.getId());
        dto.setICommentContent(comment.getICommentContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setICommentWriteTime(comment.getICommentWriteTime());
        dto.setICommentUpdateTime(comment.getICommentUpdateTime());
        dto.setICommentDeleted(comment.isICommentDeleted());
        dto.setAuthor(comment.getUser().getUsername().equals(boardAuthor));

        dto.setChildren(
                comment.getChildren().stream()
                        .map(child -> InfoCommentDTO.from(child, boardAuthor))
                        .collect(Collectors.toList())
        );

        return dto;
    }
}