import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./MyPage.css";

export default function MyPage() {
  const navigate = useNavigate();

  // 인증 상태
  const [isVerified, setIsVerified] = useState(false);
  const [verifyPassword, setVerifyPassword] = useState("");
  const [isVerifying, setIsVerifying] = useState(false);

  // 탭 상태
  const [activeTab, setActiveTab] = useState("info");

  // 마이페이지 데이터
  const [myPageData, setMyPageData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // 회원정보 수정
  const [updateForm, setUpdateForm] = useState({
    currentPassword: "",
    newPassword: "",
    newPasswordCheck: "",
    nickname: "",
    email: "",
  });

  // ✅ 이메일 인증 관련 상태 추가
  const [emailVerification, setEmailVerification] = useState({
    isChanging: false, // 이메일 변경 중인지
    newEmail: "", // 새로운 이메일
    verificationCode: "", // 인증 코드
    isCodeSent: false, // 인증 코드 발송 여부
    isSending: false, // 발송 중
    isVerifying: false, // 인증 중
    isVerified: false, // 인증 완료 여부
    timer: 300, // 5분 타이머
    timerInterval: null, // 타이머 인터벌
  });

  // 회원탈퇴
  const [deletePassword, setDeletePassword] = useState("");

  // 로그인 체크
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      alert("로그인이 필요합니다.");
      navigate("/login");
    }
  }, [navigate]);

  // ✅ 이메일 인증 타이머
  useEffect(() => {
    if (emailVerification.isCodeSent && emailVerification.timer > 0) {
      const interval = setInterval(() => {
        setEmailVerification((prev) => ({
          ...prev,
          timer: prev.timer - 1,
        }));
      }, 1000);

      return () => clearInterval(interval);
    } else if (emailVerification.timer === 0 && emailVerification.isCodeSent) {
      setEmailVerification((prev) => ({
        ...prev,
        isCodeSent: false,
        verificationCode: "",
      }));
      alert("인증 시간이 만료되었습니다. 다시 시도해주세요.");
    }
  }, [emailVerification.isCodeSent, emailVerification.timer]);

  // 비밀번호 인증
  const handleVerifyPassword = async (e) => {
    e.preventDefault();
    setIsVerifying(true);

    try {
      const res = await api.post("/api/mypage/verify-password", {
        password: verifyPassword,
      });

      if (res.data.success) {
        setIsVerified(true);
        fetchMyPageData();
      } else {
        alert(res.data.message);
      }
    } catch (err) {
      if (err.response?.status === 401) {
        alert("비밀번호가 일치하지 않습니다.");
      } else {
        alert("인증 실패: " + (err.response?.data?.message || err.message));
      }
    } finally {
      setIsVerifying(false);
    }
  };

  // 마이페이지 데이터 조회
  const fetchMyPageData = async () => {
    setIsLoading(true);
    try {
      const res = await api.get("/api/mypage/info");
      setMyPageData(res.data);

      // 수정 폼 초기값 설정
      setUpdateForm({
        currentPassword: "",
        newPassword: "",
        newPasswordCheck: "",
        nickname: res.data.nickname,
        email: res.data.email,
      });
    } catch (err) {
      alert("데이터 조회 실패: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  // ✅ 이메일 변경 시작
  const handleStartEmailChange = () => {
    setEmailVerification({
      ...emailVerification,
      isChanging: true,
      newEmail: updateForm.email,
      verificationCode: "",
      isCodeSent: false,
      isVerified: false,
      timer: 300,
    });
  };

  // ✅ 이메일 변경 취소
  const handleCancelEmailChange = () => {
    setEmailVerification({
      isChanging: false,
      newEmail: "",
      verificationCode: "",
      isCodeSent: false,
      isSending: false,
      isVerifying: false,
      isVerified: false,
      timer: 300,
      timerInterval: null,
    });
    setUpdateForm((prev) => ({ ...prev, email: myPageData.email }));
  };

  // ✅ 인증 코드 발송
  const handleSendVerificationCode = async () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailVerification.newEmail)) {
      alert("올바른 이메일 형식이 아닙니다.");
      return;
    }

    if (emailVerification.newEmail === myPageData.email) {
      alert("현재 이메일과 동일합니다.");
      return;
    }

    setEmailVerification((prev) => ({ ...prev, isSending: true }));

    try {
      const res = await api.post("/api/email/send-code", {
        email: emailVerification.newEmail,
        purpose: "CHANGE_EMAIL",
      });

      if (res.data.success) {
        alert("인증 코드가 발송되었습니다. 이메일을 확인해주세요.");
        setEmailVerification((prev) => ({
          ...prev,
          isCodeSent: true,
          timer: 300, // 5분 리셋
        }));
      }
    } catch (err) {
      alert(
        "인증 코드 발송 실패: " + (err.response?.data?.message || err.message)
      );
    } finally {
      setEmailVerification((prev) => ({ ...prev, isSending: false }));
    }
  };

  // ✅ 인증 코드 확인
  const handleVerifyCode = async () => {
    if (!emailVerification.verificationCode) {
      alert("인증 코드를 입력해주세요.");
      return;
    }

    setEmailVerification((prev) => ({ ...prev, isVerifying: true }));

    try {
      const res = await api.post("/api/email/verify-code", {
        email: emailVerification.newEmail,
        code: emailVerification.verificationCode,
        purpose: "CHANGE_EMAIL",
      });

      if (res.data.success) {
        alert("이메일 인증이 완료되었습니다.");
        setEmailVerification((prev) => ({
          ...prev,
          isVerified: true,
          isCodeSent: false,
        }));
      }
    } catch (err) {
      alert(
        "인증 실패: " +
          (err.response?.data?.message || "인증 코드가 올바르지 않습니다.")
      );
    } finally {
      setEmailVerification((prev) => ({ ...prev, isVerifying: false }));
    }
  };

  // ✅ 타이머 포맷팅 (분:초)
  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  // 회원정보 수정
  const handleUpdateInfo = async (e) => {
    e.preventDefault();

    // OAuth 사용자가 아닌 경우 비밀번호 필수
    if (!myPageData.provider && !updateForm.currentPassword) {
      alert("현재 비밀번호를 입력해주세요.");
      return;
    }

    // ✅ 이메일이 변경되었는데 인증이 완료되지 않은 경우
    if (
      updateForm.email !== myPageData.email &&
      !emailVerification.isVerified
    ) {
      alert("새 이메일 인증을 완료해주세요.");
      return;
    }

    // 새 비밀번호 입력 시 확인
    if (updateForm.newPassword) {
      if (updateForm.newPassword !== updateForm.newPasswordCheck) {
        alert("새 비밀번호가 일치하지 않습니다.");
        return;
      }

      // 비밀번호 복잡도 체크
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
      if (!passwordRegex.test(updateForm.newPassword)) {
        alert(
          "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
        );
        return;
      }
    }

    try {
      const res = await api.put("/api/mypage/update", updateForm);

      if (res.data.success) {
        alert(res.data.message);

        // ✅ 이메일 인증 상태 초기화
        setEmailVerification({
          isChanging: false,
          newEmail: "",
          verificationCode: "",
          isCodeSent: false,
          isSending: false,
          isVerifying: false,
          isVerified: false,
          timer: 300,
          timerInterval: null,
        });

        fetchMyPageData(); // 데이터 갱신
        // 비밀번호 필드 초기화
        setUpdateForm({
          ...updateForm,
          currentPassword: "",
          newPassword: "",
          newPasswordCheck: "",
        });
      }
    } catch (err) {
      alert("수정 실패: " + (err.response?.data?.message || err.message));
    }
  };

  // 회원탈퇴
  const handleDeleteAccount = async () => {
    if (
      !window.confirm(
        "정말로 회원탈퇴 하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다."
      )
    ) {
      return;
    }

    // OAuth 사용자가 아닌 경우 비밀번호 확인
    if (!myPageData.provider && !deletePassword) {
      alert("비밀번호를 입력해주세요.");
      return;
    }

    try {
      const res = await api.delete("/api/mypage/delete", {
        data: { password: deletePassword },
      });

      if (res.data.success) {
        alert(res.data.message);
        localStorage.removeItem("token");
        localStorage.removeItem("role");

        window.location.href = "/";
      }
    } catch (err) {
      alert("탈퇴 실패: " + (err.response?.data?.message || err.message));
    }
  };

  // 게시글 클릭 시 해당 게시판으로 이동
  const handlePostClick = (post) => {
    const boardMap = {
      free: "freeboard",
      counsel: "counselboard",
      info: "infoboard",
    };
    const board = boardMap[post.boardType];
    navigate(`/${board}?tab=detail&postId=${post.id}`);
  };

  // 댓글 클릭 시 해당 게시글로 이동
  const handleCommentClick = (comment) => {
    const boardMap = {
      free: "freeboard",
      counsel: "counselboard",
      info: "infoboard",
    };
    const board = boardMap[comment.boardType];
    navigate(`/${board}?tab=detail&postId=${comment.boardId}`);
  };

  // 비밀번호 인증 화면
  if (!isVerified) {
    return (
      <div className="mypage-container">
        <form className="password-verify-box" onSubmit={handleVerifyPassword}>
          <h2 className="verify-title">본인 확인</h2>
          <p className="verify-subtitle">
            마이페이지 접근을 위해 비밀번호를 입력해주세요
          </p>
          <input
            type="password"
            className="verify-input"
            placeholder="비밀번호"
            value={verifyPassword}
            onChange={(e) => setVerifyPassword(e.target.value)}
            disabled={isVerifying}
            required
          />
          <button type="submit" className="verify-btn" disabled={isVerifying}>
            {isVerifying ? "확인 중..." : "확인"}
          </button>
        </form>
      </div>
    );
  }

  // 로딩 중
  if (isLoading || !myPageData) {
    return (
      <div className="mypage-container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="mypage-container">
      <div className="mypage-wrapper">
        {/* 탭 버튼 */}
        <div className="mypage-tabs">
          <button
            onClick={() => setActiveTab("info")}
            className={`mypage-tab-btn ${activeTab === "info" ? "active" : ""}`}
          >
            회원정보
          </button>
          <button
            onClick={() => navigate(`/user/${myPageData.username}`)}
            className="mypage-tab-btn"
          >
            활동내역 보기
          </button>
          <button
            onClick={() => setActiveTab("delete")}
            className={`mypage-tab-btn ${
              activeTab === "delete" ? "active" : ""
            }`}
          >
            회원탈퇴
          </button>
        </div>

        {/* 회원정보 탭 */}
        {activeTab === "info" && (
          <div className="mypage-content-box">
            <h3 className="section-title">기본 정보</h3>

            <div className="user-info-grid">
              <div className="info-item">
                <div className="info-label">아이디</div>
                <div className="info-value">{myPageData.username}</div>
              </div>
              <div className="info-item">
                <div className="info-label">닉네임</div>
                <div className="info-value">{myPageData.nickname}</div>
              </div>
              <div className="info-item">
                <div className="info-label">이메일</div>
                <div className="info-value">{myPageData.email}</div>
              </div>
              <div className="info-item">
                <div className="info-label">가입일</div>
                <div className="info-value">
                  {new Date(myPageData.createAccount).toLocaleDateString(
                    "ko-KR"
                  )}
                </div>
              </div>
              <div className="info-item">
                <div className="info-label">계정 유형</div>
                <div className="info-value">
                  {myPageData.provider ? (
                    <>
                      OAuth 연동
                      <span className="oauth-badge">
                        {myPageData.provider.toUpperCase()}
                      </span>
                    </>
                  ) : (
                    "일반 회원"
                  )}
                </div>
              </div>
            </div>

            <h3 className="section-title">활동 통계</h3>
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-number">
                  {myPageData.activityStats.totalPosts}
                </div>
                <div className="stat-label">작성 게시글</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">
                  {myPageData.activityStats.totalComments}
                </div>
                <div className="stat-label">작성 댓글</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">
                  {myPageData.activityStats.totalLikesReceived}
                </div>
                <div className="stat-label">받은 좋아요</div>
              </div>
            </div>

            <h3 className="section-title">정보 수정</h3>
            <form onSubmit={handleUpdateInfo}>
              <div className="form-group">
                <label className="form-label">현재 비밀번호 *</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="현재 비밀번호"
                  value={updateForm.currentPassword}
                  onChange={(e) =>
                    setUpdateForm({
                      ...updateForm,
                      currentPassword: e.target.value,
                    })
                  }
                />
              </div>

              {/* ✅ 모든 사용자에게 비밀번호 설정/변경 가능 */}
              <div className="form-group">
                <label className="form-label">
                  {myPageData.provider
                    ? "비밀번호 설정"
                    : "새 비밀번호 (변경 시에만 입력)"}
                </label>
                <input
                  type="password"
                  className="form-input"
                  placeholder={
                    myPageData.provider ? "비밀번호 설정" : "새 비밀번호"
                  }
                  value={updateForm.newPassword}
                  onChange={(e) =>
                    setUpdateForm({
                      ...updateForm,
                      newPassword: e.target.value,
                    })
                  }
                />
                <p className="form-note">영문, 숫자, 특수문자 포함 8자 이상</p>
              </div>

              <div className="form-group">
                <label className="form-label">
                  {myPageData.provider ? "비밀번호 확인" : "새 비밀번호 확인"}
                </label>
                <input
                  type="password"
                  className="form-input"
                  placeholder={
                    myPageData.provider ? "비밀번호 확인" : "새 비밀번호 확인"
                  }
                  value={updateForm.newPasswordCheck}
                  onChange={(e) =>
                    setUpdateForm({
                      ...updateForm,
                      newPasswordCheck: e.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label className="form-label">닉네임 *</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="닉네임"
                  value={updateForm.nickname}
                  onChange={(e) =>
                    setUpdateForm({ ...updateForm, nickname: e.target.value })
                  }
                  required
                />
              </div>

              {/* ✅ 이메일 입력 및 인증 */}
              <div className="form-group">
                <label className="form-label">이메일 *</label>

                {!emailVerification.isChanging ? (
                  // 이메일 변경 전 - 현재 이메일 표시
                  <div
                    style={{
                      display: "flex",
                      gap: "12px",
                      alignItems: "center",
                    }}
                  >
                    <input
                      type="email"
                      className="form-input"
                      value={updateForm.email}
                      onChange={(e) =>
                        setUpdateForm({ ...updateForm, email: e.target.value })
                      }
                      required
                    />
                    {updateForm.email !== myPageData.email && (
                      <button
                        type="button"
                        className="primary-btn"
                        style={{ whiteSpace: "nowrap", padding: "16px 24px" }}
                        onClick={handleStartEmailChange}
                      >
                        인증하기
                      </button>
                    )}
                  </div>
                ) : (
                  // 이메일 변경 중 - 인증 프로세스
                  <div
                    style={{
                      padding: "20px",
                      background: "rgba(30, 30, 30, 0.5)",
                      border: "1px solid #444",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        gap: "12px",
                        alignItems: "center",
                        marginBottom: "16px",
                      }}
                    >
                      <input
                        type="email"
                        className="form-input"
                        placeholder="새 이메일 입력"
                        value={emailVerification.newEmail}
                        onChange={(e) => {
                          setEmailVerification({
                            ...emailVerification,
                            newEmail: e.target.value,
                          });
                          setUpdateForm({
                            ...updateForm,
                            email: e.target.value,
                          });
                        }}
                        disabled={
                          emailVerification.isCodeSent ||
                          emailVerification.isVerified
                        }
                        style={{ margin: 0 }}
                      />

                      {!emailVerification.isVerified && (
                        <>
                          {!emailVerification.isCodeSent ? (
                            <button
                              type="button"
                              className="primary-btn"
                              style={{
                                whiteSpace: "nowrap",
                                padding: "16px 24px",
                                background: "rgba(16, 185, 129, 0.2)",
                                color: "#10b981",
                                borderColor: "#10b981",
                              }}
                              onClick={handleSendVerificationCode}
                              disabled={emailVerification.isSending}
                            >
                              {emailVerification.isSending
                                ? "발송 중..."
                                : "인증코드 발송"}
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="primary-btn"
                              style={{
                                whiteSpace: "nowrap",
                                padding: "16px 24px",
                                background: "rgba(100, 100, 100, 0.2)",
                                color: "#888",
                                borderColor: "#666",
                              }}
                              onClick={handleSendVerificationCode}
                              disabled={emailVerification.isSending}
                            >
                              재발송
                            </button>
                          )}
                        </>
                      )}
                    </div>

                    {emailVerification.isCodeSent &&
                      !emailVerification.isVerified && (
                        <div
                          style={{
                            display: "flex",
                            gap: "12px",
                            alignItems: "center",
                            marginBottom: "16px",
                          }}
                        >
                          <input
                            type="text"
                            className="form-input"
                            placeholder="인증코드 6자리"
                            value={emailVerification.verificationCode}
                            onChange={(e) =>
                              setEmailVerification({
                                ...emailVerification,
                                verificationCode: e.target.value,
                              })
                            }
                            maxLength={6}
                            style={{
                              margin: 0,
                              textAlign: "center",
                              letterSpacing: "4px",
                            }}
                          />
                          <button
                            type="button"
                            className="primary-btn"
                            style={{
                              whiteSpace: "nowrap",
                              padding: "16px 24px",
                              background: "rgba(16, 185, 129, 0.2)",
                              color: "#10b981",
                              borderColor: "#10b981",
                            }}
                            onClick={handleVerifyCode}
                            disabled={emailVerification.isVerifying}
                          >
                            {emailVerification.isVerifying
                              ? "확인 중..."
                              : "인증하기"}
                          </button>
                          <span
                            style={{
                              color: "#ff3b30",
                              fontSize: "14px",
                              fontWeight: 900,
                              minWidth: "50px",
                              textAlign: "center",
                            }}
                          >
                            {formatTimer(emailVerification.timer)}
                          </span>
                        </div>
                      )}

                    {emailVerification.isVerified && (
                      <div
                        style={{
                          padding: "12px",
                          background: "rgba(16, 185, 129, 0.2)",
                          color: "#10b981",
                          border: "1px solid #10b981",
                          textAlign: "center",
                          fontSize: "13px",
                          fontWeight: 900,
                          letterSpacing: "1px",
                          marginBottom: "16px",
                        }}
                      >
                        ✓ 이메일 인증 완료
                      </div>
                    )}

                    <button
                      type="button"
                      style={{
                        padding: "12px 20px",
                        background: "transparent",
                        color: "#888",
                        border: "1px solid #666",
                        cursor: "pointer",
                        fontSize: "12px",
                        fontWeight: 700,
                        width: "100%",
                        transition: "all 0.3s",
                      }}
                      onClick={handleCancelEmailChange}
                    >
                      변경 취소
                    </button>
                  </div>
                )}
              </div>

              <div className="button-group">
                <button type="submit" className="primary-btn">
                  정보 수정
                </button>
              </div>
            </form>
          </div>
        )}

        {/* 회원탈퇴 탭 */}
        {activeTab === "delete" && (
          <div className="mypage-content-box">
            <h3 className="section-title">회원 탈퇴</h3>
            <p style={{ color: "#888", marginBottom: "32px", lineHeight: 1.8 }}>
              회원 탈퇴 시 모든 데이터가 영구적으로 삭제되며 복구할 수 없습니다.
              <br />
              작성한 게시글, 댓글 등 모든 활동 내역이 삭제됩니다.
            </p>

            {!myPageData.provider && (
              <div className="form-group">
                <label className="form-label">비밀번호 확인 *</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="비밀번호"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  style={{ maxWidth: "400px" }}
                />
              </div>
            )}

            <div className="button-group">
              <button onClick={handleDeleteAccount} className="danger-btn">
                회원 탈퇴
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
