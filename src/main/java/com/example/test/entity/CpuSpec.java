package com.example.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cpu_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CpuSpec {

    // 1. PRIMARY KEY 및 Product 관계 설정

    @Id
    @Column(name = "product_id") // Product 테이블의 PK를 외래키 겸 기본 키로 사용
    private Long id;

    @OneToOne
    @MapsId // Product의 PK를 이 Entity의 PK로 매핑
    @JoinColumn(name = "product_id")
    private Product product;


    // 2. CPU 기본 정보 (Product와 중복되는 필드는 Product에만 정의하는 것이 원칙이나,
    //    편의를 위해 스펙 테이블에 일부 추가할 수 있음. 여기서는 스펙 관련 필드만 포함)

    private String manufacturer;        // 제조회사
    private String releaseDate;         // 등록년월 (혹은 출시일)
    private String cpuType;             // AMD CPU종류 (예: 라이젠 5-6세대)
    private String socketType;          // 소켓 구분 (예: AM5, LGA1700)
    private String generation;          // 세대 구분 (예: 6세대 / Zen5)
    private String processNode;         // 제조 공정 (예: TSMC 4nm)


    // 3. 사양 (Specs)

    private Integer cores;              // 코어 수
    private Integer threads;            // 스레드 수
    private Double baseClock;           // 기본 클럭 (GHz, 소수점 값)
    private Double maxClock;            // 최대 클럭 (GHz, 소수점 값)
    private String l2Cache;             // L2 캐시 (예: 6MB)
    private String l3Cache;             // L3 캐시 (예: 32MB)
    private String instructionSet;      // 연산 체계 (예: 64비트)
    private Integer tdp;                // TDP (W)
    private Integer ppt;                // PPT (W)
    private String pcieVersion;         // PCIe 버전 (예: PCIe5.0)
    private Integer maxPcieLanes;       // 최대 PCIe 레인수


    // 4. 메모리 사양 (Memory Specs)

    private String memoryType;          // 메모리 규격 (예: DDR5)
    private String maxMemorySize;       // 최대 메모리 크기 (예: 192GB)
    private Integer memoryClock;        // 메모리 클럭 (MHz)
    private Integer memoryChannel;      // 메모리 채널 (예: 2)


    // 5. 그래픽 사양 (Graphics Specs)

    private String integratedGraphics;  // 내장그래픽 유무 (예: O/X)
    private String gpuModelName;        // GPU 모델명 (예: AMD 라데온 그래픽)
    private Integer gpuCoreSpeed;       // GPU 코어 속도 (MHz)


    // 6. 기술 지원 (Technology Support)

    private Boolean smtSupport;         // SMT(하이퍼스레딩) 지원 여부 (True/False)


    // 7. 구성 (Configuration)

    private String packageType;         // 패키지 형태 (예: 멀티팩/정품)
    private String coolerIncluded;      // 쿨러 (예: Wraith Stealth 포함)


    // 8. 벤치마크 (Benchmark)

    private Integer benchmarkSingleR23; // 시네벤치R23 (싱글) 점수
    private Integer benchmarkMultiR23;  // 시네벤치R23 (멀티) 점수
}