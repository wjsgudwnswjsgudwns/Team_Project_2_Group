package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "storage_spec") // HDD/SSD 등 저장장치 통합 테이블
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class StorageSpec {

    // 1. PRIMARY KEY 및 Product 관계 설정

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;


    // 2. 기본 정보

    private String manufacturer;        // 제조회사 (예: MSI)
    private String registeredMonth;     // 등록년월 (예: 2023년 05월)
    private String productType;         // 제품 분류 (예: 내장형SSD)
    private String formFactor;          // 폼팩터 (예: M.2 (2280))
    private String interfaceType;       // 인터페이스 (예: PCIe4.0 x4 (64GT/s))
    private String protocol;            // 프로토콜 (예: NVMe 1.4)
    private String capacity;            // 용량 (예: 1TB)
    private String memoryType;          // 메모리 타입 (예: TLC)
    private String nandStructure;       // 낸드 구조 (예: 3D낸드)
    private String ramType;             // RAM 타입 (예: DDR4 1GB)
    private String ramIncluded;         // RAM 탑재 유무 (예: DRAM 탑재)
    private String controller;          // 컨트롤러 (예: E18)
    private Boolean warranty5Year;       // A/S 기간 5년 지원 유무 ('O' / 'X')
    private Boolean limitedWarranty;     // 제한보증 지원 유무 ('O' / 'X')


    // 3. 성능

    private String sequentialReadSpeed; // 순차 읽기 (예: 7,400MB/s)
    private String sequentialWriteSpeed;// 순차 쓰기 (예: 6,000MB/s)
    private String randomReadIOPS;      // 읽기 IOPS (예: 750K)
    private String randomWriteIOPS;     // 쓰기 IOPS (예: 1,000K)


    // 4. 기능 지원 (O/X 항목은 Boolean)

    private Boolean trimSupport;        // TRIM 지원 유무
    private Boolean gcSupport;          // GC(Garbage Collection) 지원 유무
    private Boolean slcCachingSupport;  // SLC캐싱 지원 유무
    private Boolean smartSupport;       // S.M.A.R.T 지원 유무
    private Boolean eccSupport;         // ECC 지원 유무


    // 5. 부가 기능 (O/X 항목은 Boolean)

    private Boolean aesEncryption;      // AES 암호화 지원 유무
    private Boolean dedicatedSoftware;  // 전용 S/W 지원 유무


    // 6. 소프트웨어 지원 (O/X 항목은 Boolean)

    private Boolean migrationSupport;   // 마이그레이션 지원 유무
    private Boolean managementSupport;  // 관리 기능 지원 유무


    // 7. 환경 특성 및 내구성

    private String mtbf;                // MTBF (예: 160만시간)
    private String tbw;                 // TBW (예: 700TB)
    private Boolean gameCompatibility;  // 게임 호환 (예: PS5 호환)


    // 8. 쿨링 및 크기/무게 (NVMe/M.2 특화)

    private Boolean nvmeHeatsinkIncluded; // NVMe 방열판 포함 유무 (예: 미포함)
    private String width;               // 가로 (mm)
    private String height;              // 높이 (mm)
    private String thickness;           // 두께 (mm)
}