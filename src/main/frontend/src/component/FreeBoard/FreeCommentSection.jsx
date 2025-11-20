import { useState, useEffect } from "react";
import { useFreeComment } from "../../hooks/useFreeComment";
import FreeCommentItem from "./FreeCommentItem";
import "./FreeComment.css";
import api from "../../api/axiosConfig";

export default function FreeCommentSection({ boardId, currentUsername }) {
  const {
    comments,
    commentCount,
    commentPage,
    commentTotalPages,
    setCommentPage,
    fetchComments,
    fetchCommentCount,
    createComment,
    updateComment,
    deleteComment,
  } = useFreeComment();

  const [newComment, setNewComment] = useState("");

  useEffect(() => {
    if (boardId) {
      const initializeComments = async () => {
        try {
          // 최상위 댓글 개수로 페이지 계산
          const countRes = await api.get(
            `/api/freeboard/${boardId}/comments/count/toplevel`
          );
          const topLevelCount = countRes.data.count;

          // 전체 댓글 개수도 가져오기 (표시용)
          await fetchCommentCount(boardId);

          // 페이지당 10개씩
          const totalPages = Math.ceil(topLevelCount / 10);
          const lastPage = Math.max(0, totalPages - 1);

          setCommentPage(lastPage);
          await fetchComments(boardId, lastPage);
        } catch (err) {
          console.error("댓글 초기화 실패:", err);
        }
      };

      initializeComments();
    }
  }, [boardId]);

  // 댓글 페이지 변경
  const handleCommentPageChange = (newPage) => {
    setCommentPage(newPage);
    fetchComments(boardId, newPage);
  };

  // 새 댓글 작성
  const handleSubmitComment = async () => {
    if (!newComment.trim()) {
      alert("댓글 내용을 입력하세요.");
      return;
    }

    const success = await createComment(boardId, newComment);
    if (success) {
      setNewComment("");
      // 🔥 댓글 작성 후 마지막 페이지로 이동
      const lastPage = Math.max(0, commentTotalPages - 1);
      setCommentPage(lastPage);
      fetchComments(boardId, lastPage);
    }
  };

  // 답글 작성
  const handleReplySubmit = async (content, parentId) => {
    const success = await createComment(boardId, content, parentId);
    return success;
  };

  // 댓글 수정
  const handleEdit = async (commentId, content) => {
    await updateComment(boardId, commentId, content);
  };

  // 댓글 삭제
  const handleDelete = async (commentId) => {
    await deleteComment(boardId, commentId);
  };

  return (
    <div className="comment-section">
      <div className="comment-header-bar">
        <h3>댓글 {commentCount}개</h3>
      </div>

      {/* 댓글 목록 */}
      <div className="comment-list">
        {comments.length === 0 ? (
          <div className="empty-comment">첫 댓글을 작성해보세요!</div>
        ) : (
          comments.map((comment) => (
            <FreeCommentItem
              key={comment.id}
              comment={comment}
              currentUsername={currentUsername}
              depth={0}
              onReplySubmit={handleReplySubmit}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))
        )}
      </div>

      {/* 댓글 페이지네이션 */}
      {commentTotalPages > 1 && (
        <div className="comment-pagination">
          <button
            onClick={() => handleCommentPageChange(commentPage - 1)}
            disabled={commentPage === 0}
            className="comment-page-btn"
          >
            이전
          </button>
          <span className="comment-page-info">
            {commentPage + 1} / {commentTotalPages}
          </span>
          <button
            onClick={() => handleCommentPageChange(commentPage + 1)}
            disabled={commentPage >= commentTotalPages - 1}
            className="comment-page-btn"
          >
            다음
          </button>
        </div>
      )}

      {/* 댓글 작성 폼 */}
      <div className="comment-form">
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="댓글을 입력하세요"
          className="comment-textarea"
          rows="3"
        />
        <button onClick={handleSubmitComment} className="comment-submit-btn">
          댓글 작성
        </button>
      </div>
    </div>
  );
}
