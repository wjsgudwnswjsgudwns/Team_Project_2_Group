package com.example.test.service;

import com.example.test.dto.CartDto;
import com.example.test.entity.Cart;
import com.example.test.entity.Product;
import com.example.test.entity.User;
import com.example.test.repository.CartRepository;
import com.example.test.repository.ProductRepository;
import com.example.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 장바구니에 상품 추가
    @Transactional
    public CartDto addToCart(String username, Long productId, Integer quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 이미 장바구니에 있는 상품인지 확인
        Cart cart = cartRepository.findByUserAndProductId(user, productId)
                .orElse(new Cart());

        // 새로운 항목 추가
        if (cart.getId() == null) {
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(quantity);
        } else {
            // 기존에 있는 항목이라면 수량 증가
            cart.setQuantity(cart.getQuantity() + quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToDto(savedCart);
    }

    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public List<CartDto> getCartItems(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 장바구니 수량 변경
    @Transactional
    public CartDto updateQuantity(String username, Long cartId, Integer quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }

        cart.setQuantity(quantity);
        Cart updatedCart = cartRepository.save(cart);
        return convertToDto(updatedCart);
    }

    // 장바구니 항목 삭제
    @Transactional
    public void removeFromCart(String username, Long cartId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }

        cartRepository.delete(cart);
    }

    // Entity -> DTO 변환
    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setProductId(cart.getProduct().getId());
        dto.setProductName(cart.getProduct().getName());
        dto.setProductImage(cart.getProduct().getImageUrl());
        dto.setPrice(cart.getProduct().getPrice());
        dto.setQuantity(cart.getQuantity());
        dto.setManufacturer(cart.getProduct().getManufacturer());
        dto.setCategory(cart.getProduct().getCategory().toString());
        return dto;
    }
}