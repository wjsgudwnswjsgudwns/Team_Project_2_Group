package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memory_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MemorySpec {

    // 1. PRIMARY KEY 및 Product 관계 설정

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;


    // 2. 기본 정보

    private String manufacturer;        // 제조회사
    private String registeredMonth;     // 등록년월 (예: 2023년 05월)
    private String usageType;           // 사용 장치 (예: 데스크탑용)
    private String productCategory;     // 제품 분류 (예: DDR4)
    private String memoryType;          // 메모리 규격 (예: DIMM(UDIMM))
    private String memoryCapacity;      // 메모리 용량 (예: 8GB)


    // 3. 성능 및 기술 사양

    private String clockSpeedDetail;    // 동작클럭(대역폭) (예: 3200MHz(PC4-25600))
    private String ramTiming;           // 램 타이밍 (예: CL22)
    private String operatingVoltage;    // 동작 전압 (예: 1.20V)
    private Integer ramCount;           // 램 개수 (예: 1개)


    // 4. 외관 및 부가 기능
    private Boolean hasHeatsink;        // 히트싱크 포함 유무 (예: 미포함)

}
