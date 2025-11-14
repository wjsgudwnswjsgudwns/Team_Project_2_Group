package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDTO {

    // 사용자 기본 정보
    private String username;
    private String nickname;
    private String email;
    private String provider; // OAuth 제공자
    private LocalDateTime createAccount;

    // 활동 통계
    private ActivityStats activityStats;

    // 최근 작성 글/댓글
    private List<RecentPost> recentPosts;
    private List<RecentComment> recentComments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityStats {
        private long totalPosts;           // 총 게시글 수
        private long totalComments;        // 총 댓글 수
        private long totalLikesReceived;   // 받은 좋아요 수
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPost {
        private Long id;
        private String boardType;    // "free", "counsel", "info"
        private String title;
        private LocalDateTime writeTime;
        private Integer views;
        private Integer likes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentComment {
        private Long id;
        private Long boardId;
        private String boardType;    // "free", "counsel", "info"
        private String boardTitle;   // 댓글이 달린 게시글 제목
        private String content;
        private LocalDateTime writeTime;
    }
}