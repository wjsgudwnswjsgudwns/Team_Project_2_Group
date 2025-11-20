import React, { useRef, useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ComputerSidebar from "./ComputerSidebar";
import "./Input.css";
import api from "../../api/axiosConfig";

function Edit() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState({
    name: "",
    manufacturer: "",
    price: "",
    category: "CPU",
  });

  const [specs, setSpecs] = useState([]);
  const [newFieldKey, setNewFieldKey] = useState("");
  const [newFieldValue, setNewFieldValue] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // 이미지 관련
  const [imagePreview, setImagePreview] = useState(null);
  const [imageUrl, setImageUrl] = useState("");

  const keyInputRef = useRef(null);

  // 기존 상품 데이터 불러오기
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await api.get(`/api/products/${id}`);
        const data = response.data;

        setProduct({
          name: data.name,
          manufacturer: data.manufacturer || "",
          price: data.price,
          category: data.category,
        });

        setSpecs(data.specs || []);
        setImageUrl(data.imageUrl || "");
        setImagePreview(data.imageUrl || null);

        setIsLoading(false);
      } catch (error) {
        console.error("상품 조회 실패:", error);
        alert("상품을 불러오는데 실패했습니다.");
        navigate(-1);
      }
    };

    fetchProduct();
  }, [id, navigate]);

  const handleProductChange = (e) => {
    const { name, value } = e.target;
    setProduct((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSpecChange = (index, field, value) => {
    setSpecs((prev) => {
      const newSpecs = [...prev];
      newSpecs[index] = { ...newSpecs[index], [field]: value };
      return newSpecs;
    });
  };

  const addSpecField = () => {
    if (newFieldKey.trim() && newFieldValue.trim()) {
      setSpecs((prev) => [...prev, { key: newFieldKey, value: newFieldValue }]);
      setNewFieldKey("");
      setNewFieldValue("");
      keyInputRef.current?.focus();
    } else {
      alert("필드명과 값을 모두 입력해주세요.");
    }
  };

  const removeSpecField = (index) => {
    setSpecs((prev) => prev.filter((_, i) => i !== index));
  };

  const handleImageChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setImagePreview(URL.createObjectURL(file));

    const formData = new FormData();
    formData.append("image", file);

    try {
      const response = await api.post("api/products/upload-image", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      setImageUrl(response.data.imageUrl);
      alert("이미지가 업로드되었습니다.");
    } catch (error) {
      console.error("이미지 업로드 실패:", error);
      alert("이미지 업로드 실패. 다시 시도해주세요.");
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addSpecField();
    }
  };

  // 수정 제출
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!product.name || !product.price) {
      alert("상품명과 가격은 필수입니다.");
      return;
    }

    setIsSubmitting(true);

    const productData = {
      name: product.name,
      manufacturer: product.manufacturer,
      price: product.price,
      category: product.category,
      specs: specs,
      imageUrl: imageUrl,
    };

    console.log("수정할 데이터:", productData);

    try {
      const response = await api.patch(`/api/products/${id}`, productData);
      console.log("수정 성공:", response.data);
      alert(`상품이 성공적으로 수정되었습니다!\n상품명: ${response.data.name}`);

      // 수정 후 상세 페이지로 이동
      navigate(`/${product.category.toLowerCase()}/${id}`);
    } catch (error) {
      console.error("수정 실패:", error);
      alert("상품 수정 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="cpu-page-container">
        <ComputerSidebar />
        <div className="input-cpu-content">
          <p>상품 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="cpu-page-container">
      <ComputerSidebar />
      <div className="input-cpu-content">
        <h2>상품 수정 (관리자용)</h2>

        <form onSubmit={handleSubmit} className="cpu-spec-form">
          {/* 기본 상품 정보 */}
          <div className="spec-group">
            <h3>기본 정보</h3>
            <table className="spec-table">
              <tbody>
                <tr>
                  <td className="label-cell">상품명 *</td>
                  <td className="input-cell">
                    <input
                      type="text"
                      name="name"
                      value={product.name}
                      onChange={handleProductChange}
                      placeholder="예: 인텔 코어 i9-14900K"
                      className="spec-input"
                      required
                      disabled={isSubmitting}
                    />
                  </td>
                </tr>
                <tr>
                  <td className="label-cell">제조사</td>
                  <td className="input-cell">
                    <input
                      type="text"
                      name="manufacturer"
                      value={product.manufacturer}
                      onChange={handleProductChange}
                      placeholder="예: Intel, AMD"
                      className="spec-input"
                      disabled={isSubmitting}
                    />
                  </td>
                </tr>
                <tr>
                  <td className="label-cell">가격 (원) *</td>
                  <td className="input-cell">
                    <input
                      type="text"
                      name="price"
                      value={product.price}
                      onChange={handleProductChange}
                      placeholder="예: 750000"
                      className="spec-input"
                      required
                      disabled={isSubmitting}
                    />
                  </td>
                </tr>
                <tr>
                  <td className="label-cell">카테고리</td>
                  <td className="input-cell">
                    <select
                      name="category"
                      value={product.category}
                      onChange={handleProductChange}
                      className="spec-select"
                      disabled={isSubmitting}
                    >
                      <option value="CPU">CPU</option>
                      <option value="COOLER">쿨러</option>
                      <option value="MAINBOARD">메인보드</option>
                      <option value="MEMORY">메모리</option>
                      <option value="GPU">그래픽카드</option>
                      <option value="STORAGE">디스크</option>
                      <option value="CASE">케이스</option>
                      <option value="POWER">파워</option>
                    </select>
                  </td>
                </tr>

                {/* 이미지 업로드 섹션 */}
                <tr>
                  <td className="label-cell">상품 이미지</td>
                  <td className="input-cell">
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      disabled={isSubmitting}
                    />
                    {imagePreview && (
                      <div style={{ marginTop: "10px" }}>
                        <img
                          src={imagePreview}
                          alt="미리보기"
                          style={{
                            width: "180px",
                            height: "auto",
                            borderRadius: "6px",
                            border: "1px solid #eee",
                          }}
                        />
                      </div>
                    )}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* 상세 스펙 (동적) */}
          <div className="spec-group">
            <h3>상세 스펙</h3>

            {/* 기존 스펙 목록 */}
            {specs.length > 0 && (
              <table className="spec-table">
                <tbody>
                  {specs.map((spec, index) => (
                    <tr key={index}>
                      <td className="label-cell">{spec.key}</td>
                      <td className="input-cell">
                        <input
                          type="text"
                          value={spec.value}
                          onChange={(e) =>
                            handleSpecChange(index, "value", e.target.value)
                          }
                          className="spec-input"
                          disabled={isSubmitting}
                        />
                      </td>
                      <td className="action-cell">
                        <button
                          type="button"
                          onClick={() => removeSpecField(index)}
                          className="remove-btn"
                          disabled={isSubmitting}
                        >
                          삭제
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* 새 필드 추가 영역 */}
            <div className="add-field-section">
              <h4>새 스펙 필드 추가</h4>
              <div className="add-field-inputs">
                <input
                  type="text"
                  value={newFieldKey}
                  onChange={(e) => setNewFieldKey(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="필드명 (예: 코어수)"
                  className="field-key-input"
                  disabled={isSubmitting}
                  ref={keyInputRef}
                />
                <input
                  type="text"
                  value={newFieldValue}
                  onChange={(e) => setNewFieldValue(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="값 (예: 24)"
                  className="field-value-input"
                  disabled={isSubmitting}
                />
                <button
                  type="button"
                  onClick={addSpecField}
                  className="add-field-btn"
                  disabled={isSubmitting}
                >
                  + 추가
                </button>
              </div>
              <p className="hint-text">* Enter 키를 눌러도 추가됩니다</p>
            </div>
          </div>

          {/* 버튼 그룹 */}
          <div style={{ display: "flex", gap: "10px" }}>
            <button
              type="submit"
              className="submit-btn"
              disabled={isSubmitting}
              style={{ flex: 1 }}
            >
              {isSubmitting ? "수정 중..." : "수정 완료"}
            </button>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="submit-btn"
              disabled={isSubmitting}
              style={{
                flex: 1,
                backgroundColor: "#6c757d",
              }}
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Edit;
