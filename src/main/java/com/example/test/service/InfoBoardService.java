package com.example.test.service;

import com.example.test.dto.InfoBoardDTO;
import com.example.test.dto.InfoBoardResponseDTO;
import com.example.test.entity.InfoBoard;
import com.example.test.entity.InfoBoardLike;
import com.example.test.entity.User;
import com.example.test.repository.InfoBoardLikeRepository;
import com.example.test.repository.InfoBoardRepository;
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
public class InfoBoardService {

    @Autowired
    private InfoBoardRepository infoBoardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InfoBoardLikeRepository infoBoardLikeRepository;

    @Transactional
    public InfoBoard createPost(InfoBoardDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        InfoBoard board = new InfoBoard();
        board.setITitle(dto.getITitle());
        board.setIContent(dto.getIContent());
        board.setIFile(dto.getIFile());
        board.setUser(user);
        board.setSourceUrl(null); // 자동수집이 아닌 사용자가 직접 작성한 글은 sourceUrl null로

        return infoBoardRepository.save(board);
    }

    public Page<InfoBoard> getPostList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("iWriteTime").descending());
        return infoBoardRepository.findAllWithUser(pageable);
    }

    public InfoBoard getPost(Long id) {
        InfoBoard board = infoBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        board.setIView(board.getIView() + 1);
        return infoBoardRepository.save(board);
    }

    public InfoBoard updatePost(Long id, InfoBoardDTO dto, String username) {
        InfoBoard board = infoBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정 가능합니다");
        }

        board.setITitle(dto.getITitle());
        board.setIContent(dto.getIContent());
        board.setIFile(dto.getIFile());
        return infoBoardRepository.save(board);
    }

    public void deletePost(Long id, String username) {
        InfoBoard board = infoBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제 가능합니다");
        }

        infoBoardRepository.delete(board);
    }

    @Transactional
    public boolean toggleLike(Long boardId, String username) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<InfoBoardLike> existingLike = infoBoardLikeRepository.findByUserAndInfoBoard(user, board);

        if (existingLike.isPresent()) {
            infoBoardLikeRepository.delete(existingLike.get());
            board.setILike(board.getILike() - 1);
            infoBoardRepository.save(board);
            return false;
        } else {
            InfoBoardLike like = new InfoBoardLike(user, board);
            infoBoardLikeRepository.save(like);
            board.setILike(board.getILike() + 1);
            infoBoardRepository.save(board);
            return true;
        }
    }

    public boolean isLikedByUser(Long boardId, String username) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return infoBoardLikeRepository.existsByUserAndInfoBoard(user, board);
    }

    public Page<InfoBoardResponseDTO> searchPosts(String searchType, String keyword, Pageable pageable) {
        Page<InfoBoard> boards = switch (searchType.toLowerCase()) {
            case "title" -> infoBoardRepository.findByITitleContaining(keyword, pageable);
            case "content" -> infoBoardRepository.findByIContentContaining(keyword, pageable);
            case "author" -> infoBoardRepository.findByUsernameLike(keyword, pageable);
            case "all" -> infoBoardRepository.findByKeyword(keyword, pageable);
            default -> throw new IllegalArgumentException("Invalid search type: " + searchType);
        };

        return boards.map(board -> {
            InfoBoardResponseDTO dto = new InfoBoardResponseDTO();
            dto.setId(board.getId());
            dto.setITitle(board.getITitle());
            dto.setIContent(board.getIContent());
            dto.setIView(board.getIView());
            dto.setILike(board.getILike());
            dto.setIWriteTime(board.getIWriteTime());
            dto.setUsername(board.getUser().getUsername());
            dto.setSourceUrl(board.getSourceUrl());
            return dto;
        });
    }

    public Page<InfoBoardResponseDTO> getNearbyPosts(Long currentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("iWriteTime").descending());
        Page<InfoBoard> posts = infoBoardRepository.findAllWithUser(pageable);
        return posts.map(InfoBoardResponseDTO::from);
    }
}