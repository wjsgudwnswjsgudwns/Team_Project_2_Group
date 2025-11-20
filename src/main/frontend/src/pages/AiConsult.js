import React, { useState, useRef } from "react";
import "./AiConsult.css";

const AiConsult = () => {
  const [aiResult, setAiResult] = useState([]);
  const [productImages, setProductImages] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [selectedEstimate, setSelectedEstimate] = useState(0);

  const [formData, setFormData] = useState({
    usage: "고성능 게임",
    customUsage: "",
    minBudget: 100,
    maxBudget: 200,
    cpu: "상관없음",
    gpu: "상관없음",
    mainboard: "상관없음",
    memory: "상관없음",
  });

  const [activeField, setActiveField] = useState(null);
  const [dropdownPosition, setDropdownPosition] = useState({
    left: 0,
    top: 0,
    minWidth: 0,
  });

  const options = {
    usage: ["고성능 게임", "영상 편집", "작곡", "사무용", "상관없음", "기타"],
    cpu: ["인텔", "AMD", "상관없음"],
    gpu: ["엔비디아", "AMD", "상관없음"],
    mainboard: ["인텔", "AMD", "상관없음"],
    memory: ["64G", "32G", "16G", "8G", "상관없음"],
  };

  const fieldOrder = [
    { id: "usage", label: "사용 용도" },
    { id: "budget", label: "금액 범위" },
    { id: "cpu", label: "CPU" },
    { id: "gpu", label: "그래픽 카드" },
    { id: "mainboard", label: "메인 보드" },
    { id: "memory", label: "메모리" },
  ];

  const itemRefs = useRef(new Map());

  const handleFieldClick = (field) => {
    if (activeField === field) {
      setActiveField(null);
      return;
    }

    const ref = itemRefs.current.get(field);
    if (ref) {
      setDropdownPosition({
        left: ref.offsetLeft,
        top: ref.offsetTop + ref.offsetHeight + 10,
        minWidth: ref.offsetWidth,
      });
      setActiveField(field);
    }
  };

  const handleChange = (name, value) => {
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
    if (
      name !== "minBudget" &&
      name !== "maxBudget" &&
      name !== "customUsage"
    ) {
      setActiveField(null);
    }
  };

  const handleBudgetChange = (type, value) => {
    handleChange(type, Number(value));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setIsLoading(true);
    setAiResult([]);
    setSelectedEstimate(0);

    try {
      const response = await fetch("http://localhost:8880/api/ai/consult", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ formData }),
      });

      const data = await response.json();
      console.log("AI 응답:", data.result);

      let jsonText = data.result || "";
      const start = jsonText.indexOf("[");
      const end = jsonText.lastIndexOf("]");
      if (start !== -1 && end !== -1 && end > start) {
        jsonText = jsonText.substring(start, end + 1);
      }

      jsonText = jsonText.replace(/```json|```/g, "").trim();

      let parsedResult = [];
      try {
        parsedResult = JSON.parse(jsonText);
        console.log("✅ JSON 파싱 성공:", parsedResult);

        const enrichedEstimates = await Promise.all(
          parsedResult.map(async (estimate) => {
            const enrichedParts = await Promise.all(
              estimate.부품목록.map(async (item) => {
                try {
                  const priceResponse = await fetch(
                    `http://localhost:8880/api/price/product-info?productName=${encodeURIComponent(
                      item["제품명"]
                    )}`
                  );
                  const priceData = await priceResponse.json();

                  const imageResponse = await fetch(
                    `http://localhost:8880/api/image/search?productName=${encodeURIComponent(
                      item["제품명"]
                    )}`
                  );
                  const imageData = await imageResponse.json();

                  return {
                    ...item,
                    제품이미지: imageData.success ? imageData.imageUrl : "",
                    가격:
                      priceData.success && priceData.info["최저가"]
                        ? priceData.info["최저가"]
                        : "가격 정보 없음",
                    판매사이트링크:
                      priceData.success && priceData.info["링크"]
                        ? priceData.info["링크"]
                        : `https://search.danawa.com/dsearch.php?query=${encodeURIComponent(
                            item["제품명"]
                          )}`,
                  };
                } catch (error) {
                  console.error("정보 조회 실패:", item["제품명"], error);
                  return item;
                }
              })
            );

            return {
              ...estimate,
              부품목록: enrichedParts,
            };
          })
        );

        parsedResult = enrichedEstimates;
      } catch (err) {
        console.warn("⚠️ JSON 재파싱 실패", jsonText);
      }

      setAiResult(parsedResult);
    } catch (err) {
      console.error(err);
      alert("AI 요청 실패");
    } finally {
      setIsLoading(false);
    }
  };

  const getDefaultImage = (productType) => {
    const colors = {
      CPU: "#4CAF50",
      그래픽카드: "#2196F3",
      GPU: "#2196F3",
      메인보드: "#FF9800",
      메모리: "#9C27B0",
      RAM: "#9C27B0",
      SSD: "#607D8B",
      저장장치: "#607D8B",
      파워서플라이: "#F44336",
    };

    const color = colors[productType] || "#757575";
    const text = productType || "PC Part";

    const svg = `
      <svg width="60" height="60" xmlns="http://www.w3.org/2000/svg">
        <rect width="60" height="60" fill="${color}"/>
        <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" 
              font-family="Arial, sans-serif" font-size="10" fill="white" font-weight="bold">
          ${text}
        </text>
      </svg>
    `;

    return `data:image/svg+xml;base64,${btoa(
      unescape(encodeURIComponent(svg))
    )}`;
  };

  const renderOptionPanel = (fieldId, currentOptions) => (
    <div className="option-panel">
      {currentOptions.map((option) => (
        <div
          key={option}
          className={`option-item ${
            formData[fieldId] === option ? "selected" : ""
          }`}
          onClick={() => handleChange(fieldId, option)}
        >
          {option}
        </div>
      ))}
      {fieldId === "usage" && formData.usage === "기타" && (
        <div className="custom-usage-input">
          <input
            type="text"
            placeholder="사용 용도를 입력하세요"
            value={formData.customUsage}
            onChange={(e) => handleChange("customUsage", e.target.value)}
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
    </div>
  );

  const renderBudgetPanel = () => (
    <div className="option-panel budget-panel">
      <div className="budget-range-display">
        {formData.minBudget}만 원 ~ {formData.maxBudget}만 원
      </div>
      <div className="slider-controls">
        <label>최소 예산 ({formData.minBudget}만 원)</label>
        <div className="slider-with-input">
          <input
            type="range"
            min="50"
            max="500"
            step="10"
            value={formData.minBudget}
            onChange={(e) => handleBudgetChange("minBudget", e.target.value)}
          />
          <input
            type="number"
            min="50"
            max="500"
            step="10"
            value={formData.minBudget}
            onChange={(e) => handleBudgetChange("minBudget", e.target.value)}
            className="budget-number-input"
          />
        </div>
        <label>최대 예산 ({formData.maxBudget}만 원)</label>
        <div className="slider-with-input">
          <input
            type="range"
            min="50"
            max="500"
            step="10"
            value={formData.maxBudget}
            onChange={(e) => handleBudgetChange("maxBudget", e.target.value)}
          />
          <input
            type="number"
            min="50"
            max="500"
            step="10"
            value={formData.maxBudget}
            onChange={(e) => handleBudgetChange("maxBudget", e.target.value)}
            className="budget-number-input"
          />
        </div>
      </div>
    </div>
  );

  return (
    <div className="ai-consult-form-grid">
      <div className="form-grid-container">
        {fieldOrder.map((field) => (
          <React.Fragment key={field.id}>
            <div
              ref={(el) => itemRefs.current.set(field.id, el)}
              className={`form-item ${
                activeField === field.id ? "active" : ""
              }`}
              onClick={() => handleFieldClick(field.id)}
            >
              <label className="input-label">{field.label}</label>
              <div className="selected-value">
                {field.id === "budget"
                  ? `${formData.minBudget}만 ~ ${formData.maxBudget}만`
                  : field.id === "usage" && formData.usage === "기타"
                  ? `기타: ${formData.customUsage || "(입력 필요)"}`
                  : formData[field.id]}
              </div>
            </div>
          </React.Fragment>
        ))}

        <button
          type="button"
          onClick={handleSubmit}
          className="search-button"
          disabled={isLoading}
        >
          {isLoading ? "AI 견적 생성 중..." : "검색하기"}
        </button>
      </div>

      {activeField && (
        <div
          className="dropdown-wrapper"
          style={{
            left: dropdownPosition.left,
            top: dropdownPosition.top,
            minWidth: dropdownPosition.minWidth,
          }}
        >
          <button
            className="close-panel-btn"
            onClick={() => setActiveField(null)}
          >
            &times;
          </button>

          {activeField === "budget" && renderBudgetPanel()}
          {options[activeField] &&
            renderOptionPanel(activeField, options[activeField])}
        </div>
      )}

      {isLoading && (
        <div className="loading-overlay">
          <div className="loading-content">
            <div className="loading-spinner"></div>
            <p className="loading-text">
              AI가 최적의 견적을 생성하고 있습니다...
            </p>
            <p className="loading-subtext">약 2분정도 시간이 소요됩니다. </p>
            <p className="loading-subtext">
              AI가 정보 제공 시 실수를 할 수 있으니 다시 한번 확인하세요.{" "}
            </p>
          </div>
        </div>
      )}

      {!isLoading &&
        aiResult &&
        Array.isArray(aiResult) &&
        aiResult.length > 0 && (
          <>
            <div className="estimate-tabs">
              {aiResult.map((estimate, index) => (
                <button
                  key={index}
                  type="button"
                  className={`estimate-tab-btn ${
                    selectedEstimate === index ? "active" : ""
                  }`}
                  onClick={() => setSelectedEstimate(index)}
                >
                  추천 {index + 1}
                  <span className="tab-subtitle">{estimate.견적명}</span>
                </button>
              ))}
            </div>

            <div style={{ marginBottom: "40px" }}>
              {/* <h3 style={{ marginTop: "30px", marginBottom: "10px" }}>
                견적 {aiResult[selectedEstimate].견적번호}:{" "}
                {aiResult[selectedEstimate].견적명} (
                {aiResult[selectedEstimate].총예상가격})
              </h3> */}

              <div className="ai-result-list-container">
                <div className="product-list-header">
                  <div className="col-image"></div>
                  <div className="col-type">종류</div>
                  <div className="col-name">제품명</div>
                  <div className="col-price" style={{ textAlign: "right" }}>
                    가격
                  </div>
                  <div className="col-link">구매</div>
                </div>

                {aiResult[selectedEstimate].부품목록.map((item, index) => (
                  <div key={index} className="product-list-item">
                    <div className="col-image">
                      <img
                        src={
                          item["제품이미지"] ||
                          productImages[item["제품명"]] ||
                          getDefaultImage(item["제품종류"])
                        }
                        alt={item["제품명"]}
                        onError={(e) => {
                          e.target.src = getDefaultImage(item["제품종류"]);
                        }}
                      />
                    </div>

                    <div className="col-type">{item["제품종류"]}</div>
                    <div className="col-name">{item["제품명"]}</div>
                    <div className="col-price">{item["가격"]}</div>

                    <div className="col-link">
                      <a
                        href={item["판매사이트링크"]}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        바로가기
                      </a>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
    </div>
  );
};

export default AiConsult;
