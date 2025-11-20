import { useLocation, useNavigate } from "react-router-dom";
import "./CompatibilityResult.css";

function CompatibilityResult() {
  const cpuImage = process.env.REACT_APP_CPU_IMAGES_URL;
  const gpuImage = process.env.REACT_APP_GPU_IMAGES_URL;
  const caseImage = process.env.REACT_APP_CASE_IMAGES_URL;
  const mainboardImage = process.env.REACT_APP_MAINBOARD_IMAGES_URL;
  const powerImage = process.env.REACT_APP_POWER_IMAGES_URL;
  const ramImage = process.env.REACT_APP_RAM_IMAGES_URL;

  const location = useLocation();
  const navigate = useNavigate();

  const raw = location.state?.results;
  let results = Array.isArray(raw) ? raw : [];

  // ✅ 에러 처리 강화
  if (results.length === 0) {
    return (
      <div className="compatibility-result">
        <h2 className="compatibility-title">호환성 검사 결과</h2>
        <div className="back-button-container">
          <button onClick={() => navigate(-1)} className="back-button">
            ← 돌아가기
          </button>
        </div>
        <div className="detail-section">
          <p style={{ textAlign: "center", color: "#666", padding: "40px" }}>
            호환성 검사 결과가 없습니다.
          </p>
        </div>
      </div>
    );
  }

  const getStatus = (answer) => {
    if (!answer) return "unknown";
    const text = answer.toLowerCase();
    if (text.includes("호환됨") || text.includes("장착가능")) return "success";
    if (text.includes("호환안됨") || text.includes("장착불가")) return "error";
    if (text.includes("조건부")) return "warning";
    return "unknown";
  };

  // ✅ 안전한 호환성 맵 생성
  const compatMap = results.reduce((acc, r) => {
    if (r && r.question) {
      acc[r.question] = {
        status: getStatus(r.answer),
        answer: r.answer || r.error || "응답 없음",
      };
    }
    return acc;
  }, {});

  // ✅ 안전한 호환성 체크 헬퍼
  const getCompatInfo = (questionKey) => {
    return compatMap[questionKey] || { status: "unknown", answer: "정보 없음" };
  };

  const getStatusIcon = (status) => {
    if (status === "success") return "✓";
    if (status === "error") return "✕";
    if (status === "warning") return "⚠";
    return "?";
  };

  const getStatusText = (status) => {
    if (status === "success") return "성공";
    if (status === "error") return "불가";
    if (status === "warning") return "조건부";
    return "확인필요";
  };

  return (
    <div className="compatibility-result">
      <h2 className="compatibility-title">호환성 검사 결과</h2>
      <div className="back-button-container">
        <button onClick={() => navigate(-1)} className="back-button">
          ← 돌아가기
        </button>
      </div>
      <div className="diagram-container">
        <div className="diagram-layout">
          {/* 상단: CPU ↔ 메모리 */}
          <div className="top-row">
            <div className="component-box">
              <span className="component-icon">
                <img src={cpuImage} alt="CPU"></img>
              </span>
              <div className="component-label">CPU</div>
            </div>

            <div className="horizontal-link">
              <span className="arrow-icon">↔</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("CPU와 메모리 호환성").status
                }`}
              >
                {getStatusText(getCompatInfo("CPU와 메모리 호환성").status)}
              </div>
              <span className="arrow-icon">↔</span>
            </div>

            <div className="component-box">
              <span className="component-icon">
                <img src={ramImage} alt="메모리"></img>
              </span>
              <div className="component-label">메모리</div>
            </div>
          </div>

          {/* CPU와 메모리 아래 화살표들 */}
          <div className="middle-row">
            <div className="left-vertical">
              <span className="arrow-icon">↕</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("CPU와 메인보드 호환성").status
                }`}
              >
                {getStatusText(getCompatInfo("CPU와 메인보드 호환성").status)}
              </div>
              <span className="arrow-icon">↕</span>
            </div>

            <div className="spacer"></div>

            <div className="right-vertical">
              <span className="arrow-icon">↕</span>
            </div>
          </div>

          {/* 메인보드 행 */}
          <div className="mainboard-row">
            <div className="component-box">
              <span className="component-icon">
                <img src={mainboardImage} alt="메인보드"></img>
              </span>
              <div className="component-label">메인보드</div>
            </div>

            <div className="horizontal-link-reverse">
              <span className="arrow-icon">↔</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("메모리와 메인보드 호환성").status
                }`}
              >
                {getStatusText(
                  getCompatInfo("메모리와 메인보드 호환성").status
                )}
              </div>
            </div>

            <div className="empty-space"></div>
          </div>

          {/* 메인보드 아래 */}
          <div className="middle-row">
            <div className="left-vertical">
              <span className="arrow-icon">↕</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("케이스와 메인보드 장착").status
                }`}
              >
                {getStatusText(getCompatInfo("케이스와 메인보드 장착").status)}
              </div>
              <span className="arrow-icon">↕</span>
            </div>

            <div className="spacer"></div>
            <div className="spacer"></div>
          </div>

          {/* 케이스 ↔ 파워 */}
          <div className="top-row">
            <div className="component-box">
              <span className="component-icon">
                <img src={caseImage} alt="케이스"></img>
              </span>
              <div className="component-label">케이스</div>
            </div>

            <div className="horizontal-link">
              <span className="arrow-icon">↔</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("케이스와 파워 호환성").status
                }`}
              >
                {getStatusText(getCompatInfo("케이스와 파워 호환성").status)}
              </div>
              <span className="arrow-icon">↔</span>
            </div>

            <div className="component-box">
              <span className="component-icon">
                <img src={powerImage} alt="파워"></img>
              </span>
              <div className="component-label">파워</div>
            </div>
          </div>

          {/* 케이스 아래 */}
          <div className="middle-row">
            <div className="left-vertical">
              <span className="arrow-icon">↕</span>
              <div
                className={`status-badge status-${
                  getCompatInfo("케이스와 GPU 장착").status
                }`}
              >
                {getStatusText(getCompatInfo("케이스와 GPU 장착").status)}
              </div>
              <span className="arrow-icon">↕</span>
            </div>

            <div className="spacer"></div>
            <div className="spacer"></div>
          </div>

          {/* 그래픽카드 */}
          <div className="bottom-row">
            <div className="component-box">
              <span className="component-icon">
                <img src={gpuImage} alt="그래픽카드"></img>
              </span>
              <div className="component-label">그래픽카드</div>
            </div>
          </div>
        </div>
      </div>
      <div className="detail-section">
                <h3 className="detail-title">상세 호환성 정보</h3>
                <hr className="result-divider" />     
        <div className="detail-list">
          {results.map((r, i) => {
            if (!r || !r.question) return null;
            const status = getStatus(r.answer);
            return (
              <div key={i} className={`detail-item status-${status}`}>
                <div className="status-icon">{getStatusIcon(status)}</div>     
                <div className="compatibility-content-box">
                  <div className="compatibility-question">{r.question}</div>   
                  <div className="compatibility-answer">
                    {r.answer || r.error || "응답 없음"}   
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default CompatibilityResult;
