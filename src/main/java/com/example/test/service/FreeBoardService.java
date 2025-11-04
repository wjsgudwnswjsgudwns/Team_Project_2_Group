package com.example.test.service;

import com.example.test.dto.FreeBoardDTO;
import com.example.test.entity.FreeBoard;
import com.example.test.entity.User;
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

@Service
@Transactional
public class FreeBoardService {

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    @Autowired
    private UserRepository userRepository;

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
}
