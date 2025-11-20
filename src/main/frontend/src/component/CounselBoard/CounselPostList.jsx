export default function CounselPostList({
  posts,
  onPostClick,
  isSearching,
  currentPage,
}) {
  if (!posts || !Array.isArray(posts) || posts.length === 0) {
    return (
      <div className="empty-message">
        {isSearching ? "검색 결과가 없습니다." : "게시글이 없습니다."}
      </div>
    );
  }

  return (
    <div className="posts-list">
      {posts.map((post) => (
        <div
          key={post.id}
          className="post-item"
          onClick={() => onPostClick(post.id, currentPage)}
        >
          <div className="post-header">
            <h3 className="post-title">{post.ctitle}</h3>

            {/* ✅ 이미지 있으면 단순 아이콘 표시 */}
            {post.imageCount > 0 && (
              <div className="image-icon">
                <div className="image-icon-box"></div>
                <div className="image-icon-box"></div>
              </div>
            )}
          </div>

          <div className="post-meta">
            <span>작성자: {post.username || "Unknown"}</span>
            <span>조회수: {post.cview}</span>
            <span>좋아요: {post.clike}</span>
            <span>
              작성일:{" "}
              {post.cwriteTime
                ? new Date(post.cwriteTime).toLocaleDateString("ko-KR")
                : "-"}
            </span>
          </div>

          {/* ✅ 호버 시 미리보기 팝업 - 헤더 제거, 패딩 없이 */}
          {post.firstImageUrl && (
            <div className="image-preview-popup">
              <img
                src={post.firstImageUrl}
                alt="미리보기"
                className="preview-popup-image"
              />
              {post.imageCount > 1 && (
                <div className="preview-popup-count">
                  +{post.imageCount - 1} more
                </div>
              )}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
