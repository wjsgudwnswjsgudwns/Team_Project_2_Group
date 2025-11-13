package com.example.test.dto;

import com.example.test.entity.CounselBoard;
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
public class CounselBoardResponseDTO {
    private Long id;
    private String cTitle;
    private String cContent;
    private String cFile;
    private LocalDateTime cWriteTime;
    private String username;
    private Integer cView;
    private Integer cLike;

    // ✅ 추가
    private String firstImageUrl;
    private Integer imageCount;

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

        // ✅ 이미지 추출
        extractFirstImage(dto, board.getCContent());

        return dto;
    }

    private static void extractFirstImage(CounselBoardResponseDTO dto, String htmlContent) {
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