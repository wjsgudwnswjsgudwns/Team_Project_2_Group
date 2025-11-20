import { useState, useEffect } from "react";
import { useCounselComment } from "../../hooks/useCounselComment";
import CounselCommentItem from "./CounselCommentItem";
import "../FreeBoard/FreeComment.css";
import api from "../../api/axiosConfig";

export default function CounselCommentSection({ boardId, currentUsername }) {
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
  } = useCounselComment();

  const [newComment, setNewComment] = useState("");

  useEffect(() => {
    if (boardId) {
      const initializeComments = async () => {
        try {
          const countRes = await api.get(
            `/api/counselboard/${boardId}/comments/count/toplevel`
          );
          const topLevelCount = countRes.data.count;

          await fetchCommentCount(boardId);

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

  const handleCommentPageChange = (newPage) => {
    setCommentPage(newPage);
    fetchComments(boardId, newPage);
  };

  const handleSubmitComment = async () => {
    if (!newComment.trim()) {
      alert("댓글 내용을 입력하세요.");
      return;
    }

    const success = await createComment(boardId, newComment);
    if (success) {
      setNewComment("");
      const lastPage = Math.max(0, commentTotalPages - 1);
      setCommentPage(lastPage);
      fetchComments(boardId, lastPage);
    }
  };

  const handleReplySubmit = async (content, parentId) => {
    const success = await createComment(boardId, content, parentId);
    return success;
  };

  const handleEdit = async (commentId, content) => {
    await updateComment(boardId, commentId, content);
  };

  const handleDelete = async (commentId) => {
    await deleteComment(boardId, commentId);
  };

  return (
    <div className="comment-section">
      <div className="comment-header-bar">
        <h3>댓글 {commentCount}개</h3>
      </div>

      <div className="comment-list">
        {comments.length === 0 ? (
          <div className="empty-comment">첫 댓글을 작성해보세요!</div>
        ) : (
          comments.map((comment) => (
            <CounselCommentItem
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
