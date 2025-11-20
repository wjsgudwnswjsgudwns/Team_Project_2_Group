import { useEffect, useState } from "react";
import "./App.css";

import api from "./api/axiosConfig";
import { Route, Routes, useNavigate, useLocation } from "react-router-dom";
import Navbar from "./component/Navbar";
import Home from "./pages/Home";
import Signup from "./pages/Signup";
import Login from "./pages/Login";
import OAuth2RedirectHandler from "./pages/OAuth2RedirectHandler";
import Cpu from "./pages/Computer/Cpu";
import AiConsult from "./pages/AiConsult";
import Input from "./pages/Computer/Input";
import FreeBoard from "./pages/FreeBoard"; // 게시판 추가
import CpuView from "./pages/Computer/ViewDetail/CpuView";
import Cooler from "./pages/Computer/Cooler";
import CoolerView from "./pages/Computer/ViewDetail/CoolerView";
import MainBoard from "./pages/Computer/MainBoard";
import MainBoardView from "./pages/Computer/ViewDetail/MainBoardView";
import Memory from "./pages/Computer/Memory";
import MemoryView from "./pages/Computer/ViewDetail/MemoryView";
import Gpu from "./pages/Computer/Gpu";
import GpuView from "./pages/Computer/ViewDetail/GpuView";
import Disk from "./pages/Computer/Disk";
import DiskView from "./pages/Computer/ViewDetail/DiskView";
import HardCase from "./pages/Computer/HardCase";
import HardCaseView from "./pages/Computer/ViewDetail/HardCaseView";
import Power from "./pages/Computer/Power";
import PowerView from "./pages/Computer/ViewDetail/PowerView";
import { CartProvider } from "./pages/Computer/context/CartContext";
import CompatibilityResult from "./pages/Computer/cartcomponents/CompatibilityResult";
import Edit from "./pages/Computer/Edit";
import Chatbot from "./component/Chatbot";
import MyPage from "./pages/MyPage";
import UserProfile from "./pages/UserProfile";

import CounselBoard from "./pages/CounselBoard";
import InfoBoard from "./pages/InfoBoard";
import Home2 from "./pages/Home2";
import SetPassword from "./pages/SetPassword";
import Footer from "./component/Footer";
import Help from "./help/Help";
import MyHelp from "./help/MyHelp";
import HelpDetail from "./help/HelpDetail";
import GuestInquiry from "./help/GuestInquiry";
import HelpList from "./help/HelpList";
import FindUsername from "./FindAccount/FindUsername";
import FindPassword from "./FindAccount/FindPassword";

function App() {
  const [user, setUser] = useState(null); // 현재 로그인한 유저의 이름
  const [role, setRole] = useState(null); // 사용자 역할

  const navigate = useNavigate("");
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem("token");
    const savedRole = localStorage.getItem("role");

    if (token) {
      api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    }

    if (savedRole) {
      setRole(savedRole);
    }
  }, []);

  // 스크롤 복원 비활성화 (히스토리 관리 개선)
  useEffect(() => {
    if ("scrollRestoration" in window.history) {
      window.history.scrollRestoration = "manual";
    }
  }, []);

  // 라우트 변경 시 스크롤 최상단으로 (필요한 경우만)
  useEffect(() => {
    // freeboard detail에서 detail로 이동할 때는 스크롤 유지
    if (
      location.pathname === "/freeboard" &&
      location.search.includes("tab=detail")
    ) {
      return; // 스크롤 유지
    }

    window.scrollTo(0, 0);
  }, [location.pathname, location.search]);

  // 사용자 확인
  const checkUser = async () => {
    // ⭐ 토큰이 없으면 바로 리턴
    const token = localStorage.getItem("token");
    if (!token) {
      setUser(null);
      setRole(null);
      return;
    }

    try {
      const res = await api.get("/api/auth/me");
      console.log("/api/auth/me 응답:", res.data);
      setUser(res.data.nickname);

      const savedRole = localStorage.getItem("role"); // 역할 가져오기
      setRole(savedRole); // 역할 저장
    } catch (error) {
      console.error("사용자 정보 확인 실패:", error);
      // ⭐ /api/auth/me 실패 시에도 토큰은 유지 (axios 인터셉터에서 처리)
      setUser(null);
      setRole(null);
    }
  };

  useEffect(() => {
    checkUser();
  }, []);

  // 로그 아웃
  const handleLogout = async () => {
    localStorage.removeItem("token"); // 토큰 삭제
    localStorage.removeItem("role"); // 권한 삭제

    setUser(null);
    setRole(null);
    navigate("/");
  };

  return (
    <div className="App">
      <Navbar onLogout={handleLogout} user={user} role={role} />
      <CartProvider>
        <Routes>
          <Route path="/" element={<Home user={user} role={role} />}></Route>
          <Route
            path="/home2"
            element={<Home2 user={user} role={role} />}
          ></Route>
          <Route path="/signup" element={<Signup />}></Route>
          <Route
            path="/login"
            element={<Login onLogin={setUser} setRole={setRole} />}
          ></Route>
          <Route
            path="/oauth2/redirect"
            element={
              <OAuth2RedirectHandler onLogin={setUser} setRole={setRole} />
            }
          />
          <Route path="/help" element={<Help />}></Route>
          <Route path="/myhelp" element={<MyHelp />}></Route>
          <Route path="/help/:id" element={<HelpDetail />}></Route>
          <Route path="/guest-inquiry" element={<GuestInquiry />}></Route>
          <Route path="/helplist" element={<HelpList />}></Route>

          <Route path="/find-username" element={<FindUsername />} />
          <Route path="/find-password" element={<FindPassword />} />

          <Route path="/set-password" element={<SetPassword />}></Route>

          <Route path="/input" element={<Input />}></Route>
          <Route path="/edit/:id" element={<Edit />}></Route>

          <Route path="/cpu" element={<Cpu role={role} user={user} />}></Route>
          <Route path="/cpu/:id" element={<CpuView role={role} />}></Route>

          <Route path="/cooler" element={<Cooler role={role} />}></Route>
          <Route
            path="/cooler/:id"
            element={<CoolerView role={role} />}
          ></Route>

          <Route path="/mainboard" element={<MainBoard role={role} />}></Route>
          <Route
            path="/mainboard/:id"
            element={<MainBoardView role={role} />}
          ></Route>

          <Route path="/memory" element={<Memory role={role} />}></Route>
          <Route
            path="/memory/:id"
            element={<MemoryView role={role} />}
          ></Route>

          <Route path="/gpu" element={<Gpu role={role} />}></Route>
          <Route path="/gpu/:id" element={<GpuView role={role} />}></Route>

          <Route path="/disk" element={<Disk role={role} />}></Route>
          <Route path="/disk/:id" element={<DiskView role={role} />}></Route>

          <Route path="/case" element={<HardCase role={role} />}></Route>
          <Route
            path="/case/:id"
            element={<HardCaseView role={role} />}
          ></Route>

          <Route path="/power" element={<Power role={role} />}></Route>
          <Route path="/power/:id" element={<PowerView role={role} />}></Route>

          <Route path="/freeboard" element={<FreeBoard />}></Route>

          <Route path="/counselboard" element={<CounselBoard />}></Route>
          <Route path="/infoboard" element={<InfoBoard />}></Route>
          <Route path="/mypage" element={<MyPage />}></Route>
          <Route path="/user/:username" element={<UserProfile />}></Route>

          <Route path="/ai" element={<AiConsult />}></Route>
          <Route
            path="compatibility-result"
            element={<CompatibilityResult />}
          ></Route>
        </Routes>
        <Chatbot />
      </CartProvider>
      <Footer />
    </div>
  );
}

export default App;
