import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useCounselBoard } from "../hooks/useCounselBoard";
import { useAuth } from "../hooks/useAuth";
import { useSearch } from "../hooks/useSearch";
import CounselPostList from "../component/CounselBoard/CounselPostList";
import CounselPostDetail from "../component/CounselBoard/CounselPostDetail";
import CounselPostForm from "../component/CounselBoard/CounselPostForm";
import SearchBar from "../component/FreeBoard/SearchBar";
import "./FreeBoard.css";

export default function CounselBoard() {
  const navigate = useNavigate();
  const location = useLocation();

  const [activeTab, setActiveTab] = useState("posts");
  const [formData, setFormData] = useState({ cTitle: "", cContent: "" });
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const isNavigatingRef = useRef(false);

  const { currentUsername } = useAuth();
  const {
    posts,
    selectedPost,
    page,
    totalPages,
    isLiked,
    setPage,
    setSearchResults,
    fetchPosts,
    fetchPostDetail,
    createPost,
    updatePost,
    deletePost,
    toggleLike,
  } = useCounselBoard();

  const {
    searchType,
    searchKeyword,
    isSearching,
    setSearchType,
    setSearchKeyword,
    handleSearch,
    resetSearch,
  } = useSearch();

  useEffect(() => {
    if (isNavigatingRef.current) {
      isNavigatingRef.current = false;
      return;
    }

    const params = new URLSearchParams(location.search);
    const tab = params.get("tab");
    const postId = params.get("postId");

    if (postId && tab === "detail") {
      const id = parseInt(postId);
      if (!selectedPost || selectedPost.id !== id) {
        fetchPostDetail(id);
      }
      setActiveTab("detail");
    } else if (tab === "write") {
      // 로그인 체크
      if (!currentUsername) {
        alert("로그인이 필요한 서비스입니다.");
        navigate("/login");
        return;
      }
      setActiveTab("write");
    } else {
      setActiveTab("posts");
    }
  }, [location.search]);

  useEffect(() => {
    if (activeTab === "posts") {
      fetchPosts();
    }
  }, [page, fetchPosts, activeTab]);

  const handlePostClick = async (id, fromPage = page) => {
    isNavigatingRef.current = true;
    await fetchPostDetail(id);
    setActiveTab("detail");
    const targetPage = fromPage !== undefined ? fromPage : page;
    navigate(`/counselboard?tab=detail&postId=${id}&page=${targetPage}`, {
      replace: false,
    });
  };

  const handleBackToList = () => {
    isNavigatingRef.current = true;
    setActiveTab("posts");
    navigate("/counselboard?tab=posts", { replace: false });
  };

  const handleSubmit = async () => {
    if (!formData.cTitle.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!formData.cContent.trim() || formData.cContent === "<br>") {
      alert("내용을 입력해주세요.");
      return;
    }

    const success = isEditing
      ? await updatePost(editingId, formData)
      : await createPost(formData);

    if (success) {
      setFormData({ cTitle: "", cContent: "" });
      setIsEditing(false);
      setEditingId(null);
      fetchPosts();
      isNavigatingRef.current = true;
      setActiveTab("posts");
      navigate("/counselboard?tab=posts", { replace: true });
    }
  };

  const startEdit = (post) => {
    isNavigatingRef.current = true;
    setIsEditing(true);
    setEditingId(post.id);
    setFormData({
      cTitle: post.ctitle,
      cContent: post.ccontent,
    });
    setActiveTab("write");
    navigate("/counselboard?tab=write", { replace: false });
  };

  const handleDeleteClick = async () => {
    const success = await deletePost(selectedPost.id);
    if (success) {
      fetchPosts();
      isNavigatingRef.current = true;
      setActiveTab("posts");
      navigate("/counselboard?tab=posts", { replace: true });
    }
  };

  const handleCancelEdit = () => {
    isNavigatingRef.current = true;
    setIsEditing(false);
    setEditingId(null);
    setFormData({ cTitle: "", cContent: "" });
    setActiveTab("posts");
    navigate("/counselboard?tab=posts", { replace: false });
  };

  const handleSearchClick = () => {
    handleSearch((data) => {
      setSearchResults(data);
      setPage(0);
      setActiveTab("posts");
    });
  };

  const handleResetSearch = () => {
    resetSearch(() => {
      setPage(0);
      fetchPosts();
    });
  };

  return (
    <div className="freeboard-container">
      <div className="freeboard-wrapper">
        <div className="tab-buttons">
          <button
            onClick={() => {
              isNavigatingRef.current = true;
              setActiveTab("posts");
              navigate("/counselboard?tab=posts", { replace: false });
              fetchPosts();
            }}
            className={`tab-btn ${activeTab === "posts" ? "active" : ""}`}
          >
            게시글 목록
          </button>
          <button
            onClick={() => {
              // 로그인 한 사람만 글쓰기 가능
              if (!currentUsername) {
                alert("로그인 후 작성 가능합니다.");
                navigate("/login");
                return;
              }
              isNavigatingRef.current = true;
              setActiveTab("write");
              navigate("/counselboard?tab=write", { replace: false });
              setIsEditing(false);
              setFormData({ cTitle: "", cContent: "" });
            }}
            className={`tab-btn ${activeTab === "write" ? "active" : ""}`}
          >
            글쓰기
          </button>
        </div>

        {activeTab === "posts" && (
          <div className="content-box">
            <SearchBar
              searchType={searchType}
              searchKeyword={searchKeyword}
              isSearching={isSearching}
              onSearchTypeChange={setSearchType}
              onSearchKeywordChange={setSearchKeyword}
              onSearch={handleSearchClick}
              onReset={handleResetSearch}
            />

            {isSearching && (
              <div className="search-result-text">
                검색결과: "{searchKeyword}"
              </div>
            )}

            <CounselPostList
              posts={posts}
              onPostClick={handlePostClick}
              isSearching={isSearching}
              currentPage={page}
            />

            <div className="pagination">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="page-btn"
              >
                이전
              </button>
              <span className="page-info">
                {page + 1} / {totalPages || 1}
              </span>
              <button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="page-btn"
              >
                다음
              </button>
            </div>
          </div>
        )}

        {activeTab === "write" && (
          <div className="content-box">
            <h2 className="section-title">
              {isEditing ? "게시글 수정" : "게시글 작성"}
            </h2>
            <CounselPostForm
              formData={formData}
              isEditing={isEditing}
              onFormChange={setFormData}
              onSubmit={handleSubmit}
              onCancel={handleCancelEdit}
            />
          </div>
        )}

        {activeTab === "detail" && selectedPost && (
          <CounselPostDetail
            post={selectedPost}
            isLiked={isLiked}
            currentUsername={currentUsername}
            onLike={() => toggleLike(selectedPost.id)}
            onEdit={() => startEdit(selectedPost)}
            onDelete={handleDeleteClick}
            onBack={handleBackToList}
            onPostClick={handlePostClick}
          />
        )}
      </div>
    </div>
  );
}
