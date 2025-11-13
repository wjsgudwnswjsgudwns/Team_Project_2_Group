package com.example.test.service;

import com.example.test.dto.NewsArticleDTO;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RssFeedService {

    // RSS Feed URL 목록 (작동 확인된 것만)
    private static final List<RssFeedSource> RSS_SOURCES = List.of(
            new RssFeedSource("PC Gamer", "https://www.pcgamer.com/rss/"),
            new RssFeedSource("TechPowerUp", "https://www.techpowerup.com/rss/news")
    );

    /**
     * 모든 RSS 피드에서 최신 뉴스 가져오기
     */
    public List<NewsArticleDTO> fetchLatestNews(int maxArticlesPerSource) {
        List<NewsArticleDTO> allArticles = new ArrayList<>();

        for (RssFeedSource source : RSS_SOURCES) {
            try {
                List<NewsArticleDTO> articles = fetchFromRss(source, maxArticlesPerSource);
                allArticles.addAll(articles);
                System.out.println("✅ " + source.getName() + "에서 " + articles.size() + "개 기사 수집");
            } catch (Exception e) {
                System.err.println("❌ " + source.getName() + " RSS 피드 오류: " + e.getMessage());
                // 오류 발생 시에도 계속 진행
            }
        }

        return allArticles;
    }

    /**
     * 특정 RSS 피드에서 뉴스 가져오기 (향상된 버전)
     */
    private List<NewsArticleDTO> fetchFromRss(RssFeedSource source, int maxArticles) throws Exception {
        List<NewsArticleDTO> articles = new ArrayList<>();

        // HttpURLConnection으로 User-Agent 설정하여 403 에러 방지
        URL url = new URL(source.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        try (InputStream inputStream = connection.getInputStream()) {
            SyndFeedInput input = new SyndFeedInput();
            input.setPreserveWireFeed(true); // 원본 피드 유지

            // XML 파싱 설정 - DOCTYPE 및 잘못된 태그 허용
            SyndFeed feed = input.build(new XmlReader(inputStream) {
                @Override
                public String getEncoding() {
                    return "UTF-8";
                }
            });

            int count = 0;
            for (SyndEntry entry : feed.getEntries()) {
                if (count >= maxArticles) break;

                try {
                    NewsArticleDTO article = new NewsArticleDTO();
                    article.setTitle(cleanText(entry.getTitle()));
                    article.setDescription(cleanHtml(entry.getDescription() != null ? entry.getDescription().getValue() : ""));
                    article.setLink(entry.getLink());
                    article.setSource(source.getName());

                    // 발행일 변환
                    Date pubDate = entry.getPublishedDate();
                    if (pubDate != null) {
                        article.setPublishedDate(
                                LocalDateTime.ofInstant(pubDate.toInstant(), ZoneId.systemDefault())
                        );
                    } else {
                        article.setPublishedDate(LocalDateTime.now());
                    }

                    // 이미지 추출 시도
                    if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
                        article.setImageUrl(entry.getEnclosures().get(0).getUrl());
                    } else {
                        // 본문에서 이미지 추출 시도
                        String imageUrl = extractImageFromContent(entry.getDescription() != null ? entry.getDescription().getValue() : "");
                        article.setImageUrl(imageUrl);
                    }

                    articles.add(article);
                    count++;
                } catch (Exception e) {
                    System.err.println("❌ 개별 기사 파싱 실패: " + e.getMessage());
                }
            }
        }

        return articles;
    }

    /**
     * HTML 태그 제거
     */
    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        try {
            Document doc = Jsoup.parse(html);
            return doc.text();
        } catch (Exception e) {
            return html.replaceAll("<[^>]*>", "");
        }
    }

    /**
     * 텍스트 정리
     */
    private String cleanText(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * 본문에서 이미지 URL 추출
     */
    private String extractImageFromContent(String content) {
        if (content == null || content.isEmpty()) return null;
        try {
            Document doc = Jsoup.parse(content);
            var img = doc.select("img").first();
            if (img != null) {
                return img.attr("src");
            }
        } catch (Exception e) {
            // 이미지 추출 실패 시 null 반환
        }
        return null;
    }

    /**
     * RSS Feed 소스 정보
     */
    private static class RssFeedSource {
        private final String name;
        private final String url;

        public RssFeedSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
    }

    /**
     * 특정 기사의 전체 내용 가져오기 (웹 스크래핑)
     */
    public String fetchFullContent(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // 사이트별로 본문 추출 로직이 다를 수 있음
            var article = doc.selectFirst("article");
            if (article != null) {
                return article.text();
            }

            // 대안: class나 id로 본문 찾기
            var content = doc.selectFirst(".article-content, .post-content, .entry-content, .article-body");
            if (content != null) {
                return content.text();
            }

            // 최후의 수단: 전체 텍스트
            return doc.body().text();

        } catch (Exception e) {
            System.err.println("본문 추출 실패: " + e.getMessage());
            return "";
        }
    }
}