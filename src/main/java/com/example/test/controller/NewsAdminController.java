package com.example.test.controller;

import com.example.test.service.NewsSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/news")
public class NewsAdminController {

    @Autowired
    private NewsSchedulerService newsSchedulerService;

    /**
     * 관리자 전용: 즉시 뉴스 수집 실행
     * GET /api/admin/news/collect-now
     */
    @GetMapping("/collect-now")
    public ResponseEntity<?> collectNewsNow() {
        try {
            new Thread(() -> {
                newsSchedulerService.runNowForTesting();
            }).start();

            return ResponseEntity.ok(Map.of(
                    "message", "뉴스 수집이 백그라운드에서 시작되었습니다.\n\n- 해외 RSS: 5개\n- 퀘이사존: 게임 3개, 모바일 3개, 파트너뉴스 3개\n\n총 14개 기사 수집 예정 (중복 제외)\n약 30초 후 새로고침하세요.",
                    "status", "processing"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "뉴스 수집 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 강제 수집 (중복 체크 없이 - 테스트용)
     */
    @GetMapping("/collect-force")
    public ResponseEntity<?> collectForce() {
        try {
            new Thread(() -> {
                newsSchedulerService.runNowForTestingForce();
            }).start();

            return ResponseEntity.ok(Map.of(
                    "message", "[강제 모드] 중복 체크 없이 수집 시작!\n소량만 수집합니다 (테스트용)",
                    "status", "processing"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "강제 수집 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "message", "뉴스 자동 수집 시스템이 정상 작동 중입니다.",
                "schedule", "매 1시간마다 자동 실행",
                "sources", Map.of(
                        "해외RSS", "5개 (Tom's Hardware, AnandTech 등)",
                        "퀘이사존_게임", "3개",
                        "퀘이사존_모바일", "3개",
                        "퀘이사존_파트너뉴스", "3개",
                        "총계", "14개/시간 (중복 제외)"
                )
        ));
    }
}