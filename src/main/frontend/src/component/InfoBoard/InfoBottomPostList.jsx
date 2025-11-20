import { useState, useEffect } from "react";
import api from "../../api/axiosConfig";
import "../FreeBoard/BottomPostList.css";

export default function InfoBottomPostList({
  currentPostId,
  initialPage = 0,
  onPostClick,
}) {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(initialPage);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    setPage(initialPage);
  }, [currentPostId, initialPage]);

  useEffect(() => {
    if (currentPostId) {
      fetchNearbyPosts(page);
    }
  }, [page, currentPostId]);

  const fetchNearbyPosts = async (pageNum) => {
    if (isLoading) return;

    setIsLoading(true);
    try {
      const res = await api.get(`/api/infoboard?page=${pageNum}&size=10`);

      if (res.data && res.data.content) {
        setPosts(res.data.content);
        setTotalPages(res.data.totalPages);
      } else {
        setPosts([]);
        setTotalPages(0);
      }
    } catch (err) {
      console.error("하단 게시글 목록 조회 실패:", err);
      setPosts([]);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handlePostItemClick = (postId) => {
    onPostClick(postId, page);
  };

  return (
    <div className="bottom-post-section">
      <h3 className="bottom-post-title">다른 게시글</h3>
      <div className="bottom-post-list">
        {isLoading ? (
          <div className="empty-message">로딩 중...</div>
        ) : posts.length === 0 ? (
          <div className="empty-message">게시글이 없습니다.</div>
        ) : (
          posts.map((post) => (
            <div
              key={post.id}
              className={`bottom-post-item ${
                post.id === currentPostId ? "current" : ""
              }`}
              onClick={() => handlePostItemClick(post.id)}
            >
              <div className="bottom-post-header">
                <h4 className="bottom-post-title-text">{post.ititle}</h4>
                {post.iFile && post.iFile !== "[]" && (
                  <span className="bottom-image-badge">📷</span>
                )}
              </div>
              <div className="bottom-post-meta">
                <span>{post.username}</span>
                <span>·</span>
                <span>조회 {post.iview}</span>
                <span>·</span>
                <span>
                  {post.iwriteTime
                    ? new Date(post.iwriteTime).toLocaleDateString("ko-KR")
                    : "-"}
                </span>
              </div>
            </div>
          ))
        )}
      </div>

      {totalPages > 1 && (
        <div className="bottom-post-pagination">
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0 || isLoading}
            className="bottom-page-btn"
          >
            이전
          </button>
          <span className="bottom-page-info">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1 || isLoading}
            className="bottom-page-btn"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
