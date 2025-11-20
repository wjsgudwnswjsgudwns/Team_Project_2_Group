import React, { useEffect, useState } from "react";
import { useCart } from "./context/CartContext";
import api from "../../api/axiosConfig";
import CartPanel from "./cartcomponents/CartPanel";
import ComputerSidebar from "./ComputerSidebar";
import { Link, useNavigate } from "react-router-dom";
import "./Cpu.css";

function Power({ role }) {
  const [products, setProducts] = useState([]);
  const [searchName, setSearchName] = useState(""); // 검색어
  const [currentPage, setCurrentPage] = useState(0); // 현재 페이지
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수
  const [totalElements, setTotalElements] = useState(0); // 전체 상품 수
  const [pageSize] = useState(10); // 페이지당 상품 수
  const [loading, setLoading] = useState(false);

  const { addToCart } = useCart();
  const navigate = useNavigate();

  // 상품 목록 불러오기
  useEffect(() => {
    loadProducts();
  }, [currentPage, searchName]);

  const loadProducts = async () => {
    try {
      setLoading(true);

      // 카테고리별 API 호출로 변경
      const url = searchName.trim()
        ? `/api/products/category/POWER/search?name=${searchName}&page=${currentPage}&size=${pageSize}&sortBy=id`
        : `/api/products/category/POWER?page=${currentPage}&size=${pageSize}&sortBy=id`;

      const response = await api.get(url);

      // 필터링 불필요 - 이미 백엔드에서 CPU만 반환함
      setProducts(response.data?.content || []);
      setTotalPages(response.data?.totalPages || 0);
      setTotalElements(response.data?.totalElements || 0);
    } catch (error) {
      console.error("상품 로드 실패:", error);
      setProducts([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  // 검색 버튼 클릭
  const handleSearch = () => {
    setCurrentPage(0); // 검색 시 첫 페이지로
    loadProducts();
  };

  // 검색어 입력 시 Enter 키 처리
  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  // 페이지 변경
  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  // 상세보기로 이동
  const handleRowClick = (productId) => {
    navigate(`/power/${productId}`);
  };

  return (
    <div>
      <div className="page-container">
        <ComputerSidebar />
        <div className="product-list">
          <div className="category-title">파워</div>

          {/* 검색창 */}
          <div className="search-container">
            <input
              type="text"
              placeholder="상품명으로 검색"
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
              onKeyPress={handleKeyPress}
              className="search-input"
            />
            <button onClick={handleSearch} className="search-btn">
              검색
            </button>
          </div>

          {/* 로딩 상태 */}
          {loading && <div className="loading-message">로딩 중...</div>}

          {/* 상품 목록 */}
          {!loading && products.length === 0 ? (
            <div className="no-products-message">등록된 상품이 없습니다.</div>
          ) : (
            products.map((p) => (
              <div key={p.id} className="product-card">
                <img
                  src={p.imageUrl || "/placeholder-image.png"}
                  alt={p.name}
                  width="120"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = "/placeholder-image.png";
                  }}
                />
                <h4 onClick={() => handleRowClick(p.id)}>{p.name}</h4>
                <div className="product-price-container">
                  <p>{Number(p.price).toLocaleString()}원</p>
                  <button onClick={() => addToCart(p.id, "POWER")}>담기</button>
                </div>
              </div>
            ))
          )}

          {/* 페이지네이션 */}
          {totalPages > 0 && (
            <div className="pagination">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="page-btn"
              >
                &lt;
              </button>

              <span className="page-info">
                {currentPage + 1} / {totalPages} 페이지
              </span>

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="page-btn"
              >
                &gt;
              </button>
            </div>
          )}

          {/* 글쓰기 버튼 (관리자만) */}
          {role === "ROLE_ADMIN" && (
            <div className="write-btn-container">
              <Link to="/input" className="write-btn">
                글쓰기
              </Link>
            </div>
          )}
        </div>

        <CartPanel />
      </div>
    </div>
  );
}

export default Power;
