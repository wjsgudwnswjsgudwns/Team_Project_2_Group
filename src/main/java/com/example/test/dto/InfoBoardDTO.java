package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InfoBoardDTO {
    @JsonProperty("iTitle")
    @NotBlank(message = "제목은 필수입니다")
    private String iTitle;

    @JsonProperty("iContent")
    @NotBlank(message = "내용은 필수입니다")
    private String iContent;

    @JsonProperty("iFile")
    private String iFile;
}