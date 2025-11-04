package com.example.test.controller;

import com.example.test.entity.*;
import com.example.test.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. 전체 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }


    // 2. 카테고리별 상품 목록 조회
    // 예: GET /api/products/category/CPU
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable ProductCategory category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }


    // 3. 상품 기본 정보 조회
    // 예: GET /api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 4. CPU 스펙 조회
    // 예: GET /api/products/1/cpu-spec
    @GetMapping("/{id}/cpu-spec")
    public ResponseEntity<CpuSpec> getCpuSpec(@PathVariable Long id) {
        return productService.getCpuSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 5. 쿨러 스펙 조회
    @GetMapping("/{id}/cooler-spec")
    public ResponseEntity<CoolerSpec> getCoolerSpec(@PathVariable Long id) {
        return productService.getCoolerSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 6. 메인보드 스펙 조회
    @GetMapping("/{id}/mainboard-spec")
    public ResponseEntity<MainboardSpec> getMainboardSpec(@PathVariable Long id) {
        return productService.getMainboardSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 7. 메모리 스펙 조회
    @GetMapping("/{id}/memory-spec")
    public ResponseEntity<MemorySpec> getMemorySpec(@PathVariable Long id) {
        return productService.getMemorySpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 8. GPU 스펙 조회
    @GetMapping("/{id}/gpu-spec")
    public ResponseEntity<GpuSpec> getGpuSpec(@PathVariable Long id) {
        return productService.getGpuSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 9. 저장장치 스펙 조회
    @GetMapping("/{id}/storage-spec")
    public ResponseEntity<StorageSpec> getStorageSpec(@PathVariable Long id) {
        return productService.getStorageSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 10. 케이스 스펙 조회
    @GetMapping("/{id}/case-spec")
    public ResponseEntity<CaseSpec> getCaseSpec(@PathVariable Long id) {
        return productService.getCaseSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 11. 파워 스펙 조회
    @GetMapping("/{id}/power-spec")
    public ResponseEntity<PowerSpec> getPowerSpec(@PathVariable Long id) {
        return productService.getPowerSpec(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
