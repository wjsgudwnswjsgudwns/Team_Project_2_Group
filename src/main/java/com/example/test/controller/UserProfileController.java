package com.example.test.controller;

import com.example.test.dto.MyPageDTO;
import com.example.test.service.MyPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private MyPageService myPageService;

    /**
     * 공개 프로필 조회 (비밀번호 인증 불필요)
     * GET /api/user/profile/{username}
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            // MyPageService의 getMyPageInfo를 재사용
            // 단, 민감한 정보(이메일 등)는 제외하고 반환
            MyPageDTO fullData = myPageService.getMyPageInfo(username);

            // 이메일과 계정 타입 정보는 숨김
            MyPageDTO publicData = new MyPageDTO();
            publicData.setUsername(fullData.getUsername());
            publicData.setNickname(fullData.getNickname());
            publicData.setCreateAccount(fullData.getCreateAccount());
            publicData.setActivityStats(fullData.getActivityStats());
            publicData.setRecentPosts(fullData.getRecentPosts());
            publicData.setRecentComments(fullData.getRecentComments());

            return ResponseEntity.ok(publicData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
    }
}