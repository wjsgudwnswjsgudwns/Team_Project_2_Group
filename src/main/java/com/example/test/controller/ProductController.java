package com.example.test.controller;

import com.example.test.dto.ProductCreateRequestDto;
import com.example.test.dto.ProductDetailResponseDto;
import com.example.test.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    // 상품 등록
    @PostMapping
    public ResponseEntity<ProductDetailResponseDto> createProduct(
            @RequestBody ProductCreateRequestDto request) {

        ProductDetailResponseDto response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    // 상품 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> getProduct(@PathVariable Long id) {

        ProductDetailResponseDto response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductDetailResponseDto>> getAllProducts() {
        List<ProductDetailResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}