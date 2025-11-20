import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useFreeBoard } from "../hooks/useFreeBoard";
import { useAuth } from "../hooks/useAuth";
import { useSearch } from "../hooks/useSearch";
import PostList from "../component/FreeBoard/PostList";
import PostDetail from "../component/FreeBoard/PostDetail";
import PostForm from "../component/FreeBoard/PostForm";
import SearchBar from "../component/FreeBoard/SearchBar";
import "./FreeBoard.css";

export default function FreeBoard() {
  const navigate = useNavigate();
  const location = useLocation();

  const [activeTab, setActiveTab] = useState("posts");
  const [formData, setFormData] = useState({ fTitle: "", fContent: "" });
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);

  // ✅ 중복 navigate 방지용 ref
  const isNavigatingRef = useRef(false);

  // Custom Hooks
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
  } = useFreeBoard();

  const {
    searchType,
    searchKeyword,
    isSearching,
    setSearchType,
    setSearchKeyword,
    handleSearch,
    resetSearch,
  } = useSearch();

  // ✅ URL 파라미터로 activeTab 관리 (navigate 호출 없이)
  useEffect(() => {
    if (isNavigatingRef.current) {
      isNavigatingRef.current = false;
      return;
    }

    const params = new URLSearchParams(location.search);
    const tab = params.get("tab");
    const postId = params.get("postId");

    if (postId && tab === "detail") {
      // 게시글 상세 보기 (navigate 호출 없이 데이터만 fetch)
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

  // // 게시글 클릭 (히스토리 1개만 추가)
  // const handlePostClick = async (id, fromPage = page) => {
  //   isNavigatingRef.current = true;

  //   await fetchPostDetail(id);
  //   setActiveTab("detail");

  //   // fromPage가 명시적으로 전달되지 않으면 현재 page 사용
  //   const targetPage = fromPage !== undefined ? fromPage : page; // ✅ undefined 체크
  //   navigate(`/freeboard?tab=detail&postId=${id}&page=${targetPage}`, {
  //     replace: false,
  //   });
  // };

  const handlePostClick = async (id, fromPage = page) => {
    console.log("🔴 handlePostClick 호출:", {
      id,
      fromPage,
      currentPage: page,
      fromPageType: typeof fromPage,
    });

    isNavigatingRef.current = true;

    await fetchPostDetail(id);
    setActiveTab("detail");

    const targetPage = fromPage !== undefined ? fromPage : page;

    console.log("🔴 navigate할 페이지:", targetPage);

    navigate(`/freeboard?tab=detail&postId=${id}&page=${targetPage}`, {
      replace: false,
    });
  };

  // 목록으로 돌아가기 (replace 사용)
  const handleBackToList = () => {
    isNavigatingRef.current = true;
    setActiveTab("posts");
    navigate("/freeboard?tab=posts", { replace: false });
  };

  // 글쓰기/수정 제출
  const handleSubmit = async () => {
    if (!formData.fTitle.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!formData.fContent.trim() || formData.fContent === "<br>") {
      alert("내용을 입력해주세요.");
      return;
    }

    const success = isEditing
      ? await updatePost(editingId, formData)
      : await createPost(formData);

    if (success) {
      setFormData({ fTitle: "", fContent: "" });
      setIsEditing(false);
      setEditingId(null);
      fetchPosts();
      isNavigatingRef.current = true;
      setActiveTab("posts");
      navigate("/freeboard?tab=posts", { replace: true });
    }
  };

  // 수정 시작
  const startEdit = (post) => {
    isNavigatingRef.current = true;
    setIsEditing(true);
    setEditingId(post.id);
    setFormData({
      fTitle: post.ftitle,
      fContent: post.fcontent,
    });
    setActiveTab("write");
    navigate("/freeboard?tab=write", { replace: false });
  };

  // 삭제
  const handleDeleteClick = async () => {
    const success = await deletePost(selectedPost.id);
    if (success) {
      fetchPosts();
      isNavigatingRef.current = true;
      setActiveTab("posts");
      navigate("/freeboard?tab=posts", { replace: true });
    }
  };

  // 수정 취소
  const handleCancelEdit = () => {
    isNavigatingRef.current = true;
    setIsEditing(false);
    setEditingId(null);
    setFormData({ fTitle: "", fContent: "" });
    setActiveTab("posts");
    navigate("/freeboard?tab=posts", { replace: false });
  };

  // 검색
  const handleSearchClick = () => {
    handleSearch((data) => {
      setSearchResults(data);
      setPage(0);
      setActiveTab("posts");
    });
  };

  // 검색 초기화
  const handleResetSearch = () => {
    resetSearch(() => {
      setPage(0);
      fetchPosts();
    });
  };

  return (
    <div className="freeboard-container">
      <div className="freeboard-wrapper">
        {/* 탭 버튼 */}
        <div className="tab-buttons">
          <button
            onClick={() => {
              isNavigatingRef.current = true;
              setActiveTab("posts");
              navigate("/freeboard?tab=posts", { replace: false });
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
              navigate("/freeboard?tab=write", { replace: false });
              setIsEditing(false);
              setFormData({ fTitle: "", fContent: "" });
            }}
            className={`tab-btn ${activeTab === "write" ? "active" : ""}`}
          >
            글쓰기
          </button>
        </div>

        {/* 게시글 목록 */}
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

            <PostList
              posts={posts}
              onPostClick={handlePostClick}
              isSearching={isSearching}
              currentPage={page}
            />

            {/* 페이지네이션 */}
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

        {/* 글쓰기/수정 */}
        {activeTab === "write" && (
          <div className="content-box">
            <h2 className="section-title">
              {isEditing ? "게시글 수정" : "게시글 작성"}
            </h2>
            <PostForm
              formData={formData}
              isEditing={isEditing}
              onFormChange={setFormData}
              onSubmit={handleSubmit}
              onCancel={handleCancelEdit}
            />
          </div>
        )}

        {/* 게시글 상세 */}
        {activeTab === "detail" && selectedPost && (
          <PostDetail
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
