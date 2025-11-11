package com.example.test.service;

import com.example.test.dto.FreeCommentDTO;
import com.example.test.dto.FreeCommentRequestDTO;
import com.example.test.entity.FreeComment;
import com.example.test.entity.FreeBoard;
import com.example.test.entity.User;
import com.example.test.repository.FreeCommentRepository;
import com.example.test.repository.FreeBoardRepository;
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
public class FreeCommentService {

    @Autowired
    private FreeCommentRepository freeCommentRepository;

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    @Autowired
    private UserRepository userRepository;

    // 댓글 목록 조회 (페이징 적용)
    public Page<FreeCommentDTO> getComments(Long boardId, int page, int size) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("fCommentWriteTime").ascending());
        Page<FreeComment> topLevelComments = freeCommentRepository.findTopLevelCommentsByFreeBoard(board, pageable);

        String boardAuthor = board.getUser().getUsername();

        return topLevelComments.map(comment -> FreeCommentDTO.from(comment, boardAuthor));
    }

    // 페이징용 댓글 개수 조회 (최상위 댓글만)
    public long getTopLevelCommentCount(Long boardId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return freeCommentRepository.countTopLevelCommentsByFreeBoard(board);
    }

    // 댓글 작성
    public FreeCommentDTO createComment(Long boardId, FreeCommentRequestDTO dto, String username) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        FreeComment comment = new FreeComment();
        comment.setFCommentContent(dto.getFCommentContent());
        comment.setUser(user);
        comment.setFreeBoard(board);

        // 대댓글인 경우
        if (dto.getParentId() != null) {
            FreeComment parent = freeCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            comment.setParent(parent);
        }

        FreeComment saved = freeCommentRepository.save(comment);
        String boardAuthor = board.getUser().getUsername();

        return FreeCommentDTO.from(saved, boardAuthor);
    }

    // 댓글 수정
    public FreeCommentDTO updateComment(Long commentId, FreeCommentRequestDTO dto, String username) {
        FreeComment comment = freeCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        comment.setFCommentContent(dto.getFCommentContent());
        FreeComment updated = freeCommentRepository.save(comment);

        String boardAuthor = comment.getFreeBoard().getUser().getUsername();
        return FreeCommentDTO.from(updated, boardAuthor);
    }

    // 댓글 삭제 (soft delete)
    public void deleteComment(Long commentId, String username) {
        FreeComment comment = freeCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }

        // 자식 댓글이 있으면 soft delete, 없으면 hard delete
        if (comment.getChildren().isEmpty()) {
            freeCommentRepository.delete(comment);
        } else {
            comment.setFCommentDeleted(true);
            comment.setFCommentContent("삭제된 댓글입니다.");
            freeCommentRepository.save(comment);
        }
    }

    // 전체 댓글 개수 조회 (대댓글 포함 - 표시용)
    public long getCommentCount(Long boardId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return freeCommentRepository.countByFreeBoard(board);
    }
}