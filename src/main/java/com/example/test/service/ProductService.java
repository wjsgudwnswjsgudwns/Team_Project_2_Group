package com.example.test.service;

import com.example.test.dto.PageResponseDto;
import com.example.test.dto.ProductCreateRequestDto;
import com.example.test.dto.ProductDetailResponseDto;
import com.example.test.entity.Product;
import com.example.test.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            product.setImageUrl(request.getImageUrl());

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
            response.setImageUrl(product.getImageUrl());

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


    // 상품 삭제
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // 제품 이름으로 검색
    public PageResponseDto<ProductDetailResponseDto> searchByName (String name, int page, int size, String sortBy) {
        if(name == null || name.trim().isEmpty()) {
            return new PageResponseDto<>(List.of(), page, size, 0, 0, true, true, true);
        }

        // 최근 글로 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 페이징 시킴
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);

        // JSON -> Map 변환
        List<ProductDetailResponseDto> content = productPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.isEmpty()
        );

    }

    // 전체 상품
    public PageResponseDto<ProductDetailResponseDto> getAllProductsWithPaging(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductDetailResponseDto> content = productPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.isEmpty()
        );
    }
}