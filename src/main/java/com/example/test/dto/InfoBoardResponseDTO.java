package com.example.test.dto;

import com.example.test.entity.InfoBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InfoBoardResponseDTO {
    private Long id;
    private String iTitle;
    private String iContent;
    private String iFile;
    private LocalDateTime iWriteTime;
    private String username;
    private Integer iView;
    private Integer iLike;

    public static InfoBoardResponseDTO from(InfoBoard board) {
        InfoBoardResponseDTO dto = new InfoBoardResponseDTO();
        dto.setId(board.getId());
        dto.setITitle(board.getITitle());
        dto.setIContent(board.getIContent());
        dto.setIFile(board.getIFile());
        dto.setIWriteTime(board.getIWriteTime());
        dto.setUsername(board.getUser().getUsername());
        dto.setIView(board.getIView());
        dto.setILike(board.getILike());
        return dto;
    }
}