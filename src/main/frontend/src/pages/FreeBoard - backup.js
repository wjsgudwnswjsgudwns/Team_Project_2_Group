import { useState, useEffect, useRef } from "react";
import api from "../api/axiosConfig";
import "./FreeBoard.css";

export default function FreeBoard() {
  const [posts, setPosts] = useState([]);
  const [selectedPost, setSelectedPost] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [formData, setFormData] = useState({
    fTitle: "",
    fContent: "",
  });

  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [activeTab, setActiveTab] = useState("posts");

  // 좋아요 상태
  const [isLiked, setIsLiked] = useState(false);

  // 현재 로그인한 사용자
  const [currentUsername, setCurrentUsername] = useState(null);

  // 검색 기능
  const [searchType, setSearchType] = useState("all");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  const contentEditableRef = useRef(null);
  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchPosts();
    fetchCurrentUser();
  }, [page]);

  useEffect(() => {
    if (isEditing && formData.fContent && contentEditableRef.current) {
      contentEditableRef.current.innerHTML = formData.fContent;
    }
  }, [isEditing, activeTab]);

  // 현재 로그인한 사용자 정보 조회
  const fetchCurrentUser = async () => {
    try {
      const res = await api.get("/api/auth/me");
      setCurrentUsername(res.data.username);
    } catch (err) {
      console.error("사용자 정보 조회 실패:", err);
    }
  };

  // 게시글 목록 조회
  const fetchPosts = async () => {
    try {
      let url;
      if (isSearching && searchKeyword) {
        url = `/api/freeboard/search?searchType=${searchType}&keyword=${encodeURIComponent(
          searchKeyword
        )}&page=${page}&size=10`;
      } else {
        url = `/api/freeboard?page=${page}&size=10`;
      }

      const res = await api.get(url);

      // // 실제 응답 확인
      // console.log("=== 백엔드 응답 확인 ===");
      // console.log("첫 번째 게시글:", res.data.content[0]);
      // if (res.data.content[0]) {
      //   console.log("fTitle (camelCase):", res.data.content[0].fTitle);
      //   console.log("ftitle (소문자):", res.data.content[0].ftitle);
      // }

      setPosts(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      alert("게시글 조회 실패: " + err.message);
    }
  };

  // 게시글 상세 조회
  const fetchPostDetail = async (id) => {
    try {
      const res = await api.get(`/api/freeboard/${id}`);
      setSelectedPost(res.data);

      // 좋아요 상태 확인
      const likeRes = await api.get(`/api/freeboard/${id}/like/status`);
      setIsLiked(likeRes.data.isLiked);

      setActiveTab("detail");
    } catch (err) {
      alert("게시글 상세 조회 실패: " + err.message);
    }
  };

  // 이미지 삽입
  const handleImageInsert = (e) => {
    const files = Array.from(e.target.files);

    files.forEach((file) => {
      if (file.size > 5 * 1024 * 1024) {
        alert(`${file.name}은(는) 5MB를 초과합니다.`);
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        const img = document.createElement("img");
        img.src = reader.result;
        img.style.maxWidth = "100%";
        img.style.height = "auto";
        img.style.margin = "10px 0";

        if (contentEditableRef.current) {
          contentEditableRef.current.focus();

          const selection = window.getSelection();
          if (selection.rangeCount > 0) {
            const range = selection.getRangeAt(0);
            range.deleteContents();
            range.insertNode(img);

            range.setStartAfter(img);
            range.setEndAfter(img);
            selection.removeAllRanges();
            selection.addRange(range);
          }

          setFormData((prev) => ({
            ...prev,
            fContent: contentEditableRef.current.innerHTML,
          }));
        }
      };
      reader.readAsDataURL(file);
    });

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  // contentEditable 내용 변경
  const handleContentChange = () => {
    if (contentEditableRef.current) {
      setFormData((prev) => ({
        ...prev,
        fContent: contentEditableRef.current.innerHTML,
      }));
    }
  };

  // 게시글 작성/수정
  const handleSubmit = async () => {
    if (!formData.fTitle.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }

    if (!formData.fContent.trim() || formData.fContent === "<br>") {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      const submitData = {
        fTitle: formData.fTitle,
        fContent: formData.fContent,
        fFile: "",
      };

      if (isEditing) {
        await api.put(`/api/freeboard/${editingId}`, submitData);
        alert("수정 완료!");
      } else {
        await api.post("/api/freeboard", submitData);
        alert("작성 완료!");
      }

      setFormData({ fTitle: "", fContent: "" });
      if (contentEditableRef.current) {
        contentEditableRef.current.innerHTML = "";
      }
      setIsEditing(false);
      setEditingId(null);
      fetchPosts();
      setActiveTab("posts");
    } catch (err) {
      alert("작업 실패: " + err.message);
    }
  };

  // 게시글 삭제
  const handleDelete = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;

    try {
      await api.delete(`/api/freeboard/${id}`);
      alert("삭제 완료!");
      fetchPosts();
      setActiveTab("posts");
    } catch (err) {
      alert("삭제 실패: " + err.message);
    }
  };

  // 좋아요 토글
  const handleLikeToggle = async () => {
    try {
      const res = await api.post(`/api/freeboard/${selectedPost.id}/like`);
      setIsLiked(res.data.isLiked);
      await fetchPostDetail(selectedPost.id);
      alert(res.data.message);
    } catch (err) {
      alert("좋아요 처리 실패: " + err.message);
    }
  };

  // 검색
  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      alert("검색어를 입력하세요.");
      return;
    }

    try {
      setIsSearching(true);
      setPage(0);

      const res = await api.get(
        `/api/freeboard/search?searchType=${searchType}&keyword=${encodeURIComponent(
          searchKeyword
        )}&page=0&size=10`
      );

      setPosts(res.data.content);
      setTotalPages(res.data.totalPages);
      setActiveTab("posts");
    } catch (err) {
      alert("검색 실패: " + err.message);
    }
  };

  // 검색 초기화
  const resetSearch = () => {
    setIsSearching(false);
    setSearchKeyword("");
    setPage(0);
    fetchPosts();
  };

  // 수정 시작
  const startEdit = (post) => {
    setIsEditing(true);
    setEditingId(post.id);
    setFormData({
      fTitle: post.ftitle,
      fContent: post.fcontent,
    });
    setActiveTab("write");

    setTimeout(() => {
      if (contentEditableRef.current) {
        contentEditableRef.current.innerHTML = post.fcontent;
      }
    }, 0);
  };

  return (
    <div className="freeboard-container">
      <div className="freeboard-wrapper">
        {/* 탭 버튼 */}
        <div className="tab-buttons">
          <button
            onClick={() => {
              setActiveTab("posts");
              fetchPosts();
            }}
            className={`tab-btn ${activeTab === "posts" ? "active" : ""}`}
          >
            게시글 목록
          </button>
          <button
            onClick={() => {
              setActiveTab("write");
              setIsEditing(false);
              setFormData({ fTitle: "", fContent: "" });
              if (contentEditableRef.current) {
                contentEditableRef.current.innerHTML = "";
              }
            }}
            className={`tab-btn ${activeTab === "write" ? "active" : ""}`}
          >
            글쓰기
          </button>
        </div>

        {/* 게시글 목록 */}
        {activeTab === "posts" && (
          <div className="content-box">
            {/* 검색 영역 */}
            <div className="search-area">
              <select
                value={searchType}
                onChange={(e) => setSearchType(e.target.value)}
                className="search-select"
              >
                <option value="all">전체</option>
                <option value="title">제목</option>
                <option value="content">내용</option>
                <option value="author">작성자</option>
              </select>

              <input
                type="text"
                placeholder="검색어를 입력하세요"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === "Enter") handleSearch();
                }}
                className="search-input"
              />

              <button onClick={handleSearch} className="search-btn">
                검색
              </button>

              {isSearching && (
                <button onClick={resetSearch} className="reset-btn">
                  초기화
                </button>
              )}
            </div>

            {isSearching && (
              <div className="search-result-text">
                검색결과: "{searchKeyword}"
              </div>
            )}

            {/* 게시글 리스트 */}
            <div className="posts-list">
              {posts.length === 0 ? (
                <div className="empty-message">
                  {isSearching ? "검색 결과가 없습니다." : "게시글이 없습니다."}
                </div>
              ) : (
                posts.map((post) => (
                  <div
                    key={post.id}
                    className="post-item"
                    onClick={() => fetchPostDetail(post.id)}
                  >
                    <div className="post-header">
                      <h3 className="post-title">{post.ftitle}</h3>
                      {post.ffile && post.ffile !== "[]" && (
                        <span className="image-badge">📷</span>
                      )}
                    </div>
                    <div className="post-meta">
                      <span>작성자: {post.username || "Unknown"}</span>
                      <span>조회수: {post.fview}</span>
                      <span>좋아요: {post.flike}</span>
                      <span>
                        작성일:{" "}
                        {post.fwriteTime
                          ? new Date(post.fwriteTime).toLocaleDateString(
                              "ko-KR"
                            )
                          : "-"}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>

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
            <div className="write-form">
              <input
                type="text"
                placeholder="제목"
                value={formData.fTitle}
                onChange={(e) =>
                  setFormData({ ...formData, fTitle: e.target.value })
                }
                className="title-input"
              />

              <div className="image-insert-area">
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="image-insert-btn"
                >
                  📷 이미지 삽입
                </button>
                <span className="image-insert-note">
                  커서 위치에 이미지가 삽입됩니다 (최대 5MB)
                </span>
              </div>

              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept="image/*"
                onChange={handleImageInsert}
                style={{ display: "none" }}
              />

              <div
                ref={contentEditableRef}
                contentEditable
                onInput={handleContentChange}
                placeholder="내용을 입력하세요..."
                className="content-editor"
                suppressContentEditableWarning
              />

              <button onClick={handleSubmit} className="submit-btn">
                {isEditing ? "수정하기" : "작성하기"}
              </button>
            </div>
          </div>
        )}

        {/* 게시글 상세 */}
        {activeTab === "detail" && selectedPost && (
          <div className="content-box">
            <h2 className="detail-title">{selectedPost.ftitle}</h2>
            <div className="detail-meta">
              <span>작성자: {selectedPost.username}</span>
              <span>조회수: {selectedPost.fview}</span>
              <span>좋아요: {selectedPost.flike}</span>
              <span>
                작성일:{" "}
                {selectedPost.fwriteTime
                  ? new Date(selectedPost.fwriteTime).toLocaleString("ko-KR")
                  : "-"}
              </span>
            </div>

            <div
              className="detail-content"
              dangerouslySetInnerHTML={{ __html: selectedPost.fcontent }}
            />

            <div className="like-area">
              <button
                onClick={handleLikeToggle}
                className="like-btn"
                style={{
                  backgroundColor: isLiked ? "#ef4444" : "#f0f0f0",
                  color: isLiked ? "white" : "#666",
                }}
              >
                {isLiked ? "❤️ 좋아요 취소" : "🤍 좋아요"}
              </button>
            </div>

            {currentUsername && selectedPost.username === currentUsername && (
              <div className="action-buttons">
                <button
                  onClick={() => startEdit(selectedPost)}
                  className="edit-btn"
                >
                  수정
                </button>
                <button
                  onClick={() => handleDelete(selectedPost.id)}
                  className="delete-btn"
                >
                  삭제
                </button>
              </div>
            )}

            <div className="back-button-area">
              <button
                onClick={() => {
                  setActiveTab("posts");
                  fetchPosts();
                }}
                className="back-btn"
              >
                목록으로
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
