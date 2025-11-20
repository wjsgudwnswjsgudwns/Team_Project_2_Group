import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./FindAccount.css";

function FindUsername() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1: 이메일 입력, 2: 인증 코드 입력, 3: 아이디 표시
  const [email, setEmail] = useState("");
  const [verificationCode, setVerificationCode] = useState("");
  const [foundUsername, setFoundUsername] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // 1단계: 인증 코드 전송
  const handleSendCode = async (e) => {
    e.preventDefault();
    setError("");

    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post("/api/account/find-username/send-code", {
        email,
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

    if (!verificationCode.trim()) {
      setError("인증 코드를 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post(
        "/api/account/find-username/verify-code",
        {
          email,
          verificationCode,
        }
      );

      setFoundUsername(response.data.username);
      setStep(3);
    } catch (error) {
      setError(error.response?.data?.error || "인증 코드가 올바르지 않습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="find-account-container">
      <div className="find-account-card">
        <h2>아이디 찾기</h2>

        {step === 1 && (
          <form onSubmit={handleSendCode}>
            <div className="form-group">
              <label>이메일</label>
              <input
                type="email"
                className="form-input"
                placeholder="가입 시 등록한 이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
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
              <p>{email}로 인증 코드를 전송했습니다.</p>
              <p>이메일을 확인해주세요.</p>
            </div>

            <div className="form-group">
              <label>인증 코드</label>
              <input
                type="text"
                className="form-input"
                placeholder="6자리 인증 코드"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
                maxLength={6}
                disabled={loading}
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <div className="account-button-area">
              <button
                type="submit"
                className="account-primary-btn"
                disabled={loading}
              >
                {loading ? "확인 중..." : "인증 확인"}
              </button>
              <button
                type="button"
                className="account-secondary-btn"
                onClick={() => setStep(1)}
              >
                다시 입력
              </button>
            </div>
          </form>
        )}

        {step === 3 && (
          <div className="result-container">
            <div className="success-icon">✓</div>
            <h3>아이디 찾기 완료</h3>
            <div className="result-box">
              <p className="result-label">회원님의 아이디는</p>
              <p className="result-value">{foundUsername}</p>
              <p className="result-label">입니다.</p>
            </div>

            <div className="account-button-area">
              <button
                className="account-primary-btn"
                onClick={() => navigate("/login")}
              >
                로그인하기
              </button>
              <button
                className="account-secondary-btn"
                onClick={() => navigate("/find-password")}
              >
                비밀번호 찾기
              </button>
            </div>
          </div>
        )}

        <div className="find-link-group">
          <button onClick={() => navigate("/login")}>로그인</button>
          <span>|</span>
          <button onClick={() => navigate("/find-password")}>
            비밀번호 찾기
          </button>
        </div>
      </div>
    </div>
  );
}

export default FindUsername;
