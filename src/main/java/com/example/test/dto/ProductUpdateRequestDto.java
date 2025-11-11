package com.example.test.dto;

import com.example.test.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequestDto {

    private String name;           // 상품명
    private String manufacturer;   // 제조사
    private String price;          // 가격
    private ProductCategory category;  // 카테고리 (CPU, MEMORY, GPU 등)
    private List<SpecItem> specs;  // 스펙 정보
    private String imageUrl;       // s3 이미지 URL

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecItem {
        private String key;    // 필드명 (예: "제조회사")
        private String value;  // 값 (예: "Intel")
    }
}