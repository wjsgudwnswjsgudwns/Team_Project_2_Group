package com.example.test.service;

import com.example.test.dto.NewsArticleDTO;
import com.example.test.entity.InfoBoard;
import com.example.test.entity.User;
import com.example.test.repository.InfoBoardRepository;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    // ✅ URL 정규화
                    String normalizedUrl = normalizeUrl(article.getLink());

                    // ✅ 중복 체크
                    if (isArticleAlreadyPosted(normalizedUrl)) {
                        System.out.println("  ⭐️ 이미 게시됨: " + article.getTitle());
                        skipped++;
                        continue;
                    }

                    System.out.println("→ " + article.getTitle());

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            article.getDescription(),
                            article.getLink(),
                            article.getImageUrl()
                    );

                    // 저장
                    try {
                        saveNewsPost(systemUser, summarized, normalizedUrl);
                        totalSuccess++;
                        rssSuccess++;
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("  ⚠️ DB 중복 제약 위반 (동시성) - 스킵");
                        skipped++;
                    }

                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.err.println("✖ 실패: " + article.getTitle());
                }
            }

            // ==================== 2. 퀘이사존 뉴스 ====================
            System.out.println("\n🇰🇷 [2/2] 퀘이사존 뉴스 수집 중...");
            List<NewsArticleDTO> quasarzoneArticles = quasarzoneCrawlerService.fetchLatestArticles(3);
            System.out.println("📰 퀘이사존 " + quasarzoneArticles.size() + "개 수집");

            for (NewsArticleDTO article : quasarzoneArticles) {
                try {
                    System.out.println("📄 처리 중: " + article.getTitle());

                    // ✅ Step 1: 출처 URL 추출
                    QuasarzoneCrawlerService.ArticleContentResult contentResult =
                            quasarzoneCrawlerService.fetchArticleContentWithImage(article.getLink());

                    if (contentResult.sourceUrl == null || contentResult.sourceUrl.isEmpty()) {
                        System.err.println("⚠️ 출처 URL이 없어 스킵");
                        skipped++;
                        continue;
                    }

                    // ✅ Step 2: 트위터/X 체크
                    if (contentResult.sourceUrl.contains("twitter.com") ||
                            contentResult.sourceUrl.contains("x.com")) {
                        System.err.println("⚠️ 트위터/X 링크는 크롤링 불가 - 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("🌐 출처 URL: " + contentResult.sourceUrl);

                    // ✅ Step 3: URL 정규화 및 중복 체크
                    String normalizedSourceUrl = normalizeUrl(contentResult.sourceUrl);

                    if (isArticleAlreadyPosted(normalizedSourceUrl)) {
                        System.out.println("⭐️ 이미 게시됨 (중복): " + article.getTitle());
                        skipped++;
                        continue;
                    }

                    // ✅ Step 4: 출처 본문 크롤링
                    String sourceContent = rssFeedService.fetchFullContent(contentResult.sourceUrl);

                    if (sourceContent.isEmpty() || sourceContent.length() < 100) {
                        System.err.println("⚠️ 출처 사이트에서 본문을 가져올 수 없어 스킵");
                        skipped++;
                        continue;
                    }

                    System.out.println("✅ 출처 본문 크롤링 완료 (길이: " + sourceContent.length() + "자)");

                    // ✅ Step 5: AI 요약
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

                    // ✅ Step 6: 저장
                    try {
                        saveNewsPost(systemUser, summarized, normalizedSourceUrl);
                        totalSuccess++;
                        qzSuccess++;
                        System.out.println("  ✅ 저장 완료");
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("  ⚠️ DB 중복 제약 위반 (동시성) - 스킵");
                        skipped++;
                    }

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
     * ✅ 통합된 저장 메서드 (모든 뉴스는 이것만 사용)
     */
    @Transactional
    public void saveNewsPost(User systemUser, String summarized, String normalizedSourceUrl) {
        // 저장 직전 한 번 더 체크 (동시성 대비)
        if (infoBoardRepository.existsBySourceUrl(normalizedSourceUrl)) {
            throw new DataIntegrityViolationException("Duplicate sourceUrl: " + normalizedSourceUrl);
        }

        InfoBoard post = new InfoBoard();
        post.setITitle(extractTitleFromContent(summarized));
        post.setIContent(removeTitleFromContent(summarized));
        post.setUser(systemUser);
        post.setIFile("");
        post.setSourceUrl(normalizedSourceUrl);  // ✅ 반드시 설정!

        infoBoardRepository.save(post);

        System.out.println("  ✔ DB 저장 완료 (sourceUrl: " + normalizedSourceUrl + ")");
    }

    /**
     * ✅ 중복 체크
     */
    private boolean isArticleAlreadyPosted(String normalizedUrl) {
        if (normalizedUrl == null || normalizedUrl.isEmpty()) return false;

        boolean exists = infoBoardRepository.existsBySourceUrl(normalizedUrl);

        if (exists) {
            System.out.println("  🔍 중복 감지: " + normalizedUrl);
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
     * ✅ 테스트용 즉시 실행 (중복 체크 포함)
     */
    public void runNowForTesting() {
        autoPostTechNews();
    }

    /**
     * ✅ 수정된 강제 실행 메서드 (중복 체크는 하되 소량만)
     * 주의: 이 메서드도 이제 정규화된 URL로 저장합니다!
     */
    public void runNowForTestingForce() {
        System.out.println("🔥 [강제 모드] 소량 뉴스 수집 시작 (중복 체크 포함)");

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

                    // ✅ URL 정규화
                    String normalizedUrl = normalizeUrl(article.getLink());

                    // ✅ 중복 체크 (강제 모드도 체크함!)
                    if (isArticleAlreadyPosted(normalizedUrl)) {
                        System.out.println("  ⭐️ 이미 게시됨 - 스킵");
                        skipped++;
                        continue;
                    }

                    String summarized = geminiSummarizeService.summarizeAndTranslate(
                            article.getTitle(),
                            article.getDescription(),
                            article.getLink(),
                            article.getImageUrl()
                    );

                    if (!isValidSummary(summarized)) {
                        System.err.println("⚠️ AI 요약 실패 - 스킵");
                        skipped++;
                        continue;
                    }

                    // ✅ saveNewsPost 사용 (통일!)
                    try {
                        saveNewsPost(systemUser, summarized, normalizedUrl);
                        totalSuccess++;
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("⚠️ DB 중복 제약 위반 - 스킵");
                        skipped++;
                    }

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

                    // ✅ URL 정규화
                    String normalizedSourceUrl = normalizeUrl(contentResult.sourceUrl);

                    // ✅ 중복 체크 (강제 모드도 체크함!)
                    if (isArticleAlreadyPosted(normalizedSourceUrl)) {
                        System.out.println("  ⭐️ 이미 게시됨 - 스킵");
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

                    // ✅ saveNewsPost 사용 (통일!)
                    try {
                        saveNewsPost(systemUser, summarized, normalizedSourceUrl);
                        totalSuccess++;
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("⚠️ DB 중복 제약 위반 - 스킵");
                        skipped++;
                    }

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