package com.example.test.service;

import com.example.test.dto.FreeBoardDTO;
import com.example.test.entity.FreeBoard;
import com.example.test.entity.FreeBoardLike;
import com.example.test.entity.User;
import com.example.test.repository.FreeBoardLikeRepository;
import com.example.test.repository.FreeBoardRepository;
import com.example.test.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class FreeBoardService {

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreeBoardLikeRepository freeBoardLikeRepository;

    // 게시글 작성
    @Transactional
    public FreeBoard createPost(FreeBoardDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        FreeBoard board = new FreeBoard();
        board.setFTitle(dto.getFTitle());
        board.setFContent(dto.getFContent());
        board.setUser(user);
        // 파일 업로드 로직 추가

        return freeBoardRepository.save(board);
    }

    // 게시글 목록 조회 (페이징)
    public Page<FreeBoard> getPostList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fWriteTime").descending());
        return freeBoardRepository.findAllWithUser(pageable);
    }

    // 게시글 상세 조회 (조회수 증가)
    public FreeBoard getPost(Long id) {
        FreeBoard board = freeBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        board.setFView(board.getFView() + 1); // 조회수 증가
        return freeBoardRepository.save(board);
    }

    // 게시글 수정
    public FreeBoard updatePost(Long id, FreeBoardDTO dto, String username) {
        FreeBoard board = freeBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 작성자 확인
        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정 가능합니다");
        }

        board.setFTitle(dto.getFTitle());
        board.setFContent(dto.getFContent());
        return freeBoardRepository.save(board);
    }

    // 게시글 삭제
    public void deletePost(Long id, String username) {
        FreeBoard board = freeBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제 가능합니다");
        }

        freeBoardRepository.delete(board);
    }

    // 좋아요 토글 (좋아요 누르기/취소)
    @Transactional
    public boolean toggleLike(Long boardId, String username) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<FreeBoardLike> existingLike = freeBoardLikeRepository.findByUserAndFreeBoard(user, board);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀으면 취소
            freeBoardLikeRepository.delete(existingLike.get());
            board.setFLike(board.getFLike() - 1);
            freeBoardRepository.save(board);
            return false; // 좋아요 취소됨
        } else {
            // 좋아요 추가
            FreeBoardLike like = new FreeBoardLike(user, board);
            freeBoardLikeRepository.save(like);
            board.setFLike(board.getFLike() + 1);
            freeBoardRepository.save(board);
            return true; // 좋아요 추가됨
        }
    }

    // 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    public boolean isLikedByUser(Long boardId, String username) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return freeBoardLikeRepository.existsByUserAndFreeBoard(user, board);
    }
}
