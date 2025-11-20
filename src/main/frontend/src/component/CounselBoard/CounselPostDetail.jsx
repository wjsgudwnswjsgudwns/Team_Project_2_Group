import { useNavigate, useLocation } from "react-router-dom";
import CounselCommentSection from "./CounselCommentSection";
import CounselBottomPostList from "./CounselBottomPostList";

export default function CounselPostDetail({
  post,
  isLiked,
  currentUsername,
  onLike,
  onEdit,
  onDelete,
  onBack,
  onPostClick,
}) {
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthor = currentUsername && post.username === currentUsername;

  const urlParams = new URLSearchParams(location.search);
  const currentPage = parseInt(urlParams.get("page")) || 0;

  const handleBackClick = () => {
    onBack();
  };

  const handleBottomPostClick = (postId, fromPage) => {
    onPostClick(postId, fromPage);
  };

  return (
    <div className="content-box">
      <h2 className="detail-title">{post.ctitle}</h2>

      <div className="detail-meta">
        <div className="meta-left">
          <span
            style={{ cursor: "pointer", textDecoration: "none" }}
            onClick={() => navigate(`/user/${post.username}`)}
            title="프로필 보기"
          >
            작성자: {post.username}
          </span>
          <span>조회수: {post.cview}</span>
          <span>좋아요: {post.clike}</span>
          <span>
            작성일:{" "}
            {post.cwriteTime
              ? new Date(post.cwriteTime).toLocaleString("ko-KR")
              : "-"}
          </span>
        </div>
        {isAuthor && (
          <div className="meta-actions">
            <button onClick={onEdit} className="text-action-btn">
              수정
            </button>
            <span className="action-divider">|</span>
            <button onClick={onDelete} className="text-action-btn delete">
              삭제
            </button>
          </div>
        )}
      </div>

      <div
        className="detail-content"
        dangerouslySetInnerHTML={{ __html: post.ccontent }}
      />

      <div className="like-area">
        <button
          onClick={onLike}
          className="like-btn"
          style={{
            backgroundColor: isLiked ? "#ef4444" : "#f0f0f0",
            color: isLiked ? "white" : "#666",
          }}
        >
          {isLiked ? "❤️ 좋아요 취소" : "🤍 좋아요"}
        </button>
      </div>

      <CounselCommentSection
        boardId={post.id}
        currentUsername={currentUsername}
      />

      <CounselBottomPostList
        key={`${post.id}-${currentPage}`}
        currentPostId={post.id}
        initialPage={currentPage}
        onPostClick={handleBottomPostClick}
      />

      <div className="back-button-area">
        <button onClick={handleBackClick} className="back-btn">
          목록으로
        </button>
      </div>
    </div>
  );
}
