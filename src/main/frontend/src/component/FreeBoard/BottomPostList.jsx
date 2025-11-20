import { useState, useEffect } from "react";
import api from "../../api/axiosConfig";
import "./BottomPostList.css";

export default function BottomPostList({
  currentPostId,
  initialPage = 0,
  onPostClick,
}) {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(initialPage); // ✅ 초기값을 initialPage로 설정
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // ✅ initialPage 변경 시 page 상태 업데이트
  useEffect(() => {
    console.log("🔵 BottomPostList - initialPage 변경:", {
      currentPostId,
      initialPage,
    });
    setPage(initialPage);
  }, [currentPostId, initialPage]);

  // ✅ page 또는 currentPostId 변경 시 fetch
  useEffect(() => {
    if (currentPostId) {
      console.log("🟢 페이지로 fetch:", page);
      fetchNearbyPosts(page);
    }
  }, [page, currentPostId]);

  const fetchNearbyPosts = async (pageNum) => {
    if (isLoading) return;

    setIsLoading(true);
    try {
      console.log("📡 API 요청:", `/api/freeboard?page=${pageNum}&size=10`);

      const res = await api.get(`/api/freeboard?page=${pageNum}&size=10`);

      console.log("📥 API 응답:", res.data);

      if (res.data && res.data.content) {
        setPosts(res.data.content);
        setTotalPages(res.data.totalPages);
        console.log("✅ 데이터 설정 완료:", {
          posts: res.data.content.length,
          totalPages: res.data.totalPages,
          currentPage: pageNum,
        });
      } else {
        console.error("❌ content가 없음:", res.data);
        setPosts([]);
        setTotalPages(0);
      }
    } catch (err) {
      console.error("❌ 하단 게시글 목록 조회 실패:", err);
      setPosts([]);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    console.log("🔄 페이지 변경:", page, "->", newPage);
    setPage(newPage);
  };

  // ✅ 게시글 클릭 시 현재 page도 함께 전달
  const handlePostItemClick = (postId) => {
    console.log("🖱️ 게시글 클릭:", postId, "현재 페이지:", page);
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
                <h4 className="bottom-post-title-text">{post.ftitle}</h4>
                {post.fFile && post.fFile !== "[]" && (
                  <span className="bottom-image-badge">📷</span>
                )}
              </div>
              <div className="bottom-post-meta">
                <span>{post.username}</span>
                <span>·</span>
                <span>조회 {post.fview}</span>
                <span>·</span>
                <span>
                  {post.fwriteTime
                    ? new Date(post.fwriteTime).toLocaleDateString("ko-KR")
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

      <div style={{ fontSize: "12px", color: "#999", marginTop: "10px" }}>
        현재: {page + 1}페이지 / 총 {totalPages}페이지 / 게시글 {posts.length}개
        / initialPage: {initialPage}
      </div>
    </div>
  );
}
