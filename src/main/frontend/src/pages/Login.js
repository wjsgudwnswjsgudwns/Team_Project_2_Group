import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./Login.css";

function Login({ onLogin, setRole }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  // 일반 로그인
  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    console.log("로그인 시도:", { username, password });

    try {
      // JSON 형식으로 전송
      const loginResponse = await api.post("/api/auth/login", {
        username,
        password,
      });

      console.log("로그인 응답:", loginResponse.data);

      // 토큰과 역할 가져오기
      const token = loginResponse.data.token;
      const userRole = loginResponse.data.role;

      // localStorage에 저장
      localStorage.setItem("token", token);
      localStorage.setItem("role", userRole);

      // 사용자 정보 가져오기 - 직접 헤더에 토큰 포함
      const userResponse = await api.get("/api/auth/me", {
        headers: {
          Authorization: `Bearer ${token}`, // localStorage가 아닌 변수 사용
        },
      });

      console.log("사용자 정보:", userResponse.data);

      onLogin(userResponse.data.nickname);
      setRole(userRole);

      alert("로그인 성공!");
      navigate("/", { replace: true });
    } catch (err) {
      console.error("로그인 에러:", err);
      console.error("에러 응답:", err.response?.data);
      console.error("에러 상태:", err.response?.status);

      if (err.response?.status === 401) {
        alert("잘못된 아이디 또는 비밀번호입니다.");
      } else if (err.response?.status === 403) {
        alert("접근이 거부되었습니다.");
      } else {
        alert("로그인 실패: " + (err.response?.data?.message || err.message));
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 네이버 로그인
  const handleNaverLogin = () => {
    window.location.href = `${process.env.REACT_APP_API_URL}/oauth2/authorization/naver`;
  };

  // 구글 로그인
  const handleGoogleLogin = () => {
    window.location.href = `${process.env.REACT_APP_API_URL}/oauth2/authorization/google`;
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-logo">OPTICORE</div>
        <form onSubmit={handleLogin} className="login-form">
          <input
            type="text"
            className="login-input"
            value={username}
            placeholder="아이디"
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={isLoading}
          />
          <input
            type="password"
            className="login-input"
            value={password}
            placeholder="비밀번호"
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isLoading}
          />
          <button
            type="submit"
            className="login-submit-btn"
            disabled={isLoading}
          >
            {isLoading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <div className="divider">
          <span>또는</span>
        </div>

        <button
          className="naver-login-btn"
          onClick={handleNaverLogin}
          type="button"
        >
          <svg viewBox="0 0 20 20" className="naver-icon">
            <g>
              <path
                fill="#fff"
                d="M13.48 10.77L8.57 3.25H4.8V16.8h5.16V9.28l4.91 7.52h3.77V3.25h-5.16v7.52z"
              />
            </g>
          </svg>
          네이버 로그인
        </button>

        <button
          className="google-login-btn"
          onClick={handleGoogleLogin}
          type="button"
          style={{
            marginTop: "10px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: "#fff",
            color: "#000",
            border: "1px solid #ddd",
            padding: "10px",
            borderRadius: "5px",
            width: "100%",
            cursor: "pointer",
          }}
        >
          <img
            src="https://developers.google.com/identity/images/g-logo.png"
            alt="Google Logo"
            style={{ width: "20px", height: "20px", marginRight: "8px" }}
          />
          Google 로그인
        </button>

        <div className="login-signup-link">
          <Link to="/signup">회원가입</Link>
          <span> / </span>
          <Link to="/find-username">아이디 찾기</Link>
          <span> / </span>
          <Link to="/find-password">비밀번호 찾기</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
