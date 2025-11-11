package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CounselCommentRequestDTO {
    @JsonProperty("cCommentContent")
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String cCommentContent;

    @JsonProperty("parentId")
    private Long parentId;
}