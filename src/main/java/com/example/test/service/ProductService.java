package com.example.test.service;

import com.example.test.entity.*;
import com.example.test.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CpuSpecRepository cpuSpecRepository;

    @Autowired
    private CoolerSpecRepository coolerSpecRepository;

    @Autowired
    private MainboardSpecRepository mainboardSpecRepository;

    @Autowired
    private MemorySpecRepository memorySpecRepository;

    @Autowired
    private GpuSpecRepository gpuSpecRepository;

    @Autowired
    private StorageSpecRepository storageSpecRepository;

    @Autowired
    private CaseSpecRepository caseSpecRepository;

    @Autowired
    private PowerSpecRepository powerSpecRepository;


    // 1. 전체 상품 목록 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    // 2. 카테고리별 상품 목록 조회
    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }


    // 3. 상품 ID로 기본 정보 조회
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }


    // 4. CPU 상세 스펙 조회
    public Optional<CpuSpec> getCpuSpec(Long productId) {
        return cpuSpecRepository.findById(productId);
    }


    // 5. 쿨러 상세 스펙 조회
    public Optional<CoolerSpec> getCoolerSpec(Long productId) {
        return coolerSpecRepository.findById(productId);
    }


    // 6. 메인보드 상세 스펙 조회
    public Optional<MainboardSpec> getMainboardSpec(Long productId) {
        return mainboardSpecRepository.findById(productId);
    }


    // 7. 메모리 상세 스펙 조회
    public Optional<MemorySpec> getMemorySpec(Long productId) {
        return memorySpecRepository.findById(productId);
    }


    // 8. GPU 상세 스펙 조회
    public Optional<GpuSpec> getGpuSpec(Long productId) {
        return gpuSpecRepository.findById(productId);
    }


    // 9. 저장장치 상세 스펙 조회
    public Optional<StorageSpec> getStorageSpec(Long productId) {
        return storageSpecRepository.findById(productId);
    }


    // 10. 케이스 상세 스펙 조회
    public Optional<CaseSpec> getCaseSpec(Long productId) {
        return caseSpecRepository.findById(productId);
    }


    // 11. 파워 상세 스펙 조회
    public Optional<PowerSpec> getPowerSpec(Long productId) {
        return powerSpecRepository.findById(productId);
    }

}
