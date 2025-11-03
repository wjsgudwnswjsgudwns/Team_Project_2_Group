package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "case_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CaseSpec {

    // 1. PRIMARY KEY 및 Product 관계 설정

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;


    // 2. 기본 정보

    private String manufacturer;        // 제조회사 (예: DAVEN)
    private String registeredMonth;     // 등록년월 (예: 2022년 11월)
    private String productType;         // 제품 분류 (예: ATX 케이스)
    private String caseSize;            // 케이스 크기 (예: 미들타워)
    private String standardPsuType;     // 표준 파워 규격 (예: 표준-ATX)


    // 3. 크기 (Size)

    private Integer width;              // 너비(W) (mm)
    private Integer depth;              // 깊이(D) (mm)
    private Integer height;             // 높이(H) (mm)


    // 4. 호환성 (Compatibility)

    // 메인보드 지원 규격 ('O' / 'X')
    private Boolean supportATX;         // ATX 지원
    private Boolean supportMATX;        // M-ATX 지원
    private Boolean supportMITX;        // M-ITX 지원

    private Integer maxVgaLength;       // VGA 길이 (mm)
    private Integer maxCpuCoolerHeight; // CPU 쿨러 높이 (mm)
    private String powerMountLength;    // 파워 장착 길이 (예: 170~250mm)
    private String powerLocation;       // 파워 위치 (예: 하단후면)
    private Boolean psuIncluded;        // 파워 포함여부 (예: 파워미포함)

    // 수랭 쿨러 규격 지원
    private String liquidCoolerSupport; // 수랭쿨러 규격 (예: 최대 2열 지원)
    private String radiatorTop;         // 라디에이터(상단) 지원 크기 (예: 최대 280mm, 240mm)
    private String radiatorFront;       // 라디에이터(전면) 지원 크기 (예: 최대 360mm, 280mm)
    private String radiatorRear;        // 라디에이터(후면) 지원 크기 (예: 최대 120mm)


    // 5. 내부 확장

    private Integer driveBay35Count;    // 8.9cm 베이(3.5인치) 개수
    private Integer driveBay25Count;    // 6.4cm 베이(2.5인치) 개수
    private Integer maxStorageCount;    // 저장장치 장착 최대 개수
    private Integer pciSlotCount;       // PCI 슬롯(수평) 개수 (예: 7개)


    // 6. 패널

    private String frontPanelType;      // 전면 패널 타입 (예: 메쉬)
    private String sidePanelType;       // 측면 패널 타입 (예: 강화유리)
    private String dustFilter;          // 먼지필터 (예: 부분)


    // 7. 쿨링/튜닝

    private String totalFanCount;       // 쿨링팬 총 개수 (예: 총 6개)
    private String ledFanCount;         // LED팬 개수 (예: 4개)
    private String fanRearSpec;         // 후면 팬 스펙 (예: 120mm LED x1)
    private String fanFrontSpec;        // 전면 팬 스펙 (예: 120mm LED x3)
    private String fanTopSpec;          // 상단 팬 스펙 (예: 120mm x2)

    private Boolean rgbController;      // RGB 컨트롤 지원 유무 ('O' / 'X')


    // 8. 외부 포트

    private Boolean portUsb20;          // 외부 포트 USB 2.0 지원 유무 ('O' / 'X')
    private Boolean portUsb3x5Gbps;     // 외부 포트 USB3.x 5Gbps 지원 유무 ('O' / 'X')
}