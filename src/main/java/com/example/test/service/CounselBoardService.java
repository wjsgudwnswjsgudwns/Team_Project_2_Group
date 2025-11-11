package com.example.test.service;

import com.example.test.dto.CounselBoardDTO;
import com.example.test.dto.CounselBoardResponseDTO;
import com.example.test.entity.CounselBoard;
import com.example.test.entity.CounselBoardLike;
import com.example.test.entity.User;
import com.example.test.repository.CounselBoardLikeRepository;
import com.example.test.repository.CounselBoardRepository;
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
public class CounselBoardService {

    @Autowired
    private CounselBoardRepository counselBoardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselBoardLikeRepository counselBoardLikeRepository;

    @Transactional
    public CounselBoard createPost(CounselBoardDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        CounselBoard board = new CounselBoard();
        board.setCTitle(dto.getCTitle());
        board.setCContent(dto.getCContent());
        board.setCFile(dto.getCFile());
        board.setUser(user);

        return counselBoardRepository.save(board);
    }

    public Page<CounselBoard> getPostList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cWriteTime").descending());
        return counselBoardRepository.findAllWithUser(pageable);
    }

    public CounselBoard getPost(Long id) {
        CounselBoard board = counselBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        board.setCView(board.getCView() + 1);
        return counselBoardRepository.save(board);
    }

    public CounselBoard updatePost(Long id, CounselBoardDTO dto, String username) {
        CounselBoard board = counselBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정 가능합니다");
        }

        board.setCTitle(dto.getCTitle());
        board.setCContent(dto.getCContent());
        board.setCFile(dto.getCFile());
        return counselBoardRepository.save(board);
    }

    public void deletePost(Long id, String username) {
        CounselBoard board = counselBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제 가능합니다");
        }

        counselBoardRepository.delete(board);
    }

    @Transactional
    public boolean toggleLike(Long boardId, String username) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<CounselBoardLike> existingLike = counselBoardLikeRepository.findByUserAndCounselBoard(user, board);

        if (existingLike.isPresent()) {
            counselBoardLikeRepository.delete(existingLike.get());
            board.setCLike(board.getCLike() - 1);
            counselBoardRepository.save(board);
            return false;
        } else {
            CounselBoardLike like = new CounselBoardLike(user, board);
            counselBoardLikeRepository.save(like);
            board.setCLike(board.getCLike() + 1);
            counselBoardRepository.save(board);
            return true;
        }
    }

    public boolean isLikedByUser(Long boardId, String username) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return counselBoardLikeRepository.existsByUserAndCounselBoard(user, board);
    }

    public Page<CounselBoardResponseDTO> searchPosts(String searchType, String keyword, Pageable pageable) {
        Page<CounselBoard> boards = switch (searchType.toLowerCase()) {
            case "title" -> counselBoardRepository.findByCTitleContaining(keyword, pageable);
            case "content" -> counselBoardRepository.findByCContentContaining(keyword, pageable);
            case "author" -> counselBoardRepository.findByUsernameLike(keyword, pageable);
            case "all" -> counselBoardRepository.findByKeyword(keyword, pageable);
            default -> throw new IllegalArgumentException("Invalid search type: " + searchType);
        };

        return boards.map(board -> {
            CounselBoardResponseDTO dto = new CounselBoardResponseDTO();
            dto.setId(board.getId());
            dto.setCTitle(board.getCTitle());
            dto.setCContent(board.getCContent());
            dto.setCView(board.getCView());
            dto.setCLike(board.getCLike());
            dto.setCWriteTime(board.getCWriteTime());
            dto.setUsername(board.getUser().getUsername());
            return dto;
        });
    }

    public Page<CounselBoardResponseDTO> getNearbyPosts(Long currentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cWriteTime").descending());
        Page<CounselBoard> posts = counselBoardRepository.findAllWithUser(pageable);
        return posts.map(CounselBoardResponseDTO::from);
    }
}