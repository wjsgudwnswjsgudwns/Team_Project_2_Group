import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import FreeCommentSection from "./FreeCommentSection";
import BottomPostList from "./BottomPostList";

export default function PostDetail({
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

  // URL에서 페이지 정보 추출
  const urlParams = new URLSearchParams(location.search);
  const currentPage = parseInt(urlParams.get("page")) || 0;

  const handleBackClick = () => {
    onBack();
  };

  // 하단 목록에서 게시글 클릭 시 현재 페이지 정보도 함께 전달
  const handleBottomPostClick = (postId, fromPage) => {
    onPostClick(postId, fromPage);
  };

  return (
    <div className="content-box">
      <h2 className="detail-title">{post.ftitle}</h2>

      <div className="detail-meta">
        <div className="meta-left">
          <span
            style={{ cursor: "pointer", textDecoration: "none" }}
            onClick={() => navigate(`/user/${post.username}`)}
            title="프로필 보기"
          >
            작성자: {post.username}
          </span>
          <span>조회수: {post.fview}</span>
          <span>좋아요: {post.flike}</span>
          <span>
            작성일:{" "}
            {post.fwriteTime
              ? new Date(post.fwriteTime).toLocaleString("ko-KR")
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
        dangerouslySetInnerHTML={{ __html: post.fcontent }}
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

      {/* 댓글 섹션 */}
      <FreeCommentSection boardId={post.id} currentUsername={currentUsername} />

      {/* 하단 게시글 목록 */}
      <BottomPostList
        key={`${post.id}-${currentPage}`} // key 추가로 강제 리마운트
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
