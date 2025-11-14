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
            List<NewsArticleDTO> rssArticles = rssFeedService.fetchLatestNews(4);
            System.out.println("📰 해외 뉴스 " + rssArticles.size() + "개 수집됨");

            for (NewsArticleDTO article : rssArticles) {
                try {
                    // ✅ 개선된 중복 검사
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

            // ==================== 2. 퀘이사존 뉴스 ====================
            System.out.println("\n🇰🇷 퀘이사존 뉴스 수집 중...");
            List<NewsArticleDTO> quasarzoneArticles = quasarzoneCrawlerService.fetchLatestArticles(3);
            System.out.println("✅ 퀘이사존 " + quasarzoneArticles.size() + "개 수집");

            for (NewsArticleDTO article : quasarzoneArticles) {
                try {
                    // ✅ 개선된 중복 검사
                    if (isArticleAlreadyPosted(article.getLink())) {
                        System.out.println("⏭️  이미 게시됨: " + article.getTitle());
                        continue;
                    }

                    System.out.println("📄 처리 중: " + article.getTitle());

                    QuasarzoneCrawlerService.ArticleContentResult contentResult =
                            quasarzoneCrawlerService.fetchArticleContentWithImage(article.getLink());

                    if (contentResult.sourceUrl == null || contentResult.sourceUrl.isEmpty()) {
                        System.err.println("⚠️ 출처 URL이 없어 스킵");
                        skipped++;
                        continue;
                    }

                    if (contentResult.sourceUrl.contains("twitter.com") ||
                            contentResult.sourceUrl.contains("x.com")) {
                        System.err.println("⚠️ 트위터/X 링크는 크롤링 불가 - 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("🌐 출처 URL: " + contentResult.sourceUrl);

                    // ✅ 출처 URL도 중복 검사
                    if (isArticleAlreadyPosted(contentResult.sourceUrl)) {
                        System.out.println("⏭️  출처 URL 중복: " + article.getTitle());
                        continue;
                    }

                    String sourceContent = rssFeedService.fetchFullContent(contentResult.sourceUrl);

                    if (sourceContent.isEmpty() || sourceContent.length() < 100) {
                        System.err.println("⚠️ 출처 사이트에서 본문을 가져올 수 없어 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("✅ 출처 본문 크롤링 완료 (길이: " + sourceContent.length() + "자)");

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            sourceContent.substring(0, Math.min(sourceContent.length(), 2000)),
                            contentResult.sourceUrl,
                            contentResult.imageUrl
                    );

                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 - 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("✅ AI 요약 완료");

                    // ✅ 출처 URL을 sourceUrl로 저장
                    createAutoPostWithSourceUrl(systemUser, article, summarized, article.getSource(),
                            contentResult.imageUrl, contentResult.sourceUrl);
                    totalSuccess++;
                    qzSuccess++;
                    Thread.sleep(3000);

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
     * ✅ 개선된 중복 검사 - sourceUrl 필드 활용
     */
    private boolean isArticleAlreadyPosted(String link) {
        if (link == null || link.isEmpty()) return false;

        String normalizedLink = normalizeUrl(link);

        // ✅ DB 인덱스를 활용한 빠른 검사
        boolean exists = infoBoardRepository.existsBySourceUrl(normalizedLink);

        if (exists) {
            System.out.println("  🔍 중복 감지: " + normalizedLink);
        }

        return exists;
    }

    /**
     * ✅ URL 정규화
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) return "";

        return url
                .replaceAll("\\?.*$", "")        // 쿼리스트링 제거
                .replaceAll("^https?://", "")    // 프로토콜 제거
                .replaceAll("/$", "")            // 끝 슬래시 제거
                .toLowerCase()                    // 소문자 변환
                .trim();                          // 공백 제거
    }

    /**
     * ✅ 자동 게시글 생성 (해외 RSS용)
     */
    private void createAutoPost(User systemUser, NewsArticleDTO article, String content,
                                String sourceType, String imageUrl) {
        InfoBoard post = new InfoBoard();

        String translatedTitle = extractTitleFromContent(content);
        String contentWithoutTitle = removeTitleFromContent(content);

        post.setITitle(translatedTitle);
        post.setIContent(contentWithoutTitle);
        post.setUser(systemUser);
        post.setIFile("");

        // ✅ sourceUrl 저장 (RSS는 article.getLink()를 사용)
        String normalizedUrl = normalizeUrl(article.getLink());
        post.setSourceUrl(normalizedUrl);

        infoBoardRepository.save(post);

        System.out.println("  ✔ 게시됨 (sourceUrl: " + normalizedUrl + ")");
    }

    /**
     * ✅ 자동 게시글 생성 (퀘이사존용 - 출처 URL 별도 전달)
     */
    private void createAutoPostWithSourceUrl(User systemUser, NewsArticleDTO article, String content,
                                             String sourceType, String imageUrl, String sourceUrl) {
        InfoBoard post = new InfoBoard();

        String translatedTitle = extractTitleFromContent(content);
        String contentWithoutTitle = removeTitleFromContent(content);

        post.setITitle(translatedTitle);
        post.setIContent(contentWithoutTitle);
        post.setUser(systemUser);
        post.setIFile("");

        // ✅ sourceUrl 저장 (퀘이사존은 실제 출처 URL 사용)
        String normalizedUrl = normalizeUrl(sourceUrl);
        post.setSourceUrl(normalizedUrl);

        infoBoardRepository.save(post);

        System.out.println("  ✔ 게시됨 (sourceUrl: " + normalizedUrl + ")");
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

    /**
     * 본문에서 제목 제거
     */
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

        if (summary.length() < 100) {
            System.err.println("  ❌ 요약이 너무 짧음 (" + summary.length() + "자)");
            return false;
        }

        String title = extractTitleFromContent(summary);

        if (title.equals("뉴스") || title.isEmpty()) {
            System.err.println("  ❌ 제목 추출 실패");
            return false;
        }

        if (title.length() > 100) {
            System.err.println("  ❌ 제목이 비정상적으로 김 (" + title.length() + "자)");
            return false;
        }

        if (title.endsWith("...") || title.endsWith("..")) {
            System.err.println("  ❌ 제목이 잘림: " + title);
            return false;
        }

        String lowerSummary = summary.toLowerCase();
        if (lowerSummary.contains("요약할 수 없습니다") ||
                lowerSummary.contains("실패") ||
                lowerSummary.contains("error") ||
                lowerSummary.contains("cannot") ||
                lowerSummary.contains("unable")) {
            System.err.println("  ❌ 에러 메시지 포함");
            return false;
        }

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
     * 테스트용: 중복 체크 없이 강제 실행 (소량만)
     */
    public void runNowForTestingForce() {
        System.out.println("🔥 [강제 모드] 중복 체크 없이 뉴스 수집 시작!");

        try {
            User systemUser = getOrCreateSystemUser();
            int totalSuccess = 0;
            int skipped = 0;

            // RSS 뉴스 1개만
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

                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 - 스킵");
                        continue;
                    }

                    // 강제 모드는 sourceUrl 저장 안함 (테스트용)
                    InfoBoard post = new InfoBoard();
                    String translatedTitle = extractTitleFromContent(summarized);
                    String contentWithoutTitle = removeTitleFromContent(summarized);
                    post.setITitle(translatedTitle);
                    post.setIContent(contentWithoutTitle);
                    post.setUser(systemUser);
                    post.setIFile("");
                    post.setSourceUrl(null);  // 강제 모드는 null
                    infoBoardRepository.save(post);

                    totalSuccess++;
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("❌ 처리 실패: " + e.getMessage());
                }
            }

            // 퀘이사존 1개만
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

                    String sourceContent = rssFeedService.fetchFullContent(contentResult.sourceUrl);

                    if (sourceContent.isEmpty() || sourceContent.length() < 100) {
                        System.err.println("⚠️ 출처 본문 크롤링 실패");
                        skipped++;
                        continue;
                    }

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            sourceContent.substring(0, Math.min(sourceContent.length(), 2000)),
                            contentResult.sourceUrl,
                            contentResult.imageUrl
                    );

                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 - 스킵");
                        skipped++;
                        continue;
                    }

                    // 강제 모드는 sourceUrl 저장 안함
                    InfoBoard post = new InfoBoard();
                    String translatedTitle = extractTitleFromContent(summarized);
                    String contentWithoutTitle = removeTitleFromContent(summarized);
                    post.setITitle(translatedTitle);
                    post.setIContent(contentWithoutTitle);
                    post.setUser(systemUser);
                    post.setIFile("");
                    post.setSourceUrl(null);  // 강제 모드는 null
                    infoBoardRepository.save(post);

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