package com.example.test.controller;

import com.example.test.dto.PageResponseDto;
import com.example.test.dto.ProductCreateRequestDto;
import com.example.test.dto.ProductDetailResponseDto;
import com.example.test.dto.ProductUpdateRequestDto;
import com.example.test.entity.ProductCategory;
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

        System.out.println(response);
        return ResponseEntity.ok(response);
    }

//    @GetMapping
//    public ResponseEntity<List<ProductDetailResponseDto>> getAllProducts() {
//        List<ProductDetailResponseDto> products = productService.getAllProducts();
//        return ResponseEntity.ok(products);
//    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProdect(@PathVariable Long id) {
        productService.deleteProduct(id);

        return ResponseEntity.ok(null);
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequestDto request) {

        ProductDetailResponseDto response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    // 제품 이름으로 검색
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<ProductDetailResponseDto>> searchProducts(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {

        PageResponseDto<ProductDetailResponseDto> response = productService.searchByName(name, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    // 전체 상품 페이지
    @GetMapping("/paging")
    public ResponseEntity<PageResponseDto<ProductDetailResponseDto>> getAllProductsWithPaging(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {

        PageResponseDto<ProductDetailResponseDto> response = productService.getAllProductsWithPaging(page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    // 카테고리별 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponseDto<ProductDetailResponseDto>> getProductsByCategory(
            @PathVariable ProductCategory category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {

        PageResponseDto<ProductDetailResponseDto> response =
                productService.getProductsByCategory(category, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    // 카테고리별 검색
    @GetMapping("/category/{category}/search")
    public ResponseEntity<PageResponseDto<ProductDetailResponseDto>> searchByCategoryAndName(
            @PathVariable ProductCategory category,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {

        PageResponseDto<ProductDetailResponseDto> response =
                productService.searchByCategoryAndName(category, name, page, size, sortBy);
        return ResponseEntity.ok(response);
    }


}