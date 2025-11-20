import { useState } from "react";

export default function CounselCommentItem({
  comment,
  currentUsername,
  depth = 0,
  onReplySubmit,
  onEdit,
  onDelete,
}) {
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.cCommentContent);
  const [isReplying, setIsReplying] = useState(false);
  const [replyContent, setReplyContent] = useState("");

  const isAuthor = currentUsername === comment.username;
  const maxDepth = 4;
  const actualDepth = Math.min(depth, maxDepth);

  const handleEditSubmit = () => {
    if (!editContent.trim()) {
      alert("댓글 내용을 입력하세요.");
      return;
    }
    onEdit(comment.id, editContent);
    setIsEditing(false);
  };

  const handleEditCancel = () => {
    setEditContent(comment.cCommentContent);
    setIsEditing(false);
  };

  const handleReplyClick = () => {
    setIsReplying(true);
  };

  const handleReplySubmit = async () => {
    if (!replyContent.trim()) {
      alert("답글 내용을 입력하세요.");
      return;
    }

    const success = await onReplySubmit(replyContent, comment.id);
    if (success) {
      setReplyContent("");
      setIsReplying(false);
    }
  };

  const handleReplyCancel = () => {
    setReplyContent("");
    setIsReplying(false);
  };

  return (
    <div
      className="comment-item-wrapper"
      style={{ marginLeft: depth > 0 ? "40px" : "0" }}
    >
      <div
        className={`comment-item ${comment.isAuthor ? "author-comment" : ""} ${
          comment.cCommentDeleted ? "deleted-comment" : ""
        }`}
      >
        <div className="comment-header">
          <span className="comment-username">
            {comment.username}
            {comment.isAuthor && <span className="author-badge">작성자</span>}
          </span>
          <span className="comment-date">
            {new Date(comment.cCommentWriteTime).toLocaleString("ko-KR")}
            {comment.cCommentUpdateTime && " (수정됨)"}
          </span>
        </div>

        {isEditing ? (
          <div className="comment-edit-form">
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              className="comment-edit-textarea"
              rows="3"
            />
            <div className="comment-edit-buttons">
              <button onClick={handleEditSubmit} className="comment-edit-btn">
                수정완료
              </button>
              <button onClick={handleEditCancel} className="comment-cancel-btn">
                취소
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="comment-content">{comment.cCommentContent}</div>

            {!comment.cCommentDeleted && (
              <div className="comment-actions">
                <button
                  onClick={handleReplyClick}
                  className="comment-action-btn"
                >
                  답글
                </button>
                {isAuthor && (
                  <>
                    <button
                      onClick={() => setIsEditing(true)}
                      className="comment-action-btn"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => onDelete(comment.id)}
                      className="comment-action-btn delete"
                    >
                      삭제
                    </button>
                  </>
                )}
              </div>
            )}
          </>
        )}
      </div>

      {isReplying && (
        <div
          className="reply-form"
          style={{ marginLeft: depth > 0 ? "0" : "40px" }}
        >
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글을 입력하세요"
            className="comment-textarea"
            rows="2"
          />
          <div className="reply-form-buttons">
            <button
              onClick={handleReplySubmit}
              className="comment-submit-btn small"
            >
              답글 작성
            </button>
            <button
              onClick={handleReplyCancel}
              className="comment-cancel-btn small"
            >
              취소
            </button>
          </div>
        </div>
      )}

      {comment.children && comment.children.length > 0 && (
        <>
          {comment.children.map((child) => (
            <CounselCommentItem
              key={child.id}
              comment={child}
              currentUsername={currentUsername}
              depth={depth + 1}
              onReplySubmit={onReplySubmit}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </>
      )}
    </div>
  );
}
