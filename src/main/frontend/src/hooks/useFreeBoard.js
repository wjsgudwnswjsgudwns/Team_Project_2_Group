import { useState, useCallback } from "react";
import api from "../api/axiosConfig";

export const useFreeBoard = () => {
  const [posts, setPosts] = useState([]);
  const [selectedPost, setSelectedPost] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLiked, setIsLiked] = useState(false);

  // 게시글 목록 조회
  const fetchPosts = useCallback(
    async (searchParams = null) => {
      try {
        let url;
        if (searchParams && searchParams.keyword) {
          url = `/api/freeboard/search?searchType=${
            searchParams.searchType
          }&keyword=${encodeURIComponent(
            searchParams.keyword
          )}&page=${page}&size=10`;
        } else {
          url = `/api/freeboard?page=${page}&size=10`;
        }

        const res = await api.get(url);
        setPosts(res.data.content);
        setTotalPages(res.data.totalPages);
      } catch (err) {
        alert("게시글 조회 실패: " + err.message);
      }
    },
    [page]
  );

  // 검색 결과를 posts에 직접 설정하는 함수 추가
  const setSearchResults = useCallback((searchData) => {
    setPosts(searchData.content);
    setTotalPages(searchData.totalPages);
  }, []);

  // 게시글 상세 조회
  const fetchPostDetail = async (id) => {
    try {
      const res = await api.get(`/api/freeboard/${id}`);
      setSelectedPost(res.data);

      // ⭐ 로그인 상태일 때만 좋아요 여부 확인
      const token = localStorage.getItem("token");
      if (token) {
        try {
          const likeRes = await api.get(`/api/freeboard/${id}/like/status`);
          setIsLiked(likeRes.data.isLiked);
        } catch (likeError) {
          console.log("좋아요 상태 확인 실패 (비로그인 상태)");
          setIsLiked(false);
        }
      } else {
        // 비로그인 상태
        setIsLiked(false);
      }
    } catch (error) {
      console.error("게시글 상세 조회 실패:", error);
      alert("게시글을 불러올 수 없습니다.");
    }
  };

  // 게시글 작성
  const createPost = useCallback(async (formData) => {
    try {
      const submitData = {
        fTitle: formData.fTitle,
        fContent: formData.fContent,
        fFile: "",
      };

      await api.post("/api/freeboard", submitData);
      alert("작성 완료!");
      return true;
    } catch (err) {
      alert("작성 실패: " + err.message);
      return false;
    }
  }, []);

  // 게시글 수정
  const updatePost = useCallback(async (id, formData) => {
    try {
      const submitData = {
        fTitle: formData.fTitle,
        fContent: formData.fContent,
        fFile: "",
      };

      await api.put(`/api/freeboard/${id}`, submitData);
      alert("수정 완료!");
      return true;
    } catch (err) {
      alert("수정 실패: " + err.message);
      return false;
    }
  }, []);

  // 게시글 삭제
  const deletePost = useCallback(async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return false;

    try {
      await api.delete(`/api/freeboard/${id}`);
      alert("삭제 완료!");
      return true;
    } catch (err) {
      alert("삭제 실패: " + err.message);
      return false;
    }
  }, []);

  // 좋아요 토글
  const toggleLike = useCallback(
    async (postId) => {
      // 로그인 체크
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        return;
      }

      try {
        const res = await api.post(`/api/freeboard/${postId}/like`);
        setIsLiked(res.data.isLiked);
        await fetchPostDetail(postId);
        alert(res.data.message);
      } catch (err) {
        if (err.response?.status === 401) {
          alert("로그인이 필요합니다.");
        } else {
          alert("좋아요 처리 실패: " + err.message);
        }
      }
    },
    [fetchPostDetail]
  );

  return {
    // State
    posts,
    selectedPost,
    page,
    totalPages,
    isLiked,
    // Actions
    setPage,
    setSelectedPost,
    setSearchResults,
    fetchPosts,
    fetchPostDetail,
    createPost,
    updatePost,
    deletePost,
    toggleLike,
  };
};
