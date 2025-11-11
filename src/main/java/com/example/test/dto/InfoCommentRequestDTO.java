package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InfoCommentRequestDTO {
    @JsonProperty("iCommentContent")
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String iCommentContent;

    @JsonProperty("parentId")
    private Long parentId;
}