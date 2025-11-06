package com.example.test.controller;

import com.example.test.dto.ProductCreateRequestDto;
import com.example.test.dto.ProductDetailResponseDto;
import com.example.test.service.ProductService;
import com.example.test.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private S3Service s3Service;

    // 상품 등록
    @PostMapping
    public ResponseEntity<ProductDetailResponseDto> createProduct(
            @RequestBody ProductCreateRequestDto request) {

        ProductDetailResponseDto response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    // s3 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile file) {
        try {
            // S3에 이미지 업로드하고 URL 받기
            String imageUrl = s3Service.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "이미지 업로드 성공");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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