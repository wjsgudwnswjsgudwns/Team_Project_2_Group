import React, { useState } from "react";
import { useCart } from "../context/CartContext";
import api from "../../../api/axiosConfig";
import { useNavigate } from "react-router-dom";

function CartPanel() {
  const { cart, removeFromCart, totalPrice } = useCart();

  const [compatResults, setCompatResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const categoryOrder = [
    "CPU",
    "COOLER",
    "MAINBOARD",
    "MEMORY",
    "GPU",
    "STORAGE",
    "CASE",
    "POWER",
  ];

  const handleCheckCompatibility = async () => {
    setLoading(true);
    setCompatResults([]);

    try {
      const res = await api.post("/api/ai/checkAll", {
        cpu: cart.CPU?.productName || "",
        mainboard: cart.MAINBOARD?.productName || "",
        memory: cart.MEMORY?.productName || "",
        gpu: cart.GPU?.productName || "",
        case: cart.CASE?.productName || "",
        power: cart.POWER?.productName || "",
      });

      navigate("/compatibility-result", { state: { results: res.data } });
    } catch (err) {
      console.error("호환성 검사 실패:", err);
      alert("호환성 검사 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="cart-panel">
      <h3>견적 리스트</h3>

      {categoryOrder.map((cat) => {
        const item = cart[cat];
        return (
          <div key={cat} className="cart-item">
            <div className="cart-item-header">
              <span className="cart-category">{cat}</span>
            </div>

            {item ? (
              <div className="cart-item-content">
                <div className="cart-name">{item.productName}</div>
                <div className="cart-bottom">
                  <span className="cart-quantity">수량: {item.quantity}</span>
                  <span className="cart-price">
                    {parseInt(item.price).toLocaleString()}원
                  </span>
                  <button
                    className="cart-remove"
                    onClick={() => removeFromCart(cat)}
                  >
                    ✕
                  </button>
                </div>
              </div>
            ) : (
              <div className="empty-slot">제품이 없습니다.</div>
            )}
          </div>
        );
      })}
      <button
        className="compat-check-btn"
        onClick={handleCheckCompatibility}
        disabled={loading}
      >
        {loading ? "검사 중..." : "호환성 검사"}
      </button>
      <div className="cart-total">총합: {totalPrice.toLocaleString()}원</div>
    </div>
  );
}

export default CartPanel;
