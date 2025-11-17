package com.example.test.repository;

import com.example.test.entity.Product;
import com.example.test.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 카테고리별 조회만 추가 (상품 목록에서 필요)
    List<Product> findByCategory(ProductCategory category);

    // 이름으로 찾기
    Page<Product> findByName(String name, Pageable pageable);

    // 이름 부분 검색 (대소문자 검색)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 카테고리별 페이징 조회 추가
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    // 카테고리별 이름 검색 추가
    Page<Product> findByCategoryAndNameContainingIgnoreCase(
            ProductCategory category,
            String name,
            Pageable pageable
    );
}