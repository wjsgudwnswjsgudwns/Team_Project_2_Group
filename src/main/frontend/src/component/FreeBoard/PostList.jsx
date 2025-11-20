export default function PostList({
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
          onClick={() => {
            console.log("🟡 PostList 클릭:", { postId: post.id, currentPage });
            onPostClick(post.id, currentPage);
          }}
        >
          <div className="post-header">
            <h3 className="post-title">{post.ftitle}</h3>

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
            <span>조회수: {post.fview}</span>
            <span>좋아요: {post.flike}</span>
            <span>
              작성일:{" "}
              {post.fwriteTime
                ? new Date(post.fwriteTime).toLocaleDateString("ko-KR")
                : "-"}
            </span>
          </div>

          {/* ✅ 호버 시 미리보기 팝업 */}
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
