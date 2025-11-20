import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import HelpAnswer from "./HelpAnswer";
import "../help/Help.css";

function HelpList() {
  const navigate = useNavigate();
  const [helps, setHelps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("all");
  const [selectedHelp, setSelectedHelp] = useState(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const size = 10;

  useEffect(() => {
    fetchAllHelps();
  }, [page]);

  const fetchAllHelps = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("로그인이 필요합니다.");
        navigate("/login");
        return;
      }

      const response = await api.get("/api/help/admin/list", {
        headers: { Authorization: `Bearer ${token}` },
        params: { page, size },
      });

      if (response.data.content && Array.isArray(response.data.content)) {
        setHelps(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (error) {
      console.error("문의 목록 조회 실패:", error);
      setHelps([]);
      if (error.response?.status === 403) {
        alert("관리자 권한이 필요합니다.");
        navigate("/");
      } else if (error.response?.status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login");
      }
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
      window.scrollTo(0, 0);
    }
  };

  const handleAnswerToggle = async (id, currentStatus) => {
    try {
      const token = localStorage.getItem("token");
      await api.put(
        `/api/help/admin/${id}/answer?answered=${!currentStatus}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      alert("답변 상태가 변경되었습니다.");

      // 선택된 항목 업데이트
      if (selectedHelp && selectedHelp.id === id) {
        setSelectedHelp({ ...selectedHelp, answered: !currentStatus });
      }

      fetchAllHelps();
    } catch (error) {
      console.error("상태 변경 실패:", error);
      alert(error.response?.data || "상태 변경 중 오류가 발생했습니다.");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    try {
      const token = localStorage.getItem("token");
      await api.delete(`/api/help/admin/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      alert("삭제되었습니다.");
      setSelectedHelp(null);
      fetchAllHelps();
    } catch (error) {
      console.error("삭제 실패:", error);
      alert(error.response?.data || "삭제 중 오류가 발생했습니다.");
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const filteredHelps = helps.filter((help) => {
    if (filter === "answered") return help.answered;
    if (filter === "pending") return !help.answered;
    return true;
  });

  // 답변 업데이트 후 목록 새로고침
  const handleAnswerUpdate = () => {
    fetchAllHelps();
    // 현재 선택된 문의 정보도 업데이트
    if (selectedHelp) {
      fetchHelpDetail(selectedHelp.id);
    }
  };

  const fetchHelpDetail = async (id) => {
    try {
      const token = localStorage.getItem("token");
      const response = await api.get(`/api/help/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setSelectedHelp(response.data);
    } catch (error) {
      console.error("문의 상세 조회 실패:", error);
    }
  };

  if (loading) {
    return <div className="loading">로딩 중...</div>;
  }

  // 상세보기
  if (selectedHelp) {
    return (
      <div className="help-detail-container">
        <div className="help-detail-header">
          <button className="back-btn" onClick={() => setSelectedHelp(null)}>
            ← 목록으로
          </button>
        </div>

        <div className="help-detail-card">
          <div className="help-detail-title-section">
            <div>
              <h2>{selectedHelp.title}</h2>
              <span className="help-detail-date">
                {formatDate(selectedHelp.inquiryDate)}
              </span>
            </div>
            <span
              className={`status-badge ${
                selectedHelp.answered ? "answered" : "pending"
              }`}
            >
              {selectedHelp.answered ? "답변 완료" : "답변 대기"}
            </span>
          </div>

          <div className="help-detail-divider"></div>

          <div className="help-detail-content">
            <h3>문의 내용</h3>
            <p>{selectedHelp.content}</p>
          </div>

          <div className="help-detail-divider"></div>
          <div className="help-detail-info">
            <div className="info-row">
              <span className="info-label">작성자:</span>
              <span className="info-value">
                {selectedHelp.user
                  ? `${selectedHelp.user.nickname} (회원)`
                  : `${selectedHelp.name} (비회원)`}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">이메일:</span>
              <span className="info-value">{selectedHelp.email}</span>
            </div>
            {selectedHelp.phone && (
              <div className="info-row">
                <span className="info-label">전화번호:</span>
                <span className="info-value">{selectedHelp.phone}</span>
              </div>
            )}
          </div>

          <div className="help-detail-divider"></div>

          {/* 답변 컴포넌트 추가 */}
          <HelpAnswer
            helpId={selectedHelp.id}
            existingAnswer={selectedHelp.helpAnswer}
            onAnswerUpdate={handleAnswerUpdate}
          />

          <div className="help-detail-actions">
            <button
              className="delete-btn"
              onClick={() => handleDelete(selectedHelp.id)}
            >
              문의 삭제
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 목록
  return (
    <div className="help-list-container admin">
      <div className="help-list-header">
        <h2>전체 문의 관리 ({Array.isArray(helps) ? helps.length : 0}건)</h2>
        <div className="filter-buttons">
          <button
            className={filter === "all" ? "active" : ""}
            onClick={() => setFilter("all")}
          >
            전체
          </button>
          <button
            className={filter === "pending" ? "active" : ""}
            onClick={() => setFilter("pending")}
          >
            답변 대기
          </button>
          <button
            className={filter === "answered" ? "active" : ""}
            onClick={() => setFilter("answered")}
          >
            답변 완료
          </button>
        </div>
      </div>

      {!Array.isArray(helps) || filteredHelps.length === 0 ? (
        <div className="empty-list">
          <p>문의가 없습니다.</p>
        </div>
      ) : (
        <div className="help-list">
          {filteredHelps.map((help) => (
            <div
              key={help.id}
              className="help-item admin"
              onClick={() => setSelectedHelp(help)}
            >
              <div className="help-item-header">
                <div>
                  <h3 className="help-title-clickable">{help.title}</h3>
                  <span className="author-info">
                    {help.user
                      ? `회원: ${help.user.nickname}`
                      : `비회원: ${help.name}`}
                  </span>
                </div>
                <span
                  className={`status-badge ${
                    help.answered ? "answered" : "pending"
                  }`}
                >
                  {help.answered ? "답변 완료" : "답변 대기"}
                </span>
              </div>
              <div className="help-item-preview">
                <p>
                  {help.content.substring(0, 100)}
                  {help.content.length > 100 ? "..." : ""}
                </p>
              </div>
              <div className="help-item-footer">
                <span className="help-date">
                  {formatDate(help.inquiryDate)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 페이징 - 목록 마지막에 추가 */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="page-btn"
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0}
          >
            이전
          </button>

          <div className="page-numbers">
            {[...Array(totalPages)].map((_, index) => (
              <button
                key={index}
                className={`page-number ${page === index ? "active" : ""}`}
                onClick={() => handlePageChange(index)}
              >
                {index + 1}
              </button>
            ))}
          </div>

          <button
            className="page-btn"
            onClick={() => handlePageChange(page + 1)}
            disabled={page === totalPages - 1}
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}

export default HelpList;
