import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "../help/Help.css";

function GuestInquiry() {
  const navigate = useNavigate();

  const [searchData, setSearchData] = useState({
    name: "",
    phone: "",
  });

  const [helps, setHelps] = useState([]);
  const [searched, setSearched] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const size = 10;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setSearchData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSearch = async (e, pageNum = 0) => {
    if (e && e.preventDefault) {
      e.preventDefault();
    }

    if (!searchData.name.trim() || !searchData.phone.trim()) {
      alert("이름과 휴대폰 번호를 모두 입력해주세요.");
      return;
    }

    try {
      const response = await api.post("/api/help/guest/inquiry", searchData, {
        params: { page: pageNum, size },
      });
      setHelps(response.data.content);
      setTotalPages(response.data.totalPages);
      setPage(pageNum);
      setSearched(true);
    } catch (error) {
      console.error("조회 실패:", error);
      if (error.response?.status === 404) {
        alert("해당 정보로 등록된 문의가 없습니다.");
        setHelps([]);
        setTotalPages(0);
      } else {
        alert(error.response?.data || "조회 중 오류가 발생했습니다.");
      }
      setSearched(true);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      handleSearch(null, newPage);
      window.scrollTo(0, 0);
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

  return (
    <div className="guest-inquiry-container">
      <div className="guest-inquiry-form-section">
        <div className="help-form-title">비회원 문의 조회</div>

        <form className="help-form" onSubmit={handleSearch}>
          <div className="form-group">
            <label className="form-label">이름</label>
            <input
              type="text"
              name="name"
              className="form-input"
              placeholder="문의 시 입력한 이름"
              value={searchData.name}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label">휴대폰 번호</label>
            <input
              type="text"
              name="phone"
              className="form-input"
              placeholder="문의 시 입력한 번호 (ex: 01012345678)"
              value={searchData.phone}
              onChange={handleChange}
            />
          </div>

          <button type="submit" className="submit-btn">
            내 문의 조회하기
          </button>
        </form>
      </div>

      {searched && (
        <div className="help-list-section">
          {helps.length === 0 ? (
            <div className="empty-list">
              <p>등록된 문의가 없습니다.</p>
            </div>
          ) : (
            <>
              <div className="help-list">
                <h3>문의 내역 ({helps.length}건)</h3>
                {helps.map((help) => (
                  <div key={help.id} className="help-item">
                    <div className="help-item-header">
                      <h3
                        className="help-title-clickable"
                        onClick={() => navigate(`/help/${help.id}`)}
                      >
                        {help.title}
                      </h3>
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

              {/* 페이징 */}
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
                        className={`page-number ${
                          page === index ? "active" : ""
                        }`}
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
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default GuestInquiry;
