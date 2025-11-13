package com.example.test.dto;

import com.example.test.entity.InfoBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

    // ✅ 추가
    private String firstImageUrl;
    private Integer imageCount;

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

        // ✅ 이미지 추출
        extractFirstImage(dto, board.getIContent());

        return dto;
    }

    private static void extractFirstImage(InfoBoardResponseDTO dto, String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            dto.setFirstImageUrl(null);
            dto.setImageCount(0);
            return;
        }

        try {
            Document doc = Jsoup.parse(htmlContent);
            var images = doc.select("img");

            dto.setImageCount(images.size());

            if (!images.isEmpty()) {
                Element firstImg = images.first();
                dto.setFirstImageUrl(firstImg.attr("src"));
            } else {
                dto.setFirstImageUrl(null);
            }
        } catch (Exception e) {
            System.err.println("이미지 추출 실패: " + e.getMessage());
            dto.setFirstImageUrl(null);
            dto.setImageCount(0);
        }
    }
}