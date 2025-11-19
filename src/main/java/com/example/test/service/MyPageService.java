package com.example.test.service;

import com.example.test.dto.MyPageDTO;
import com.example.test.dto.UserUpdateDTO;
import com.example.test.entity.*;
import com.example.test.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MyPageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    @Autowired
    private CounselBoardRepository counselBoardRepository;

    @Autowired
    private InfoBoardRepository infoBoardRepository;

    @Autowired
    private FreeCommentRepository freeCommentRepository;

    @Autowired
    private CounselCommentRepository counselCommentRepository;

    @Autowired
    private InfoCommentRepository infoCommentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;  // ✅ 추가

    /**
     * 비밀번호 확인 (본인 인증)
     */
    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // OAuth 사용자는 비밀번호가 없으므로 항상 true 반환
        if (user.getProvider() != null && !user.getProvider().isEmpty()) {
            return true;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 마이페이지 정보 조회
     */
    public MyPageDTO getMyPageInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        MyPageDTO dto = new MyPageDTO();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setProvider(user.getProvider());
        dto.setCreateAccount(user.getCreateAccount());

        // 활동 통계
        MyPageDTO.ActivityStats stats = new MyPageDTO.ActivityStats();
        stats.setTotalPosts(getTotalPostCount(user));
        stats.setTotalComments(getTotalCommentCount(user));
        stats.setTotalLikesReceived(getTotalLikesReceived(user));
        dto.setActivityStats(stats);

        // 최근 작성 글 (최대 10개)
        dto.setRecentPosts(getRecentPosts(user, 10));

        // 최근 작성 댓글 (최대 10개)
        dto.setRecentComments(getRecentComments(user, 10));

        return dto;
    }

    /**
     * 총 게시글 수 계산
     */
    private long getTotalPostCount(User user) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

        long freeCount = freeBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .count();

        long counselCount = counselBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .count();

        long infoCount = infoBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .count();

        return freeCount + counselCount + infoCount;
    }

    /**
     * 총 댓글 수 계산
     */
    private long getTotalCommentCount(User user) {
        long freeCommentCount = freeCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .count();

        long counselCommentCount = counselCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .count();

        long infoCommentCount = infoCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .count();

        return freeCommentCount + counselCommentCount + infoCommentCount;
    }

    /**
     * 받은 총 좋아요 수 계산
     */
    private long getTotalLikesReceived(User user) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

        int freeLikes = freeBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .mapToInt(FreeBoard::getFLike)
                .sum();

        int counselLikes = counselBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .mapToInt(CounselBoard::getCLike)
                .sum();

        int infoLikes = infoBoardRepository.findAll(pageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .mapToInt(InfoBoard::getILike)
                .sum();

        return freeLikes + counselLikes + infoLikes;
    }

    /**
     * 최근 작성 글 조회
     */
    private List<MyPageDTO.RecentPost> getRecentPosts(User user, int limit) {
        List<MyPageDTO.RecentPost> recentPosts = new ArrayList<>();

        // 자유게시판
        Pageable freePageable = PageRequest.of(0, limit, Sort.by("fWriteTime").descending());
        freeBoardRepository.findAll(freePageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .limit(limit)
                .forEach(post -> recentPosts.add(new MyPageDTO.RecentPost(
                        post.getId(),
                        "free",
                        post.getFTitle(),
                        post.getFWriteTime(),
                        post.getFView(),
                        post.getFLike()
                )));

        // 구매상담게시판
        Pageable counselPageable = PageRequest.of(0, limit, Sort.by("cWriteTime").descending());
        counselBoardRepository.findAll(counselPageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .limit(limit)
                .forEach(post -> recentPosts.add(new MyPageDTO.RecentPost(
                        post.getId(),
                        "counsel",
                        post.getCTitle(),
                        post.getCWriteTime(),
                        post.getCView(),
                        post.getCLike()
                )));

        // 정보게시판
        Pageable infoPageable = PageRequest.of(0, limit, Sort.by("iWriteTime").descending());
        infoBoardRepository.findAll(infoPageable).stream()
                .filter(post -> post.getUser().getId().equals(user.getId()))
                .limit(limit)
                .forEach(post -> recentPosts.add(new MyPageDTO.RecentPost(
                        post.getId(),
                        "info",
                        post.getITitle(),
                        post.getIWriteTime(),
                        post.getIView(),
                        post.getILike()
                )));

        // 최신순으로 정렬 후 limit 적용
        return recentPosts.stream()
                .sorted((a, b) -> b.getWriteTime().compareTo(a.getWriteTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 최근 작성 댓글 조회
     */
    private List<MyPageDTO.RecentComment> getRecentComments(User user, int limit) {
        List<MyPageDTO.RecentComment> recentComments = new ArrayList<>();

        // 자유게시판 댓글
        freeCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .sorted((a, b) -> b.getFCommentWriteTime().compareTo(a.getFCommentWriteTime()))
                .limit(limit)
                .forEach(comment -> recentComments.add(new MyPageDTO.RecentComment(
                        comment.getId(),
                        comment.getFreeBoard().getId(),
                        "free",
                        comment.getFreeBoard().getFTitle(),
                        comment.getFCommentContent(),
                        comment.getFCommentWriteTime()
                )));

        // 구매상담게시판 댓글
        counselCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .sorted((a, b) -> b.getCCommentWriteTime().compareTo(a.getCCommentWriteTime()))
                .limit(limit)
                .forEach(comment -> recentComments.add(new MyPageDTO.RecentComment(
                        comment.getId(),
                        comment.getCounselBoard().getId(),
                        "counsel",
                        comment.getCounselBoard().getCTitle(),
                        comment.getCCommentContent(),
                        comment.getCCommentWriteTime()
                )));

        // 정보게시판 댓글
        infoCommentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(user.getId()))
                .sorted((a, b) -> b.getICommentWriteTime().compareTo(a.getICommentWriteTime()))
                .limit(limit)
                .forEach(comment -> recentComments.add(new MyPageDTO.RecentComment(
                        comment.getId(),
                        comment.getInfoBoard().getId(),
                        "info",
                        comment.getInfoBoard().getITitle(),
                        comment.getICommentContent(),
                        comment.getICommentWriteTime()
                )));

        // 최신순으로 정렬 후 limit 적용
        return recentComments.stream()
                .sorted((a, b) -> b.getWriteTime().compareTo(a.getWriteTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 회원정보 수정
     */
    public void updateUserInfo(String username, UserUpdateDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // OAuth 사용자가 아닌 경우에만 비밀번호 확인
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            // 현재 비밀번호 확인 (필수)
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().trim().isEmpty()) {
                throw new RuntimeException("현재 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 설정 (입력된 경우에만)
            if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
                if (dto.getNewPassword().length() < 8 || dto.getNewPassword().length() > 16) {
                    throw new RuntimeException("새 비밀번호는 8글자 이상 16글자 이하여야 합니다.");
                }

                if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
                    throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
                }

                // 비밀번호 복잡도 검사
                if (!isValidPassword(dto.getNewPassword())) {
                    throw new RuntimeException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
                }

                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            }
        }

        // 닉네임 변경 (중복 체크)
        if (!user.getNickname().equals(dto.getNickname())) {
            if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(dto.getNickname());
        }

        // ✅ 이메일 변경 (이메일이 변경된 경우에만 인증 확인)
        if (!user.getEmail().equals(dto.getEmail())) {
            // 이메일 인증 확인
            if (!emailService.isEmailVerified(dto.getEmail(), "CHANGE_EMAIL")) {
                throw new RuntimeException("새 이메일 인증이 완료되지 않았습니다.");
            }
            user.setEmail(dto.getEmail());
        }

        userRepository.save(user);
    }

    /**
     * 회원탈퇴
     */
    public void deleteAccount(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // OAuth 사용자가 아닌 경우에만 비밀번호 확인
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 회원 삭제 (CASCADE로 관련 데이터도 함께 삭제됨)
        userRepository.delete(user);
    }

    /**
     * 비밀번호 복잡도 검사
     */
    private boolean isValidPassword(String password) {
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");
        return hasLetter && hasDigit && hasSpecial && password.length() >= 8;
    }
}