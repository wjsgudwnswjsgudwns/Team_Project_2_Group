package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreeCommentRequestDTO {
    @JsonProperty("fCommentContent")
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String fCommentContent;

    @JsonProperty("parentId")
    private Long parentId; // null이면 일반 댓글, 값이 있으면 대댓글
}