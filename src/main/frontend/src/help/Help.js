import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "../help/Help.css";

function Help() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token"); // 토큰

  // 초기값
  const [formData, setFormData] = useState({
    title: "",
    content: "",
    name: "",
    email: "",
    phone: "",
  });

  const [errors, setErrors] = useState({}); // 에러

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // 에러 메시지 클리어
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  // 유효성 검사
  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = "제목을 입력해주세요.";
    }
    if (!formData.content.trim()) {
      newErrors.content = "문의 내용을 입력해주세요.";
    }
    if (!formData.name.trim()) {
      newErrors.name = "이름을 입력해주세요.";
    }
    if (!formData.email.trim()) {
      newErrors.email = "이메일을 입력해주세요.";
    }
    if (!formData.phone.trim()) {
      newErrors.phone = "휴대폰 번호를 입력해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0; // 에러 갯수 확인
  };

  // 문의 제출
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      // 회원, 비회원 구분
      const config = token
        ? { headers: { Authorization: `Bearer ${token}` } }
        : {};

      const response = await api.post("/api/help/submit", formData, config);

      // 회원이면 내 문의로, 비회원이면 홈으로
      if (token) {
        navigate("/myhelp");
      } else {
        navigate("/");
      }
    } catch (error) {
      console.error("문의 등록 실패:", error);
      alert(error.response?.data || "문의 등록 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="help-container">
      {/* 1:1 문의 폼 */}
      <div className="help-form-section">
        <div className="help-form-title">1:1 문의하기</div>

        <form className="help-form" onSubmit={handleSubmit}>
          {/* 이름 */}
          <div className="form-group">
            <label className="form-label">이름</label>
            <input
              type="text"
              name="name"
              className="form-input"
              placeholder="이름을 입력해주세요."
              value={formData.name}
              onChange={handleChange}
            />
            {errors.name && <span className="error-text">{errors.name}</span>}
          </div>

          {/* 이메일 */}
          <div className="form-group">
            <label className="form-label">이메일</label>
            <input
              type="email"
              name="email"
              className="form-input"
              placeholder="이메일을 입력해주세요."
              value={formData.email}
              onChange={handleChange}
            />
            {errors.email && <span className="error-text">{errors.email}</span>}
          </div>

          {/* 핸드폰 */}
          <div className="form-group">
            <label className="form-label">휴대폰 번호</label>
            <input
              type="text"
              name="phone"
              className="form-input"
              placeholder="ex) 01055558888"
              value={formData.phone}
              onChange={handleChange}
            />
            {errors.phone && <span className="error-text">{errors.phone}</span>}
          </div>

          {/* 제목 */}
          <div className="form-group">
            <label className="form-label">문의 제목</label>
            <input
              type="text"
              name="title"
              className="form-input"
              placeholder="제목을 입력해주세요."
              value={formData.title}
              onChange={handleChange}
            />
            {errors.title && <span className="error-text">{errors.title}</span>}
          </div>

          {/* 내용 */}
          <div className="form-group">
            <label className="form-label">문의 내용</label>
            <textarea
              name="content"
              className="form-textarea"
              placeholder="문의사항을 자세히 적어주세요."
              value={formData.content}
              onChange={handleChange}
            />
            {errors.content && (
              <span className="error-text">{errors.content}</span>
            )}
          </div>

          <button type="submit" className="submit-btn">
            문의 제출하기
          </button>
        </form>
      </div>

      {/* 안내사항 */}
      <div className="help-info-section">
        <div className="help-info-title">안내 사항</div>

        <ul className="help-info-list">
          <li>업무시간 내 순차적으로 답변드리니 조금만 기다려주세요.</li>
        </ul>

        <div className="help-info-divider"></div>

        <div className="help-info-hours">
          고객센터 운영시간
          <br />
          오전 9시 ~ 오후 6시, 연중무휴
        </div>

        {/* 비회원 문의 조회 버튼 */}
        {!token && (
          <button
            className="guest-inquiry-btn"
            onClick={() => navigate("/guest-inquiry")}
          >
            비회원 문의 조회
          </button>
        )}
      </div>
    </div>
  );
}

export default Help;
