package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreeBoardDTO {
    @JsonProperty("fTitle")
    @NotBlank(message = "제목은 필수입니다")
    private String fTitle;

    @JsonProperty("fContent")
    @NotBlank(message = "내용은 필수입니다")
    private String fContent;

    @JsonProperty("fFile")
    private String fFile;
}