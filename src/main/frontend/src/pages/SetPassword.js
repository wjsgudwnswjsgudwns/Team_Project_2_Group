import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "./Signup.css"; // 기존 Signup.css 재사용

function SetPassword() {
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [errors, setErrors] = useState({});
  const [passwordValid, setPasswordValid] = useState({
    hasLetter: false,
    hasDigit: false,
    hasSpecial: false,
    minLength: false,
  });

  const navigate = useNavigate();

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

  // 비밀번호 설정 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});

    // 비밀번호 유효성 검사
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

    try {
      await api.post("/api/auth/set-password", {
        password,
        passwordCheck,
      });

      alert("비밀번호가 설정되었습니다!");
      navigate("/", { replace: true });
    } catch (err) {
      console.error("비밀번호 설정 실패:", err);

      if (err.response && err.response.status === 400) {
        const errorData = err.response.data;
        setErrors(errorData);
      } else {
        alert("비밀번호 설정에 실패했습니다.");
      }
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-box">
        <h2 className="signup-title">비밀번호 설정</h2>
        <p style={{ textAlign: "center", color: "#666", marginBottom: "20px" }}>
          소셜 로그인으로 가입하셨습니다.
          <br />
          일반 로그인도 사용하시려면 비밀번호를 설정해주세요.
        </p>

        <form onSubmit={handleSubmit} className="signup-form">
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

          {/* 비밀번호 규칙 안내 */}
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

          <button type="submit" className="signup-submit-btn">
            비밀번호 설정 완료
          </button>
        </form>
      </div>
    </div>
  );
}

export default SetPassword;
