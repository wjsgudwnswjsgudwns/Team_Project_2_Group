package com.example.test.service;

import com.example.test.dto.NewsArticleDTO;
import com.example.test.entity.InfoBoard;
import com.example.test.entity.User;
import com.example.test.repository.InfoBoardRepository;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsSchedulerService {

    @Autowired
    private RssFeedService rssFeedService;

    @Autowired
    private QuasarzoneCrawlerService quasarzoneCrawlerService;

    @Autowired
    private GeminiSummarizeService geminiSummarizeService;

    @Autowired
    private InfoBoardRepository infoBoardRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String SYSTEM_USERNAME = "NewsBot";
    private static final int MIN_CONTENT_FOR_SUMMARY = 200; // AI 요약을 위한 최소 길이

    /**
     * 1시간마다 자동 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoPostTechNews() {
        System.out.println("🤖 [" + LocalDateTime.now() + "] 뉴스 자동 수집 시작...");

        try {
            User systemUser = getOrCreateSystemUser();
            int totalSuccess = 0;
            int rssSuccess = 0;
            int qzSuccess = 0;
            int skipped = 0;

            // ==================== 1. 해외 RSS 뉴스 ====================
            System.out.println("\n📡 [1/2] 해외 RSS 뉴스 수집 중...");
            List<NewsArticleDTO> rssArticles = rssFeedService.fetchLatestNews(4); // 각 사이트당 4개
            System.out.println("📰 해외 뉴스 " + rssArticles.size() + "개 수집됨");

            for (NewsArticleDTO article : rssArticles) {
                try {
                    if (isArticleAlreadyPosted(article.getLink())) {
                        continue;
                    }

                    System.out.println("→ " + article.getTitle());

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            article.getDescription(),
                            article.getLink(),
                            article.getImageUrl()
                    );

                    createAutoPost(systemUser, article, summarized, "해외 뉴스", article.getImageUrl());
                    totalSuccess++;
                    rssSuccess++;
                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.err.println("✖ 실패: " + article.getTitle());
                }
            }

            // ==================== 2. 퀘이사존 뉴스 (출처 기반 AI 요약) ====================
            System.out.println("\n🇰🇷 퀘이사존 뉴스 수집 중...");
            List<NewsArticleDTO> quasarzoneArticles = quasarzoneCrawlerService.fetchLatestArticles(3); // 각 게시판당 3개
            System.out.println("✅ 퀘이사존 " + quasarzoneArticles.size() + "개 수집");

            for (NewsArticleDTO article : quasarzoneArticles) {
                try {
                    if (isArticleAlreadyPosted(article.getLink())) {
                        System.out.println("⏭️  이미 게시됨: " + article.getTitle());
                        continue;
                    }

                    System.out.println("📄 처리 중: " + article.getTitle());

                    // 상세 페이지에서 출처 링크와 이미지 크롤링
                    QuasarzoneCrawlerService.ArticleContentResult contentResult =
                            quasarzoneCrawlerService.fetchArticleContentWithImage(article.getLink());

                    // 출처 URL 확인
                    if (contentResult.sourceUrl == null || contentResult.sourceUrl.isEmpty()) {
                        System.err.println("⚠️ 출처 URL이 없어 스킵");
                        skipped++;
                        continue;
                    }

                    // 트위터/X 링크 스킵
                    if (contentResult.sourceUrl.contains("twitter.com") ||
                            contentResult.sourceUrl.contains("x.com")) {
                        System.err.println("⚠️ 트위터/X 링크는 크롤링 불가 - 스킵");
                        skipped++;
                        continue;
                    }

                    // 트위터/X 링크는 크롤링 불가능하므로 스킵
                    if (contentResult.sourceUrl.contains("twitter.com") ||
                            contentResult.sourceUrl.contains("x.com")) {
                        System.err.println("⚠️ 트위터/X 링크는 크롤링 불가 - 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("🌐 출처 URL: " + contentResult.sourceUrl);

                    // 출처 사이트에서 본문 크롤링
                    String sourceContent = rssFeedService.fetchFullContent(contentResult.sourceUrl);

                    if (sourceContent.isEmpty() || sourceContent.length() < 100) {
                        System.err.println("⚠️ 출처 사이트에서 본문을 가져올 수 없어 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("✅ 출처 본문 크롤링 완료 (길이: " + sourceContent.length() + "자)");

                    // Gemini로 요약 (해외 뉴스 요약 방식 사용)
                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            sourceContent.substring(0, Math.min(sourceContent.length(), 2000)), // 최대 2000자
                            contentResult.sourceUrl,
                            contentResult.imageUrl
                    );

                    // 요약 결과 검증
                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 또는 품질 불량 - 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("✅ AI 요약 완료");

                    createAutoPost(systemUser, article, summarized, article.getSource(), contentResult.imageUrl);
                    totalSuccess++;
                    qzSuccess++;
                    Thread.sleep(3000); // AI 호출이 있으므로 대기시간 증가

                } catch (Exception e) {
                    System.err.println("❌ 퀘이사존 기사 처리 실패: " + article.getTitle() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n✅ 완료: " + totalSuccess + "개 (해외 " +
                    rssSuccess + ", 퀘이사존 " + qzSuccess + ", 스킵 " + skipped + ")");

        } catch (Exception e) {
            System.err.println("❌ 뉴스 수집 전체 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 원문 그대로 포맷팅 (여러 이미지 지원)
     */
    private String createRawContentFormat(String title, String content, String link, String source, List<String> imageUrls) {
        StringBuilder result = new StringBuilder();

        result.append("<div><h2>").append(title).append("</h2></div>");
        result.append("<div></div><br>");

        // 모든 이미지 추가 (최대 5개까지)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int imageCount = Math.min(imageUrls.size(), 5); // 최대 5개만 표시
            System.out.println("🖼️  게시글에 " + imageCount + "개 이미지 추가");

            for (int i = 0; i < imageCount; i++) {
                String imageUrl = imageUrls.get(i);
                result.append("<div><img src='").append(imageUrl)
                        .append("' alt='기사 이미지 ").append(i + 1)
                        .append("' style='max-width: 100%; height: auto; margin: 10px 0;'></div>");

                // 이미지 사이 간격
                if (i < imageCount - 1) {
                    result.append("<div></div><br>");
                }
            }
            result.append("<div></div><br>");
        }

        // 본문 내용 (문단 나누기)
        String[] paragraphs = content.split("\\n{2,}"); // 2개 이상의 개행으로 문단 구분

        if (paragraphs.length > 1) {
            for (String para : paragraphs) {
                String trimmed = para.trim();
                if (!trimmed.isEmpty() && trimmed.length() > 10) {
                    result.append("<div>").append(trimmed).append("</div>");
                    result.append("<div></div><br>");
                }
            }
        } else {
            // 문단 구분이 없으면 전체를 하나로
            result.append("<div>").append(content).append("</div>");
            result.append("<div></div><br>");
        }

        // 출처 정보
        result.append("<div style='padding: 10px; background-color: rgba(255, 59, 48, 0.1); border-left: 3px solid #ff3b30;'>");
        result.append("📌 출처: ").append(source).append("<br>");
        result.append("🔗 원문 링크: <a href='").append(link)
                .append("' target='_blank' style='color: #ff3b30;'>").append(link).append("</a>");
        result.append("</div>");

        return result.toString();
    }

    /**
     * 단일 이미지용 오버로드 메서드 (하위 호환성)
     */
    private String createRawContentFormat(String title, String content, String link, String source, String imageUrl) {
        List<String> imageUrls = imageUrl != null && !imageUrl.isEmpty()
                ? List.of(imageUrl)
                : List.of();
        return createRawContentFormat(title, content, link, source, imageUrls);
    }

    /**
     * 시스템 계정 가져오기 또는 생성
     */
    private User getOrCreateSystemUser() {
        return userRepository.findByUsername(SYSTEM_USERNAME)
                .orElseGet(() -> {
                    User systemUser = new User();
                    systemUser.setUsername(SYSTEM_USERNAME);
                    systemUser.setPassword("SYSTEM_ACCOUNT");
                    systemUser.setNickname("뉴스봇 🤖");
                    systemUser.setEmail("newsbot@system.local");
                    systemUser.setRole("ROLE_SYSTEM");
                    return userRepository.save(systemUser);
                });
    }

    /**
     * 기사 중복 확인
     */
    private boolean isArticleAlreadyPosted(String link) {
        if (link == null || link.isEmpty()) return false;

        String normalizedLink = link
                .replaceAll("\\?.*$", "")
                .replaceAll("^https?://", "")
                .replaceAll("/$", "");

        List<InfoBoard> existingPosts = infoBoardRepository.findAll();
        return existingPosts.stream()
                .anyMatch(post -> {
                    if (post.getIContent() == null) return false;

                    String content = post.getIContent()
                            .replaceAll("\\?.*$", "")
                            .replaceAll("^https?://", "")
                            .replaceAll("/$", "");

                    return content.contains(normalizedLink);
                });
    }

    /**
     * 자동 게시글 생성
     */
    private void createAutoPost(User systemUser, NewsArticleDTO article, String content, String sourceType, String imageUrl) {
        InfoBoard post = new InfoBoard();

        String translatedTitle = extractTitleFromContent(content);

        // 아이콘 제거, 제목만
        String title = translatedTitle;

        String contentWithoutTitle = removeTitleFromContent(content);

        post.setITitle(title);
        post.setIContent(contentWithoutTitle);
        post.setUser(systemUser);
        post.setIFile("");

        infoBoardRepository.save(post);

        System.out.println("  ✓ 게시됨");
    }

    /**
     * AI 응답에서 번역된 제목 추출
     */
    private String extractTitleFromContent(String content) {
        try {
            if (content.contains("<h2>") && content.contains("</h2>")) {
                int start = content.indexOf("<h2>") + 4;
                int end = content.indexOf("</h2>");
                String title = content.substring(start, end).trim();

                if (title.length() > 80) {
                    return title.substring(0, 80) + "...";
                }
                return title;
            }
        } catch (Exception e) {
            System.err.println("제목 추출 실패: " + e.getMessage());
        }

        return "뉴스";
    }

    private String removeTitleFromContent(String content) {
        try {
            if (content.contains("<h2>") && content.contains("</h2>")) {
                int end = content.indexOf("</h2>") + 5;
                String after = content.substring(end);
                if (after.startsWith("<div></div><br>")) {
                    return after.substring(15);
                }
                return after;
            }
        } catch (Exception e) {
            System.err.println("제목 제거 실패: " + e.getMessage());
        }

        return content;
    }

    /**
     * AI 요약 결과 검증
     */
    private boolean isValidSummary(String summary) {
        if (summary == null || summary.isEmpty()) {
            System.err.println("  ❌ 요약 결과가 비어있음");
            return false;
        }

        // 최소 길이 체크 (너무 짧으면 실패)
        if (summary.length() < 100) {
            System.err.println("  ❌ 요약이 너무 짧음 (" + summary.length() + "자)");
            return false;
        }

        // 제목 추출 시도
        String title = extractTitleFromContent(summary);

        // 제목이 없거나 "뉴스"만 있으면 실패
        if (title.equals("뉴스") || title.isEmpty()) {
            System.err.println("  ❌ 제목 추출 실패");
            return false;
        }

        // 제목이 너무 긴 경우 (80자 이상) - 잘린 제목일 가능성
        if (title.length() > 100) {
            System.err.println("  ❌ 제목이 비정상적으로 김 (" + title.length() + "자)");
            return false;
        }

        // "..."로 끝나는 경우 (잘린 제목)
        if (title.endsWith("...") || title.endsWith("..")) {
            System.err.println("  ❌ 제목이 잘림: " + title);
            return false;
        }

        // 에러 메시지 포함 여부
        String lowerSummary = summary.toLowerCase();
        if (lowerSummary.contains("요약할 수 없습니다") ||
                lowerSummary.contains("실패") ||
                lowerSummary.contains("error") ||
                lowerSummary.contains("cannot") ||
                lowerSummary.contains("unable")) {
            System.err.println("  ❌ 에러 메시지 포함");
            return false;
        }

        // TITLE: 또는 CONTENT: 태그가 없으면 잘못된 형식
        if (!summary.contains("TITLE:") && !summary.contains("<h2>")) {
            System.err.println("  ❌ 잘못된 형식 (TITLE 태그 없음)");
            return false;
        }

        System.out.println("  ✅ 요약 검증 통과");
        return true;
    }

    /**
     * 테스트용 즉시 실행
     */
    public void runNowForTesting() {
        autoPostTechNews();
    }

    /**
     * 테스트용: 중복 체크 없이 강제 실행
     */
    public void runNowForTestingForce() {
        System.out.println("🔥 [강제 모드] 중복 체크 없이 뉴스 수집 시작...");

        try {
            User systemUser = getOrCreateSystemUser();
            int totalSuccess = 0;
            int skipped = 0;

            // RSS 뉴스
            List<NewsArticleDTO> rssArticles = rssFeedService.fetchLatestNews(1);
            System.out.println("📰 RSS " + rssArticles.size() + "개 수집");

            for (NewsArticleDTO article : rssArticles) {
                try {
                    System.out.println("📄 처리: " + article.getTitle());

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            article.getDescription(),
                            article.getLink(),
                            article.getImageUrl()
                    );

                    // 요약 결과 검증
                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 또는 품질 불량 - 스킵");
                        continue;
                    }

                    createAutoPost(systemUser, article, summarized, "해외 뉴스", article.getImageUrl());
                    totalSuccess++;
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("❌ 처리 실패: " + e.getMessage());
                }
            }

            // 퀘이사존 뉴스
            List<NewsArticleDTO> qzArticles = quasarzoneCrawlerService.fetchLatestArticles(1);
            System.out.println("📰 퀘이사존 " + qzArticles.size() + "개 수집");

            for (NewsArticleDTO article : qzArticles) {
                try {
                    System.out.println("📄 처리: " + article.getTitle());

                    QuasarzoneCrawlerService.ArticleContentResult contentResult =
                            quasarzoneCrawlerService.fetchArticleContentWithImage(article.getLink());

                    if (contentResult.sourceUrl == null || contentResult.sourceUrl.isEmpty()) {
                        System.err.println("⚠️ 출처 URL이 없어 스킵");
                        skipped++;
                        continue;
                    }

                    // 출처 사이트 크롤링
                    String sourceContent = rssFeedService.fetchFullContent(contentResult.sourceUrl);

                    if (sourceContent.isEmpty() || sourceContent.length() < 100) {
                        System.err.println("⚠️ 출처 본문 크롤링 실패");
                        skipped++;
                        continue;
                    }

                    // Gemini 요약
                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            sourceContent.substring(0, Math.min(sourceContent.length(), 2000)),
                            contentResult.sourceUrl,
                            contentResult.imageUrl
                    );

                    // 요약 결과 검증
                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 - 스킵");
                        skipped++;
                        continue;
                    }

                    createAutoPost(systemUser, article, summarized, article.getSource(), contentResult.imageUrl);
                    totalSuccess++;
                    Thread.sleep(3000);

                } catch (Exception e) {
                    System.err.println("❌ 처리 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("✅ 강제 게시 완료: " + totalSuccess + "개 성공, " + skipped + "개 스킵");

        } catch (Exception e) {
            System.err.println("❌ 강제 수집 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}