package com.example.test.dto;

import com.example.test.entity.CounselBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CounselBoardResponseDTO {
    private Long id;
    private String cTitle;
    private String cContent;
    private String cFile;
    private LocalDateTime cWriteTime;
    private String username;
    private Integer cView;
    private Integer cLike;

    public static CounselBoardResponseDTO from(CounselBoard board) {
        CounselBoardResponseDTO dto = new CounselBoardResponseDTO();
        dto.setId(board.getId());
        dto.setCTitle(board.getCTitle());
        dto.setCContent(board.getCContent());
        dto.setCFile(board.getCFile());
        dto.setCWriteTime(board.getCWriteTime());
        dto.setUsername(board.getUser().getUsername());
        dto.setCView(board.getCView());
        dto.setCLike(board.getCLike());
        return dto;
    }
}