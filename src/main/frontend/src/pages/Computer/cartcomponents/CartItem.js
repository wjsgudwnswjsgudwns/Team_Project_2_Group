function CartItem({ item, onRemove }) {
  return (
    <div className="cart-item">
      <img src={item.productImage} alt={item.productName} width="50" />
      <div>{item.productName}</div>
      <div>
        {item.price} x {item.quantity}
      </div>
      <button onClick={() => onRemove(item.id)}>삭제</button>
    </div>
  );
}

export default CartItem;
