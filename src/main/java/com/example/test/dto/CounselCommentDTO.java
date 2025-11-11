package com.example.test.dto;

import com.example.test.entity.CounselComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CounselCommentDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("cCommentContent")
    private String cCommentContent;

    @JsonProperty("username")
    private String username;

    @JsonProperty("parentId")
    private Long parentId;

    @JsonProperty("cCommentWriteTime")
    private LocalDateTime cCommentWriteTime;

    @JsonProperty("cCommentUpdateTime")
    private LocalDateTime cCommentUpdateTime;

    @JsonProperty("cCommentDeleted")
    private boolean cCommentDeleted;

    @JsonProperty("isAuthor")
    private boolean isAuthor;

    @JsonProperty("children")
    private List<CounselCommentDTO> children = new ArrayList<>();

    public static CounselCommentDTO from(CounselComment comment, String boardAuthor) {
        CounselCommentDTO dto = new CounselCommentDTO();
        dto.setId(comment.getId());
        dto.setCCommentContent(comment.getCCommentContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setCCommentWriteTime(comment.getCCommentWriteTime());
        dto.setCCommentUpdateTime(comment.getCCommentUpdateTime());
        dto.setCCommentDeleted(comment.isCCommentDeleted());
        dto.setAuthor(comment.getUser().getUsername().equals(boardAuthor));

        dto.setChildren(
                comment.getChildren().stream()
                        .map(child -> CounselCommentDTO.from(child, boardAuthor))
                        .collect(Collectors.toList())
        );

        return dto;
    }
}