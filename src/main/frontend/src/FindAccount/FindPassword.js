import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./FindAccount.css";

function FindPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1: 아이디/이메일, 2: 인증 코드, 3: 비밀번호 재설정, 4: 완료
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    verificationCode: "",
    newPassword: "",
    newPasswordCheck: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  // 1단계: 인증 코드 전송
  const handleSendCode = async (e) => {
    e.preventDefault();
    setError("");

    if (!formData.username.trim() || !formData.email.trim()) {
      setError("아이디와 이메일을 모두 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post("/api/account/find-password/send-code", {
        username: formData.username,
        email: formData.email,
      });

      alert(response.data.message);
      setStep(2);
    } catch (error) {
      setError(error.response?.data?.error || "오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 2단계: 인증 코드 확인
  const handleVerifyCode = async (e) => {
    e.preventDefault();
    setError("");

    if (!formData.verificationCode.trim()) {
      setError("인증 코드를 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post(
        "/api/account/find-password/verify-code",
        {
          username: formData.username,
          email: formData.email,
          verificationCode: formData.verificationCode,
        }
      );

      alert(response.data.message);
      setStep(3);
    } catch (error) {
      setError(error.response?.data?.error || "인증 코드가 올바르지 않습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 3단계: 비밀번호 재설정
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError("");

    if (!formData.newPassword || !formData.newPasswordCheck) {
      setError("비밀번호를 모두 입력해주세요.");
      return;
    }

    if (formData.newPassword !== formData.newPasswordCheck) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    if (formData.newPassword.length < 8) {
      setError("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post(
        "/api/account/find-password/reset-password",
        {
          username: formData.username,
          email: formData.email,
          newPassword: formData.newPassword,
          newPasswordCheck: formData.newPasswordCheck,
        }
      );

      alert(response.data.message);
      setStep(4);
    } catch (error) {
      setError(
        error.response?.data?.error || "비밀번호 재설정 중 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="find-account-container">
      <div className="find-account-card">
        <h2>비밀번호 찾기</h2>

        {step === 1 && (
          <form onSubmit={handleSendCode}>
            <div className="form-group">
              <label>아이디</label>
              <input
                type="text"
                name="username"
                className="form-input"
                placeholder="아이디를 입력하세요"
                value={formData.username}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label>이메일</label>
              <input
                type="email"
                name="email"
                className="form-input"
                placeholder="가입 시 등록한 이메일"
                value={formData.email}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <button type="submit" className="submit-btn" disabled={loading}>
              {loading ? "전송 중..." : "인증 코드 전송"}
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleVerifyCode}>
            <div className="info-box">
              <p>{formData.email}로 인증 코드를 전송했습니다.</p>
              <p>이메일을 확인해주세요.</p>
            </div>

            <div className="form-group">
              <label>인증 코드</label>
              <input
                type="text"
                name="verificationCode"
                className="form-input"
                placeholder="6자리 인증 코드"
                value={formData.verificationCode}
                onChange={handleChange}
                maxLength={6}
                disabled={loading}
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <div className="find-button-group">
              <button type="submit" className="submit-btn" disabled={loading}>
                {loading ? "확인 중..." : "인증 확인"}
              </button>
              <button
                type="button"
                className="secondary-btn"
                onClick={() => setStep(1)}
              >
                다시 입력
              </button>
            </div>
          </form>
        )}

        {step === 3 && (
          <form onSubmit={handleResetPassword}>
            <div className="info-box">
              <p>새로운 비밀번호를 설정해주세요.</p>
            </div>

            <div className="form-group">
              <label>새 비밀번호</label>
              <input
                type="password"
                name="newPassword"
                className="form-input"
                placeholder="영문, 숫자, 특수문자 포함 8자 이상"
                value={formData.newPassword}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label>비밀번호 확인</label>
              <input
                type="password"
                name="newPasswordCheck"
                className="form-input"
                placeholder="비밀번호를 다시 입력하세요"
                value={formData.newPasswordCheck}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <button type="submit" className="submit-btn" disabled={loading}>
              {loading ? "변경 중..." : "비밀번호 변경"}
            </button>
          </form>
        )}

        {step === 4 && (
          <div className="result-container">
            <div className="success-icon">✓</div>
            <h3>비밀번호 변경 완료</h3>
            <p className="result-message">
              비밀번호가 성공적으로 변경되었습니다.
            </p>

            <button className="submit-btn" onClick={() => navigate("/login")}>
              로그인하기
            </button>
          </div>
        )}

        <div className="find-link-group">
          <button onClick={() => navigate("/login")}>로그인</button>
          <span>|</span>
          <button onClick={() => navigate("/find-username")}>
            아이디 찾기
          </button>
        </div>
      </div>
    </div>
  );
}

export default FindPassword;
