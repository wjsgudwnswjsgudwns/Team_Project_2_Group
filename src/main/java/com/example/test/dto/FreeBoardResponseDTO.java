package com.example.test.dto;

import com.example.test.entity.FreeBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FreeBoardResponseDTO {
    private Long id;
    private String fTitle;
    private String fContent;
    private String fFile;
    private LocalDateTime fWriteTime;
    private String username;
    private Integer fView;
    private Integer fLike;
    
    public static FreeBoardResponseDTO from(FreeBoard board) {
        FreeBoardResponseDTO dto = new FreeBoardResponseDTO();
        dto.setId(board.getId());
        dto.setFTitle(board.getFTitle());
        dto.setFContent(board.getFContent());
        dto.setFFile(board.getFFile());
        dto.setFWriteTime(board.getFWriteTime());
        dto.setUsername(board.getUser().getUsername());
        dto.setFView(board.getFView());
        dto.setFLike(board.getFLike());
        return dto;
    }
}
