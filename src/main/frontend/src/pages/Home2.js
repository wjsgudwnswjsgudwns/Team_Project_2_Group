import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./Home2.css";
import api from "../api/axiosConfig";

function Home2() {
  const comm = process.env.REACT_APP_COMM_URL;
  const home1 = process.env.REACT_APP_HOME1_URL;
  const home2 = process.env.REACT_APP_HOME2_URL;
  const home3 = process.env.REACT_APP_HOME3_URL;
  const home4 = process.env.REACT_APP_HOME4_URL;

  const [recentPosts, setRecentPosts] = useState({
    infoBoardPosts: [],
    freeBoardPosts: [],
    counselBoardPosts: [],
  });
  const [loading, setLoading] = useState(true);
  const [currentBanner, setCurrentBanner] = useState(0);
  const navigate = useNavigate();

  const banners = [home1, home2, home3, home4];

  useEffect(() => {
    fetchRecentPosts();
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentBanner((prev) => (prev + 1) % banners.length);
    }, 3000); // 3초마다 자동 전환

    return () => clearInterval(interval);
  }, [banners.length]);

  const fetchRecentPosts = async () => {
    try {
      const response = await api.get("/api/home2/recent");
      setRecentPosts(response.data);
      setLoading(false);
    } catch (error) {
      console.error("최신 게시글 불러오기 실패:", error);
      setLoading(false);
    }
  };

  const handlePostClick = (boardType, postId) => {
    navigate(`/${boardType}?tab=detail&postId=${postId}`);
  };

  const handleBoardClick = (boardType) => {
    navigate(`/${boardType}`);
  };

  const truncateTitle = (title, maxLength = 20) => {
    if (title.length > maxLength) {
      return title.substring(0, maxLength) + "...";
    }
    return title;
  };

  const nextBanner = () => {
    setCurrentBanner((prev) => (prev + 1) % banners.length);
  };

  const prevBanner = () => {
    setCurrentBanner((prev) => (prev - 1 + banners.length) % banners.length);
  };

  const goToBanner = (index) => {
    setCurrentBanner(index);
  };

  const renderBoardSection = (title, posts, boardType) => (
    <div className="board-section">
      <div className="board-header">
        <h2>{title}</h2>
        <button
          className="more-btn"
          onClick={() => handleBoardClick(boardType)}
        >
          더보기 →
        </button>
      </div>
      <div className="posts-list">
        {posts.length === 0 ? (
          <div className="no-posts">게시글이 없습니다</div>
        ) : (
          posts.map((post) => (
            <div
              key={post.id}
              className="post-item"
              onClick={() => handlePostClick(boardType, post.id)}
            >
              <div className="post-title">{truncateTitle(post.title)}</div>
              <div className="post-comment-count">
                <img src={comm} alt="댓글 아이콘" />
                {post.commentCount}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="home2-container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="home2-container">
      <div className="banner-carousel">
        <button className="carousel-btn prev-btn" onClick={prevBanner}>
          ‹
        </button>

        <div className="banner-wrapper">
          <div
            className="banner-track"
            style={{ transform: `translateX(-${currentBanner * 100}%)` }}
          >
            {banners.map((banner, index) => (
              <img
                key={index}
                src={banner}
                alt={`배너 ${index + 1}`}
                className="banner-image"
              />
            ))}
          </div>
        </div>

        <button className="carousel-btn next-btn" onClick={nextBanner}>
          ›
        </button>

        <div className="carousel-indicators">
          {banners.map((_, index) => (
            <button
              key={index}
              className={`indicator ${index === currentBanner ? "active" : ""}`}
              onClick={() => goToBanner(index)}
            />
          ))}
        </div>
      </div>

      <div className="boards-grid">
        {renderBoardSection(
          "상담 게시판",
          recentPosts.counselBoardPosts,
          "counselboard",
          "#ffffffff"
        )}

        {renderBoardSection(
          "자유 게시판",
          recentPosts.freeBoardPosts,
          "freeboard",
          "#ffffffff"
        )}
        {renderBoardSection(
          "정보 게시판",
          recentPosts.infoBoardPosts,
          "infoboard",
          "#ffffffff"
        )}
      </div>
    </div>
  );
}

export default Home2;
