import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/axiosConfig";

function OAuth2RedirectHandler({ onLogin, setRole }) {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    // SecurityConfig에서 리다이렉트할 때 token을 쿼리 파라미터로 전달
    const token = searchParams.get("token");

    if (!token) {
      console.error("토큰이 없습니다.");
      alert("로그인에 실패했습니다.");
      navigate("/login");
      return;
    }

    const fetchUserInfo = async () => {
      try {
        // 토큰을 localStorage에 저장
        localStorage.setItem("token", token);

        // axios 기본 헤더에 토큰 설정
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        // 사용자 정보 가져오기
        const userResponse = await api.get("/api/auth/me");
        const { nickname, username, role, hasPassword } = userResponse.data;

        // role 저장
        if (role) {
          localStorage.setItem("role", role);
          setRole(role);
        }

        // 로그인 처리
        onLogin(nickname || username);

        if (hasPassword === "false" || hasPassword === false) {
          alert("추가 정보 입력이 필요합니다.");
          navigate("/set-password", { replace: true });
        } else {
          alert("로그인 성공!");
          navigate("/", { replace: true });
        }
      } catch (err) {
        console.error("사용자 정보 조회 실패:", err);
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        alert("로그인 처리 중 오류가 발생했습니다.");
        navigate("/login");
      }
    };

    fetchUserInfo();
  }, [searchParams, navigate, onLogin, setRole]);

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
        backgroundColor: "#f5f5f5",
      }}
    >
      <div style={{ textAlign: "center" }}>
        <div
          style={{
            width: "50px",
            height: "50px",
            border: "5px solid #f3f3f3",
            borderTop: "5px solid #3498db",
            borderRadius: "50%",
            animation: "spin 1s linear infinite",
            margin: "0 auto 20px",
          }}
        ></div>
        <h2>로그인 처리 중...</h2>
        <p>잠시만 기다려주세요.</p>
        <style>
          {`
            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }
          `}
        </style>
      </div>
    </div>
  );
}

export default OAuth2RedirectHandler;
