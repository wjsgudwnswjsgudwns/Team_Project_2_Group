package com.example.test.service;

import com.example.test.dto.NewsArticleDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuasarzoneCrawlerService {

    // 퀘이사존 게시판 URL
    private static final List<QuasarzoneBoard> BOARDS = List.of(
            new QuasarzoneBoard("게임", "https://quasarzone.com/bbs/qn_game"),
            new QuasarzoneBoard("모바일", "https://quasarzone.com/bbs/qn_mobile"),
            new QuasarzoneBoard("파트너뉴스", "https://quasarzone.com/bbs/qn_partner")
    );

    /**
     * 모든 퀘이사존 게시판에서 최신 글 가져오기
     */
    public List<NewsArticleDTO> fetchLatestArticles(int maxArticlesPerBoard) {
        List<NewsArticleDTO> allArticles = new ArrayList<>();

        for (QuasarzoneBoard board : BOARDS) {
            try {
                List<NewsArticleDTO> articles = crawlBoard(board, maxArticlesPerBoard);
                allArticles.addAll(articles);
                System.out.println("✅ 퀘이사존 [" + board.getName() + "]에서 " + articles.size() + "개 수집");
            } catch (Exception e) {
                System.err.println("❌ 퀘이사존 [" + board.getName() + "] 크롤링 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return allArticles;
    }

    /**
     * 특정 게시판 크롤링
     */
    private List<NewsArticleDTO> crawlBoard(QuasarzoneBoard board, int maxArticles) throws Exception {
        List<NewsArticleDTO> articles = new ArrayList<>();

        Document doc = Jsoup.connect(board.getUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Referer", "https://quasarzone.com/")
                .timeout(20000)
                .get();

        Elements postElements = null;

        postElements = doc.select("div.market-info-list-cont > table > tbody > tr");

        if (postElements.isEmpty()) {
            postElements = doc.select("table.market-info-type-list tbody tr");
        }

        if (postElements.isEmpty()) {
            postElements = doc.select("table tbody tr");
        }

        if (postElements.isEmpty()) {
            postElements = doc.select("div.list-item, div.board-item");
        }

        System.out.println("🔍 [" + board.getName() + "] 발견된 요소 수: " + postElements.size());

        int count = 0;
        for (Element post : postElements) {
            if (count >= maxArticles) break;

            try {
                if (post.hasClass("notice") || post.hasClass("event") || post.hasClass("ad")) {
                    continue;
                }

                Element titleElement = null;

                titleElement = post.select("td.subject-link p.subject > a").first();

                if (titleElement == null) {
                    titleElement = post.select("a.subject-link, a.title").first();
                }

                if (titleElement == null) {
                    titleElement = post.select("a[href*='/bbs/']").first();
                }

                if (titleElement == null) {
                    continue;
                }

                String title = titleElement.text().trim();
                if (title.isEmpty()) continue;

                String relativeLink = titleElement.attr("href");
                String fullLink = relativeLink.startsWith("http") ?
                        relativeLink : "https://quasarzone.com" + relativeLink;

                NewsArticleDTO article = new NewsArticleDTO();
                article.setTitle(title);
                article.setLink(fullLink);
                article.setSource("퀘이사존 " + board.getName());
                article.setPublishedDate(LocalDateTime.now());
                article.setDescription("");

                articles.add(article);
                count++;

            } catch (Exception e) {
                // 조용히 스킵
            }
        }

        return articles;
    }

    /**
     * 게시글 상세 내용과 이미지 크롤링 (출처 링크 기반)
     */
    public ArticleContentResult fetchArticleContentWithImage(String articleUrl) {
        ArticleContentResult result = new ArticleContentResult();

        try {
            System.out.println("\n🔍 [디버깅] URL: " + articleUrl);

            Document doc = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://quasarzone.com/")
                    .timeout(20000)
                    .followRedirects(true)
                    .get();

            // 1. 이미지 추출
            result.imageUrls = extractAllImages(doc);
            result.imageUrl = result.imageUrls.isEmpty() ? null : result.imageUrls.get(0);

            // 2. 출처 링크 추출 (본문 대신)
            String sourceUrl = extractSourceUrl(doc);

            if (sourceUrl != null && !sourceUrl.isEmpty()) {
                System.out.println("✅ 출처 링크 발견: " + sourceUrl);
                result.content = ""; // 본문은 비워두고
                result.sourceUrl = sourceUrl; // 출처 URL 저장
                result.isValid = true;
            } else {
                System.err.println("⚠️ 출처 링크를 찾을 수 없음");
                result.isValid = false;
            }

            System.out.println("🖼️  이미지 수: " + result.imageUrls.size() + "개");

        } catch (Exception e) {
            System.err.println("❌ 크롤링 실패 [" + articleUrl + "]: " + e.getMessage());
            e.printStackTrace();
            result.content = "";
            result.isValid = false;
        }

        return result;
    }

    /**
     * 출처 URL 추출
     */
    private String extractSourceUrl(Document doc) {
        // 방법 1: p.source-area 안의 링크
        Element sourceArea = doc.selectFirst("p.source-area.link-box");
        if (sourceArea != null) {
            Element link = sourceArea.selectFirst("a");
            if (link != null) {
                String url = link.attr("href");
                System.out.println("📎 출처 링크 (source-area): " + url);
                return url;
            }
        }

        // 방법 2: "출처"로 시작하는 텍스트가 있는 링크
        Elements allLinks = doc.select("a[href]");
        for (Element link : allLinks) {
            String text = link.text();
            if (text.contains("출처") || link.parent().text().startsWith("출처")) {
                String url = link.attr("href");
                if (url.startsWith("http")) {
                    System.out.println("📎 출처 링크 (텍스트 검색): " + url);
                    return url;
                }
            }
        }

        // 방법 3: view-content 내 외부 링크
        Element viewContent = doc.selectFirst("div.view-content");
        if (viewContent != null) {
            Elements links = viewContent.select("a[href^='http']");
            for (Element link : links) {
                String url = link.attr("href");
                // 퀘이사존 자체 링크 제외
                if (!url.contains("quasarzone.com")) {
                    System.out.println("📎 출처 링크 (외부 링크): " + url);
                    return url;
                }
            }
        }

        return null;
    }

    /**
     * 퀘이사존 본문 추출 (더 이상 사용하지 않음 - 출처 기반으로 변경)
     */
    private String extractQuasarzoneContent(Document doc) {
        // 이 함수는 더 이상 사용하지 않지만, 혹시 몰라 남겨둠
        return "";
    }

    /**
     * 본문 내 모든 이미지 추출
     */
    private List<String> extractAllImages(Document doc) {
        List<String> imageUrls = new ArrayList<>();

        // 1. OG 이미지
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null && !ogImage.attr("content").isEmpty()) {
            String url = ogImage.attr("content");
            if (!url.contains("QSZLOGO")) {
                imageUrls.add(url);
                System.out.println("🖼️ OG 이미지 발견: " + url);
            } else {
                System.out.println("⏭️  OG 이미지가 기본 로고라서 제외");
            }
        }

        // 2. 본문 내 모든 이미지
        Elements contentImages = doc.select(
                "div#new_contents img, " +
                        "div.view-content img, " +
                        "div.note-editor img, " +
                        "div.fr-view img, " +
                        "article img"
        );

        System.out.println("🔍 본문에서 발견된 img 태그: " + contentImages.size() + "개");

        for (Element img : contentImages) {
            String src = img.attr("src");
            if (src.isEmpty()) src = img.attr("data-src");
            if (src.isEmpty()) src = img.attr("data-lazy-src");

            if (!src.isEmpty()) {
                if (src.contains("avatar") || src.contains("icon") ||
                        src.contains("logo") || src.contains("emoji") ||
                        src.contains("QSZLOGO") || src.contains("blank.gif")) {
                    continue;
                }

                if (!src.startsWith("http")) {
                    src = "https://quasarzone.com" + (src.startsWith("/") ? src : "/" + src);
                }

                if (!imageUrls.contains(src)) {
                    imageUrls.add(src);
                    System.out.println("🖼️ 본문 이미지 발견: " + src);
                }
            }
        }

        System.out.println("✅ 총 " + imageUrls.size() + "개 이미지 추출");
        return imageUrls;
    }

    /**
     * 게시글 상세 내용 크롤링 (하위 호환성)
     */
    public String fetchArticleContent(String articleUrl) {
        ArticleContentResult result = fetchArticleContentWithImage(articleUrl);
        return result.content;
    }

    /**
     * 기사 내용 결과 클래스
     */
    public static class ArticleContentResult {
        public String content = "";
        public String sourceUrl = null;  // 출처 URL 추가
        public String imageUrl = null;
        public List<String> imageUrls = new ArrayList<>();
        public boolean isValid = false;
        public boolean isHtml = false;
    }

    /**
     * 게시판 정보 클래스
     */
    private static class QuasarzoneBoard {
        private final String name;
        private final String url;

        public QuasarzoneBoard(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
    }
}