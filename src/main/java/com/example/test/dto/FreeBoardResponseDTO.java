package com.example.test.dto;

import com.example.test.entity.FreeBoard;
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
public class FreeBoardResponseDTO {
    private Long id;
    private String fTitle;
    private String fContent;
    private String fFile;
    private LocalDateTime fWriteTime;
    private String username;
    private Integer fView;
    private Integer fLike;

    // ✅ 추가: 첫 번째 이미지 URL
    private String firstImageUrl;

    // ✅ 추가: 이미지 개수
    private Integer imageCount;

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

        // ✅ HTML에서 첫 번째 이미지 추출
        extractFirstImage(dto, board.getFContent());

        return dto;
    }

    /**
     * HTML 콘텐츠에서 첫 번째 이미지 URL과 전체 이미지 개수 추출
     */
    private static void extractFirstImage(FreeBoardResponseDTO dto, String htmlContent) {
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
                String imgSrc = firstImg.attr("src");
                dto.setFirstImageUrl(imgSrc);
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