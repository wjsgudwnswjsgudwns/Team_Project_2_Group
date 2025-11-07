package com.example.test.controller;

import com.example.test.dto.CartDto;
import com.example.test.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.getOrDefault("quantity", 1).toString());

        CartDto cartDto = cartService.addToCart(username, productId, quantity);
        return ResponseEntity.ok(cartDto);
    }

    // 장바구니 목록 조회
    @GetMapping
    public ResponseEntity<?> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CartDto> cartItems = cartService.getCartItems(username);
        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 수량 변경
    @PutMapping("/{cartId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartId,
            @RequestBody Map<String, Integer> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer quantity = request.get("quantity");

        CartDto cartDto = cartService.updateQuantity(username, cartId, quantity);
        return ResponseEntity.ok(cartDto);
    }

    // 장바구니 항목 삭제
    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.removeFromCart(username, cartId);
        return ResponseEntity.ok().build();
    }
}