import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import ComputerSidebar from "../ComputerSidebar";
import api from "../../../api/axiosConfig";
import "./CpuView.css";

function HardCaseView({ role }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProductDetail();
  }, [id]);

  const fetchProductDetail = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/api/products/${id}`);
      console.log("API 응답:", response.data); // 디버깅용
      setProduct(response.data);
      setError(null);
    } catch (err) {
      console.error("상품 조회 실패:", err);
      setError("상품 정보를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    try {
      await api.delete(`/api/products/${id}`);
      alert("삭제되었습니다.");
      navigate("/case");
    } catch (err) {
      console.error("삭제 실패:", err);
      alert("삭제에 실패했습니다.");
    }
  };

  const goBack = () => {
    navigate("/case");
  };

  if (loading) {
    return (
      <div className="cpu-page-container">
        <ComputerSidebar />
        <div className="cpu-view-content">
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>로딩 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="cpu-page-container">
        <ComputerSidebar />
        <div className="cpu-view-content">
          <div className="error-container">
            <p className="error-message">
              {error || "상품을 찾을 수 없습니다."}
            </p>
            <button onClick={goBack} className="back-btn">
              목록으로
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cpu-page-container">
      <ComputerSidebar />
      <div className="cpu-view-content">
        {/* 상품 상세 컨테이너 */}
        <div className="product-detail-container">
          {/* 상단: 이미지 + 기본 정보 */}
          <div className="product-overview">
            {/* 이미지 영역 */}
            <div className="product-image-section">
              {product.imageUrl ? (
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="product-image"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = "/placeholder-image.png"; // 대체 이미지
                  }}
                />
              ) : (
                <div className="product-image-placeholder">
                  <span>이미지 없음</span>
                </div>
              )}
            </div>

            {/* 기본 정보 영역 */}
            <div className="product-info-section">
              <h1 className="product-name">{product.name}</h1>

              <div className="product-basic-info">
                <div className="info-row">
                  <span className="info-label">제조사</span>
                  <span className="info-value">
                    {product.manufacturer || "-"}
                  </span>
                </div>
                <div className="info-row">
                  <span className="info-label">가격</span>
                  <span className="info-value price">
                    {parseInt(product.price).toLocaleString()}원
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* 상세 스펙 섹션 */}
          <div className="product-specs-section">
            <h2 className="section-title">상세 스펙</h2>
            {product.specs && product.specs.length > 0 ? (
              <div className="specs-grid">
                {product.specs.map((spec, index) => (
                  <div key={index} className="spec-item">
                    <div className="spec-label">{spec.key}</div>
                    <div className="spec-value">{spec.value}</div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="no-specs">등록된 상세 스펙이 없습니다.</p>
            )}
          </div>

          {/* 하단 액션 버튼 */}
          <div className="product-actions">
            <button onClick={goBack} className="action-btn btn-list">
              목록
            </button>
            {role === "ROLE_ADMIN" && (
              <>
                <Link to={`/edit/${id}`} className="action-btn btn-edit">
                  수정
                </Link>
                <button
                  onClick={handleDelete}
                  className="action-btn btn-delete"
                >
                  삭제
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default HardCaseView;
