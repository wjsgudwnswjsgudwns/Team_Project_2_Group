package com.example.test.service;

import com.example.test.dto.ProductCreateRequestDto;
import com.example.test.dto.ProductDetailResponseDto;
import com.example.test.entity.Product;
import com.example.test.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 상품 등록
    @Transactional
    public ProductDetailResponseDto createProduct(ProductCreateRequestDto request) {
        try {
            // 1. Product 엔티티 생성
            Product product = new Product();
            product.setName(request.getName());
            product.setManufacturer(request.getManufacturer());
            product.setPrice(request.getPrice());
            product.setCategory(request.getCategory());

            // 2. specs Map을 JSON 문자열로 변환하여 저장
            String specsJson = objectMapper.writeValueAsString(request.getSpecs());
            product.setSpecs(specsJson);

            // 3. DB에 저장
            Product savedProduct = productRepository.save(product);

            // 4. Response DTO로 변환하여 반환
            return convertToResponseDto(savedProduct);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("상품 등록 중 JSON 변환 오류 발생", e);
        }
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailResponseDto getProduct(Long id) {
        // 1. DB에서 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + id));

        // 2. Response DTO로 변환하여 반환
        return convertToResponseDto(product);
    }

    // Product 엔티티를 ProductDetailResponseDto로 변환
    private ProductDetailResponseDto convertToResponseDto(Product product) {
        try {
            ProductDetailResponseDto response = new ProductDetailResponseDto();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setManufacturer(product.getManufacturer());
            response.setPrice(product.getPrice());
            response.setCategory(product.getCategory());

            // JSON 문자열을 Map으로 변환
            Map<String, Object> specs = objectMapper.readValue(
                    product.getSpecs(),
                    new TypeReference<Map<String, Object>>() {}
            );
            response.setSpecs(specs);

            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("상품 조회 중 JSON 변환 오류 발생", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ProductDetailResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
}