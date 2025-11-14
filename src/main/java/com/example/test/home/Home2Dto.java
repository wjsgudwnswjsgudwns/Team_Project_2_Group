package com.example.test.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Home2Dto {
    private List<BoardPreviewDTO> infoBoardPosts;
    private List<BoardPreviewDTO> freeBoardPosts;
    private List<BoardPreviewDTO> counselBoardPosts;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BoardPreviewDTO {
    private Long id;
    private String title;
    private Integer commentCount;
    private String boardType; // "info", "free", "counsel"
}