package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "power_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PowerSpec {

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
    private String registeredMonth;     // 등록년월 (예: 2021년 09월)
    private String productCategory;     // 제품 분류 (예: ATX 파워)
    private String ratedOutput;         // 정격 출력 (예: 650W)
    private String certification80Plus; // 80 PLUS 인증 (예: 80 PLUS 브론즈)
    private String cableConnection;     // 케이블 연결 (예: 케이블일체형)
    private String psuDepth;            // 깊이 (예: 140mm)
    private String warrantyPeriod;      // A/S 보증기간 (예: 무상 5년)


    // 3. +12V 출력 방식 및 효율

    private String v12OutputMethod;     // +12V 출력방식 (예: +12V 싱글레일)
    private Integer v12Availability;    // +12V 가용율 (예: 99%)


    // 4. 쿨링팬

    private String fanSize;             // 쿨링팬 크기 (예: 120mm 팬)
    private String fanCount;            // 쿨링팬 개수 (예: 1개(팬))


    // 5. DC 출력 (전류 A)

    private Double v3_3Output;          // +3.3V 출력 (A)
    private Double v5Output;            // +5V 출력 (A)
    private Double v12Output;           // +12V 출력 (A)
    private Double v12NegativeOutput;   // -12V 출력 (A)
    private Double v5SbOutput;          // +5Vsb 출력 (A)


    // 6. 커넥터

    private String mainPowerConnector;  // 메인전원 (예: 24핀(20+4))
    private String auxiliaryPower;      // 보조전원 (예: (4+4핀)x1)
    private Integer pcie8PinCount;      // PCIe 8핀(6+2) 개수
    private Integer sataConnectorCount; // SATA 커넥터 개수
    private Integer ide4PinCount;       // IDE 4핀 개수
    private Integer fddConnectorCount;  // FDD 커넥터 개수


    // 7. 부가 기능 및 내부 설계 (O/X 항목은 Boolean)

    private Boolean supportPreFlight;   // 프리볼트 지원 유무
    private Boolean dcToDcDesign;       // DC to DC 설계 유무


    // 8. 보호 회로 (O/X 항목은 Boolean)

    private Boolean protectionOVP;      // 과전압 보호 (OVP) 지원 유무
    private Boolean protectionUVP;      // 저전압 보호 (UVP) 지원 유무
    private Boolean protectionOCP;      // 과전류 보호 (OCP) 지원 유무
    private Boolean protectionOPP;      // 과전력 보호 (OPP) 지원 유무
    private Boolean protectionOTP;      // 과온 보호 (OTP) 지원 유무
    private Boolean protectionSCP;      // 단락(합선) 보호 (SCP) 지원 유무

}