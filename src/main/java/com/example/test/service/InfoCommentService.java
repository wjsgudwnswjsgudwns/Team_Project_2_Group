package com.example.test.service;

import com.example.test.dto.InfoCommentDTO;
import com.example.test.dto.InfoCommentRequestDTO;
import com.example.test.entity.InfoComment;
import com.example.test.entity.InfoBoard;
import com.example.test.entity.User;
import com.example.test.repository.InfoCommentRepository;
import com.example.test.repository.InfoBoardRepository;
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
public class InfoCommentService {

    @Autowired
    private InfoCommentRepository infoCommentRepository;

    @Autowired
    private InfoBoardRepository infoBoardRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<InfoCommentDTO> getComments(Long boardId, int page, int size) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("iCommentWriteTime").ascending());
        Page<InfoComment> topLevelComments = infoCommentRepository.findTopLevelCommentsByInfoBoard(board, pageable);

        String boardAuthor = board.getUser().getUsername();

        return topLevelComments.map(comment -> InfoCommentDTO.from(comment, boardAuthor));
    }

    public long getTopLevelCommentCount(Long boardId) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return infoCommentRepository.countTopLevelCommentsByInfoBoard(board);
    }

    public InfoCommentDTO createComment(Long boardId, InfoCommentRequestDTO dto, String username) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        InfoComment comment = new InfoComment();
        comment.setICommentContent(dto.getICommentContent());
        comment.setUser(user);
        comment.setInfoBoard(board);

        if (dto.getParentId() != null) {
            InfoComment parent = infoCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            comment.setParent(parent);
        }

        InfoComment saved = infoCommentRepository.save(comment);
        String boardAuthor = board.getUser().getUsername();

        return InfoCommentDTO.from(saved, boardAuthor);
    }

    public InfoCommentDTO updateComment(Long commentId, InfoCommentRequestDTO dto, String username) {
        InfoComment comment = infoCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        comment.setICommentContent(dto.getICommentContent());
        InfoComment updated = infoCommentRepository.save(comment);

        String boardAuthor = comment.getInfoBoard().getUser().getUsername();
        return InfoCommentDTO.from(updated, boardAuthor);
    }

    public void deleteComment(Long commentId, String username) {
        InfoComment comment = infoCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }

        if (comment.getChildren().isEmpty()) {
            infoCommentRepository.delete(comment);
        } else {
            comment.setICommentDeleted(true);
            comment.setICommentContent("삭제된 댓글입니다.");
            infoCommentRepository.save(comment);
        }
    }

    public long getCommentCount(Long boardId) {
        InfoBoard board = infoBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return infoCommentRepository.countByInfoBoard(board);
    }
}