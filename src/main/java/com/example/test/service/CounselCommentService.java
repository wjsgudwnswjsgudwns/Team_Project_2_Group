package com.example.test.service;

import com.example.test.dto.CounselCommentDTO;
import com.example.test.dto.CounselCommentRequestDTO;
import com.example.test.entity.CounselComment;
import com.example.test.entity.CounselBoard;
import com.example.test.entity.User;
import com.example.test.repository.CounselCommentRepository;
import com.example.test.repository.CounselBoardRepository;
import com.example.test.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CounselCommentService {

    @Autowired
    private CounselCommentRepository counselCommentRepository;

    @Autowired
    private CounselBoardRepository counselBoardRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<CounselCommentDTO> getComments(Long boardId, int page, int size) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("cCommentWriteTime").ascending());
        Page<CounselComment> topLevelComments = counselCommentRepository.findTopLevelCommentsByCounselBoard(board, pageable);

        String boardAuthor = board.getUser().getUsername();

        return topLevelComments.map(comment -> CounselCommentDTO.from(comment, boardAuthor));
    }

    public long getTopLevelCommentCount(Long boardId) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return counselCommentRepository.countTopLevelCommentsByCounselBoard(board);
    }

    public CounselCommentDTO createComment(Long boardId, CounselCommentRequestDTO dto, String username) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        CounselComment comment = new CounselComment();
        comment.setCCommentContent(dto.getCCommentContent());
        comment.setUser(user);
        comment.setCounselBoard(board);

        if (dto.getParentId() != null) {
            CounselComment parent = counselCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            comment.setParent(parent);
        }

        CounselComment saved = counselCommentRepository.save(comment);
        String boardAuthor = board.getUser().getUsername();

        return CounselCommentDTO.from(saved, boardAuthor);
    }

    public CounselCommentDTO updateComment(Long commentId, CounselCommentRequestDTO dto, String username) {
        CounselComment comment = counselCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        comment.setCCommentContent(dto.getCCommentContent());
        CounselComment updated = counselCommentRepository.save(comment);

        String boardAuthor = comment.getCounselBoard().getUser().getUsername();
        return CounselCommentDTO.from(updated, boardAuthor);
    }

    public void deleteComment(Long commentId, String username) {
        CounselComment comment = counselCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }

        if (comment.getChildren().isEmpty()) {
            counselCommentRepository.delete(comment);
        } else {
            comment.setCCommentDeleted(true);
            comment.setCCommentContent("삭제된 댓글입니다.");
            counselCommentRepository.save(comment);
        }
    }

    public long getCommentCount(Long boardId) {
        CounselBoard board = counselBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return counselCommentRepository.countByCounselBoard(board);
    }
}