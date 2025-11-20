import React, { useEffect, useState } from "react";
import axios from "axios";

export default function ProductList({ category, onAddToCart }) {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/products")
      .then((res) => {
        const filtered = res.data.filter((p) => p.category === category);
        setProducts(filtered);
      })
      .catch((err) => console.error(err));
  }, [category]);

  const handleAdd = async (productId) => {
    try {
      const res = await axios.post("http://localhost:8080/api/cart", {
        productId,
        quantity: 1,
      });
      onAddToCart(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="product-list">
      {products.map((p) => (
        <div key={p.id} className="product-card">
          <img src={p.imageUrl} alt={p.name} width="120" />
          <div>{p.name}</div>
          <div>{p.price} 원</div>
          <button onClick={() => handleAdd(p.id)}>담기</button>
        </div>
      ))}
    </div>
  );
}
