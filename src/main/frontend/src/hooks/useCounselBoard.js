import { useState, useCallback } from "react";
import api from "../api/axiosConfig";

export const useCounselBoard = () => {
  const [posts, setPosts] = useState([]);
  const [selectedPost, setSelectedPost] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLiked, setIsLiked] = useState(false);

  const fetchPosts = useCallback(
    async (searchParams = null) => {
      try {
        let url;
        if (searchParams && searchParams.keyword) {
          url = `/api/counselboard/search?searchType=${
            searchParams.searchType
          }&keyword=${encodeURIComponent(
            searchParams.keyword
          )}&page=${page}&size=10`;
        } else {
          url = `/api/counselboard?page=${page}&size=10`;
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

  const setSearchResults = useCallback((searchData) => {
    setPosts(searchData.content);
    setTotalPages(searchData.totalPages);
  }, []);

  const fetchPostDetail = useCallback(async (id) => {
    try {
      const res = await api.get(`/api/counselboard/${id}`);
      setSelectedPost(res.data);

      const likeRes = await api.get(`/api/counselboard/${id}/like/status`);
      setIsLiked(likeRes.data.isLiked);

      return res.data;
    } catch (err) {
      alert("게시글 상세 조회 실패: " + err.message);
      throw err;
    }
  }, []);

  const createPost = useCallback(async (formData) => {
    try {
      const submitData = {
        cTitle: formData.cTitle,
        cContent: formData.cContent,
        cFile: "",
      };

      await api.post("/api/counselboard", submitData);
      alert("작성 완료!");
      return true;
    } catch (err) {
      alert("작성 실패: " + err.message);
      return false;
    }
  }, []);

  const updatePost = useCallback(async (id, formData) => {
    try {
      const submitData = {
        cTitle: formData.cTitle,
        cContent: formData.cContent,
        cFile: "",
      };

      await api.put(`/api/counselboard/${id}`, submitData);
      alert("수정 완료!");
      return true;
    } catch (err) {
      alert("수정 실패: " + err.message);
      return false;
    }
  }, []);

  const deletePost = useCallback(async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return false;

    try {
      await api.delete(`/api/counselboard/${id}`);
      alert("삭제 완료!");
      return true;
    } catch (err) {
      alert("삭제 실패: " + err.message);
      return false;
    }
  }, []);

  const toggleLike = useCallback(
    async (postId) => {
      // ⭐ 로그인 체크
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인 후 이용 가능합니다.");
        return;
      }

      try {
        const res = await api.post(`/api/counselboard/${postId}/like`);
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
    posts,
    selectedPost,
    page,
    totalPages,
    isLiked,
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
