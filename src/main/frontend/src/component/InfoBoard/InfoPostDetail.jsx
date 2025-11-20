import { useNavigate, useLocation } from "react-router-dom";
import InfoCommentSection from "./InfoCommentSection";
import InfoBottomPostList from "./InfoBottomPostList";

export default function InfoPostDetail({
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
      <h2 className="detail-title">{post.ititle}</h2>

      <div className="detail-meta">
        <div className="meta-left">
          <span
            style={{ cursor: "pointer", textDecoration: "none" }}
            onClick={() => navigate(`/user/${post.username}`)}
            title="프로필 보기"
          >
            작성자: {post.username}
          </span>
          <span>조회수: {post.iview}</span>
          <span>좋아요: {post.ilike}</span>
          <span>
            작성일:{" "}
            {post.iwriteTime
              ? new Date(post.iwriteTime).toLocaleString("ko-KR")
              : "-"}
          </span>
        </div>
        {isAuthor && (
          <div className="meta-actions">
            <button onClick={onEdit} className="text-action-btn">
              ✏️ 수정
            </button>
            <span className="action-divider">|</span>
            <button onClick={onDelete} className="text-action-btn delete">
              🗑️ 삭제
            </button>
          </div>
        )}
      </div>

      <div
        className="detail-content"
        dangerouslySetInnerHTML={{ __html: post.icontent }}
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

      <InfoCommentSection boardId={post.id} currentUsername={currentUsername} />

      <InfoBottomPostList
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
