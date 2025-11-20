import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8880",
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    // ⭐ 항상 최신 토큰 가져오기
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log("🔑 요청 토큰:", token.substring(0, 20) + "...");
    } else {
      console.log("🔑 토큰 없음");
      delete config.headers.Authorization;
    }

    console.log("📤 요청:", config.method.toUpperCase(), config.url);

    return config;
  },
  (error) => {
    console.error("❌ 요청 인터셉터 에러:", error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    console.log("✅ 응답 성공:", response.config.url, response.status);
    return response;
  },
  (error) => {
    console.log("응답 실패:", error.config?.url, error.response?.status);

    if (error.response?.status === 401) {
      console.warn("401 에러 - 인증 실패");

      // GET 요청의 초기 체크용 API만 토큰 삭제하지 않음
      const isGetRequest = error.config?.method?.toUpperCase() === "GET";
      const isInitialCheckUrl =
        error.config?.url?.includes("/api/auth/me") ||
        error.config?.url?.includes("/api/cart");

      // 게시판 GET 요청도 예외 처리
      const isBoardRequest =
        error.config?.url?.includes("/api/freeboard") ||
        error.config?.url?.includes("/api/counselboard") ||
        error.config?.url?.includes("/api/infoboard");

      // 마이페이지 비밀번호 인증 요청도 예외 처리
      const isPasswordVerify = error.config?.url?.includes(
        "/api/mypage/verify-password"
      );

      // 게시판 또는 초기 체크 요청이면 리다이렉트 하지 않음
      if (
        (isGetRequest && isBoardRequest) ||
        isInitialCheckUrl ||
        isPasswordVerify
      ) {
        console.log("게시판 조회 또는 초기 체크 - 401 무시");
        return Promise.reject(error);
      }

      // 다른 401 에러는 토큰 삭제
      console.log("토큰 삭제 및 로그인 페이지로 이동");
      localStorage.removeItem("token");
      localStorage.removeItem("role");

      // 로그인 페이지가 아닐 때만 리다이렉트
      if (
        window.location.pathname !== "/login" &&
        window.location.pathname !== "/" &&
        !window.location.pathname.startsWith("/oauth2")
      ) {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default api;
