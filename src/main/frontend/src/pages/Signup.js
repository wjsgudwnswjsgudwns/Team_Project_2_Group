import { useState, useEffect } from "react"; // ✅ useEffect import 확인
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./Signup.css";

function Signup() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [errors, setErrors] = useState({});

  // ✅ 이메일 인증 상태
  const [verificationCode, setVerificationCode] = useState("");
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isVerifyingCode, setIsVerifyingCode] = useState(false);
  const [timer, setTimer] = useState(0);

  const [passwordValid, setPasswordValid] = useState({
    hasLetter: false,
    hasDigit: false,
    hasSpecial: false,
    minLength: false,
  });

  const navigate = useNavigate();

  // ✅ 타이머 카운트다운 (useState → useEffect로 수정)
  useEffect(() => {
    if (timer > 0) {
      const interval = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [timer]);

  // ✅ 이메일 인증 코드 발송
  const handleSendVerificationCode = async () => {
    if (!email) {
      alert("이메일을 입력해주세요.");
      return;
    }

    // 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      alert("올바른 이메일 형식이 아닙니다.");
      return;
    }

    setIsSendingCode(true);

    try {
      const res = await api.post("/api/email/send-code", {
        email,
        purpose: "SIGNUP",
      });

      if (res.data.success) {
        alert("인증 코드가 발송되었습니다. 이메일을 확인해주세요.");
        setIsCodeSent(true);
        setTimer(300); // 5분 타이머
        setVerificationCode(""); // 인증 코드 입력란 초기화
      }
    } catch (err) {
      alert(
        "인증 코드 발송 실패: " + (err.response?.data?.message || err.message)
      );
    } finally {
      setIsSendingCode(false);
    }
  };

  // ✅ 인증 코드 확인
  const handleVerifyCode = async () => {
    if (!verificationCode) {
      alert("인증 코드를 입력해주세요.");
      return;
    }

    setIsVerifyingCode(true);

    try {
      const res = await api.post("/api/email/verify-code", {
        email,
        code: verificationCode,
        purpose: "SIGNUP",
      });

      if (res.data.success) {
        alert("이메일 인증이 완료되었습니다.");
        setIsEmailVerified(true);
        setTimer(0);
      }
    } catch (err) {
      alert(err.response?.data?.message || "인증 코드가 올바르지 않습니다.");
    } finally {
      setIsVerifyingCode(false);
    }
  };

  // 회원 가입
  const handleSignup = async (e) => {
    e.preventDefault();
    setErrors({});

    // ✅ 이메일 인증 확인
    if (!isEmailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    // ✅ 비밀번호 정규식 (영문 + 숫자 + 특수문자 + 8자 이상)
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

    if (!passwordRegex.test(password)) {
      alert(
        "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
      );
      return;
    }

    if (password !== passwordCheck) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    // 전송할 데이터 확인
    const signupData = {
      username,
      password,
      passwordCheck,
      email,
      nickname,
    };

    try {
      const response = await api.post("/api/auth/signup", signupData);
      alert("회원 가입 성공");
      navigate("/login");
    } catch (err) {
      if (err.response && err.response.status === 400) {
        const errorData = err.response.data;
        setErrors(errorData);
      } else {
        alert("회원 가입 실패: " + err.message);
      }
    }
  };

  // 실시간 비밀번호 유효성 검사
  const handlePasswordChange = (value) => {
    setPassword(value);

    setPasswordValid({
      hasLetter: /[A-Za-z]/.test(value),
      hasDigit: /\d/.test(value),
      hasSpecial: /[^A-Za-z0-9]/.test(value),
      minLength: value.length >= 8,
    });
  };

  // ✅ 타이머 포맷 (MM:SS)
  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  return (
    <div className="signup-container">
      <div className="signup-box">
        <h2 className="signup-title">회원가입</h2>
        <form onSubmit={handleSignup} className="signup-form">
          <input
            type="text"
            className="signup-input"
            placeholder="아이디"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          {errors.username && <p className="signup-error">{errors.username}</p>}
          {errors.userIdDuplicated && (
            <p className="signup-error">{errors.userIdDuplicated}</p>
          )}

          <input
            type="text"
            className="signup-input"
            placeholder="닉네임"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
          />
          {errors.nickname && <p className="signup-error">{errors.nickname}</p>}
          {errors.nicknameDuplicated && (
            <p className="signup-error">{errors.nicknameDuplicated}</p>
          )}

          <input
            type="password"
            className="signup-input"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => handlePasswordChange(e.target.value)}
            required
          />
          {errors.password && <p className="signup-error">{errors.password}</p>}
          {errors.passwordComplexity && (
            <p className="signup-error">{errors.passwordComplexity}</p>
          )}
          {errors.passwordLengthError && (
            <p className="signup-error">{errors.passwordLengthError}</p>
          )}

          {/* ✅ 비밀번호 규칙 안내 UI */}
          <div className="password-rules">
            <p className={passwordValid.hasLetter ? "valid" : "invalid"}>
              {passwordValid.hasLetter ? "✅" : "❌"} 영문 포함
            </p>
            <p className={passwordValid.hasDigit ? "valid" : "invalid"}>
              {passwordValid.hasDigit ? "✅" : "❌"} 숫자 포함
            </p>
            <p className={passwordValid.hasSpecial ? "valid" : "invalid"}>
              {passwordValid.hasSpecial ? "✅" : "❌"} 특수문자 포함
            </p>
            <p className={passwordValid.minLength ? "valid" : "invalid"}>
              {passwordValid.minLength ? "✅" : "❌"} 최소 8자 이상
            </p>
          </div>

          <input
            type="password"
            className="signup-input"
            placeholder="비밀번호 확인"
            value={passwordCheck}
            onChange={(e) => setPasswordCheck(e.target.value)}
            required
          />
          {errors.passwordNotSame && (
            <p className="signup-error">{errors.passwordNotSame}</p>
          )}

          {/* ✅ 이메일 입력 및 인증 */}
          <div style={{ position: "relative" }}>
            <input
              type="email"
              className="signup-input"
              placeholder="이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isEmailVerified}
              required
              style={{
                paddingRight: "120px",
                background: isEmailVerified ? "#1a3a1a" : "",
                borderColor: isEmailVerified ? "#10b981" : "",
              }}
            />
            {!isEmailVerified && (
              <button
                type="button"
                onClick={handleSendVerificationCode}
                disabled={isSendingCode || (isCodeSent && timer > 0)}
                style={{
                  position: "absolute",
                  right: "8px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  padding: "8px 16px",
                  background: isCodeSent && timer > 0 ? "#555" : "#ffffff",
                  color: isCodeSent && timer > 0 ? "#aaa" : "#000",
                  border: "1px solid #555",
                  cursor: isCodeSent && timer > 0 ? "not-allowed" : "pointer",
                  fontSize: "12px",
                  fontWeight: "700",
                }}
              >
                {isSendingCode
                  ? "발송중..."
                  : isCodeSent && timer > 0
                  ? "발송완료"
                  : "인증코드 발송"}
              </button>
            )}
            {isEmailVerified && (
              <span
                style={{
                  position: "absolute",
                  right: "16px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "#10b981",
                  fontSize: "12px",
                  fontWeight: "700",
                }}
              >
                ✅ 인증완료
              </span>
            )}
          </div>
          {errors.email && <p className="signup-error">{errors.email}</p>}
          {errors.emailNotVerified && (
            <p className="signup-error">{errors.emailNotVerified}</p>
          )}

          {/* ✅ 인증 코드 입력 */}
          {isCodeSent && !isEmailVerified && (
            <div style={{ position: "relative", marginBottom: "30px" }}>
              <input
                type="text"
                className="signup-input"
                placeholder="인증 코드 6자리"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
                maxLength={6}
                style={{ paddingRight: "120px" }}
              />
              <button
                type="button"
                onClick={handleVerifyCode}
                disabled={isVerifyingCode}
                style={{
                  position: "absolute",
                  right: "8px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  padding: "8px 16px",
                  background: "#ffffff",
                  color: "#000",
                  border: "1px solid #555",
                  cursor: "pointer",
                  fontSize: "12px",
                  fontWeight: "700",
                }}
              >
                {isVerifyingCode ? "확인중..." : "인증하기"}
              </button>
              {timer > 0 && (
                <span
                  style={{
                    position: "absolute",
                    left: "16px",
                    bottom: "-24px",
                    color: "#ff3b30",
                    fontSize: "11px",
                    fontWeight: "700",
                  }}
                >
                  ⏱ {formatTimer(timer)}
                </span>
              )}
            </div>
          )}

          <button type="submit" className="signup-submit-btn">
            회원가입
          </button>
        </form>
        <div className="signup-login-link">
          이미 계정이 있으신가요?
          <Link to="/login">로그인</Link>
        </div>
      </div>
    </div>
  );
}

export default Signup;
