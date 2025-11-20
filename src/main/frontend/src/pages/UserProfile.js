import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./MyPage.css";

export default function UserProfile() {
  const { username } = useParams();
  const navigate = useNavigate();

  const [userData, setUserData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchUserProfile();
  }, [username]);

  const fetchUserProfile = async () => {
    setIsLoading(true);
    try {
      const res = await api.get(`/api/user/profile/${username}`);
      setUserData(res.data);
    } catch (err) {
      alert("사용자 정보를 불러올 수 없습니다: " + err.message);
      navigate(-1);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePostClick = (post) => {
    const boardMap = {
      free: "freeboard",
      counsel: "counselboard",
      info: "infoboard",
    };
    const board = boardMap[post.boardType];
    navigate(`/${board}?tab=detail&postId=${post.id}`);
  };

  const handleCommentClick = (comment) => {
    const boardMap = {
      free: "freeboard",
      counsel: "counselboard",
      info: "infoboard",
    };
    const board = boardMap[comment.boardType];
    navigate(`/${board}?tab=detail&postId=${comment.boardId}`);
  };

  if (isLoading || !userData) {
    return (
      <div className="mypage-container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="mypage-container">
      <div className="mypage-wrapper">
        <div className="mypage-content-box">
          <h3 className="section-title">{username}님의 프로필</h3>

          <div className="user-info-grid">
            <div className="info-item">
              <div className="info-label">닉네임</div>
              <div className="info-value">{userData.nickname}</div>
            </div>
            <div className="info-item">
              <div className="info-label">가입일</div>
              <div className="info-value">
                {new Date(userData.createAccount).toLocaleDateString("ko-KR")}
              </div>
            </div>
          </div>

          <h3 className="section-title">활동 통계</h3>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-number">
                {userData.activityStats.totalPosts}
              </div>
              <div className="stat-label">작성 게시글</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">
                {userData.activityStats.totalComments}
              </div>
              <div className="stat-label">작성 댓글</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">
                {userData.activityStats.totalLikesReceived}
              </div>
              <div className="stat-label">받은 좋아요</div>
            </div>
          </div>

          <h3 className="section-title">최근 작성 글</h3>
          {userData.recentPosts.length === 0 ? (
            <div className="empty-message">작성한 게시글이 없습니다.</div>
          ) : (
            <div className="post-list">
              {userData.recentPosts.map((post) => (
                <div
                  key={`${post.boardType}-${post.id}`}
                  className="post-item"
                  onClick={() => handlePostClick(post)}
                >
                  <div className="post-title">
                    <span className="board-type-badge">
                      {post.boardType === "free"
                        ? "자유"
                        : post.boardType === "counsel"
                        ? "상담"
                        : "정보"}
                    </span>
                    {post.title}
                  </div>
                  <div className="post-meta">
                    <span>
                      {new Date(post.writeTime).toLocaleDateString("ko-KR")}
                    </span>
                    <span>·</span>
                    <span>조회 {post.views}</span>
                    <span>·</span>
                    <span>좋아요 {post.likes}</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          <h3 className="section-title" style={{ marginTop: "48px" }}>
            최근 작성 댓글
          </h3>
          {userData.recentComments.length === 0 ? (
            <div className="empty-message">작성한 댓글이 없습니다.</div>
          ) : (
            <div className="comment-list">
              {userData.recentComments.map((comment) => (
                <div
                  key={`${comment.boardType}-${comment.id}`}
                  className="comment-item"
                  onClick={() => handleCommentClick(comment)}
                >
                  <div className="comment-board-title">
                    <span className="board-type-badge">
                      {comment.boardType === "free"
                        ? "자유"
                        : comment.boardType === "counsel"
                        ? "상담"
                        : "정보"}
                    </span>
                    {comment.boardTitle}
                  </div>
                  <div className="comment-content">{comment.content}</div>
                  <div className="comment-meta">
                    <span>
                      {new Date(comment.writeTime).toLocaleDateString("ko-KR")}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="button-group">
            <button onClick={() => navigate(-1)} className="primary-btn">
              뒤로가기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
