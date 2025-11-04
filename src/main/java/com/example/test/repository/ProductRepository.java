package com.example.test.repository;

import com.example.test.entity.Product;
import com.example.test.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 카테고리별 조회만 추가 (상품 목록에서 필요)
    List<Product> findByCategory(ProductCategory category);
}