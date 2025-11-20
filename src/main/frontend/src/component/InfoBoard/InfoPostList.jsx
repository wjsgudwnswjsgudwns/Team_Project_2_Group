// import logoImage from "../../images/logo-white-Photoroom.png";

export default function InfoPostList({
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

  const logoImage = process.env.REACT_APP_LOGO_URL;
  return (
    <div className="posts-list">
      {posts.map((post) => (
        <div
          key={post.id}
          className="info-post-item"
          onClick={() => onPostClick(post.id, currentPage)}
        >
          {/* ✅ 썸네일 영역 - 이미지 없으면 로고 표시 */}
          <div className="info-post-thumbnail">
            {post.firstImageUrl ? (
              <img src={post.firstImageUrl} alt={post.ititle} />
            ) : (
              <img
                src={logoImage}
                alt="Default Logo"
                className="info-post-default-logo"
              />
            )}
          </div>

          {/* ✅ 컨텐츠 영역 */}
          <div className="info-post-content">
            <div className="info-post-header">
              <h3 className="info-post-title">{post.ititle}</h3>
            </div>

            <div className="info-post-meta">
              <span>작성자: {post.username || "Unknown"}</span>
              <span>조회수: {post.iview}</span>
              <span>좋아요: {post.ilike}</span>
              <span>
                작성일:{" "}
                {post.iwriteTime
                  ? new Date(post.iwriteTime).toLocaleDateString("ko-KR")
                  : "-"}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
