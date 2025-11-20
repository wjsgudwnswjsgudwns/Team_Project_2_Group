import { useState, useCallback } from "react";
import api from "../api/axiosConfig";

export const useCounselComment = () => {
  const [comments, setComments] = useState([]);
  const [commentCount, setCommentCount] = useState(0);
  const [commentPage, setCommentPage] = useState(0);
  const [commentTotalPages, setCommentTotalPages] = useState(0);

  const fetchComments = useCallback(async (boardId, page = 0, size = 10) => {
    try {
      const res = await api.get(
        `/api/counselboard/${boardId}/comments?page=${page}&size=${size}`
      );
      setComments(res.data.content);
      setCommentPage(res.data.number);
      setCommentTotalPages(res.data.totalPages);
    } catch (err) {
      console.error("댓글 조회 실패:", err);
      alert("댓글을 불러오는데 실패했습니다.");
    }
  }, []);

  const fetchCommentCount = useCallback(async (boardId) => {
    try {
      const res = await api.get(`/api/counselboard/${boardId}/comments/count`);
      setCommentCount(res.data.count);
    } catch (err) {
      console.error("댓글 개수 조회 실패:", err);
    }
  }, []);

  const createComment = useCallback(
    async (boardId, cCommentContent, parentId = null) => {
      try {
        await api.post(`/api/counselboard/${boardId}/comments`, {
          cCommentContent,
          parentId,
        });
        await fetchComments(boardId, commentPage);
        await fetchCommentCount(boardId);
        return true;
      } catch (err) {
        console.error("댓글 작성 실패:", err);
        alert("댓글 작성에 실패했습니다.");
        return false;
      }
    },
    [fetchComments, fetchCommentCount, commentPage]
  );

  const updateComment = useCallback(
    async (boardId, commentId, cCommentContent) => {
      try {
        await api.put(`/api/counselboard/${boardId}/comments/${commentId}`, {
          cCommentContent,
        });
        await fetchComments(boardId, commentPage);
        return true;
      } catch (err) {
        console.error("댓글 수정 실패:", err);
        alert("댓글 수정에 실패했습니다.");
        return false;
      }
    },
    [fetchComments, commentPage]
  );

  const deleteComment = useCallback(
    async (boardId, commentId) => {
      if (!window.confirm("댓글을 삭제하시겠습니까?")) return false;

      try {
        await api.delete(`/api/counselboard/${boardId}/comments/${commentId}`);
        await fetchComments(boardId, commentPage);
        await fetchCommentCount(boardId);
        alert("댓글이 삭제되었습니다.");
        return true;
      } catch (err) {
        console.error("댓글 삭제 실패:", err);
        alert("댓글 삭제에 실패했습니다.");
        return false;
      }
    },
    [fetchComments, fetchCommentCount, commentPage]
  );

  return {
    comments,
    commentCount,
    commentPage,
    commentTotalPages,
    setCommentPage,
    fetchComments,
    fetchCommentCount,
    createComment,
    updateComment,
    deleteComment,
  };
};
