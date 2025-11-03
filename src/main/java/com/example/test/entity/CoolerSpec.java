package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cooler_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CoolerSpec {

    // 1. PRIMARY KEY 및 Product 관계 설정

    @Id
    @Column(name = "product_id") // Product 테이블의 PK를 외래키 겸 기본 키로 사용
    private Long id;

    @OneToOne
    @MapsId // Product의 PK를 이 Entity의 PK로 매핑
    @JoinColumn(name = "product_id")
    private Product product;


    // 2. 기본 정보

    private String manufacturer;        // 제조회사
    private String registeredMonth;     // 등록년월 (예: 2025년 02월)
    private String coolingType;         // 냉각 방식 (예: 수랭, 공랭)
    private String warrantyPeriod;      // A/S 기간 (예: 5년+누수보상)


    // 3. 재질 정보

    private String baseBlockMaterial;   // 베이스/워터블록 재질 (예: 구리)
    private String radiatorMaterial;    // 방열판/라디에이터 재질 (예: 알루미늄)


    // 4. 소켓 지원 정보 (O/X로 표시되므로 Boolean 또는 String 사용)

    private Boolean supportLGA1851;     // 인텔 소켓 LGA1851 지원
    private Boolean supportLGA1700;     // 인텔 소켓 LGA1700 지원
    private Boolean supportAM5;         // AMD 소켓 AM5 지원
    private Boolean supportAM4;         // AMD 소켓 AM4 지원

    // 5. 펌프 및 라디에이터 정보 (수랭 기준)

    private String pumpNoise;           // 펌프 소음 (예: 20dBA)
    private Integer radiatorLength;     // 라디에이터 길이 (mm)
    private String radiatorType;        // 라디에이터 (예: 2열, 3열)
    private Integer radiatorThickness;  // 라디에이터 두께 (mm)


    // 6. 쿨링팬 정보

    private Integer fanSize;            // 팬 크기 (mm)
    private Integer fanCount;           // 팬 개수
    private String fanThickness;        // 팬 두께 (예: 25T)
    private String fanConnector;        // 팬 커넥터 (예: 3~4핀)
    private String bearingType;         // 베어링 (예: Rifle(유체))
    private Integer maxFanSpeed;        // 최대 팬 속도 (RPM)
    private Double airFlow;             // 최대 풍량 (CFM)
    private Double airPressure;         // 최대 풍압 (mmH₂O)
    private Double maxFanNoise;         // 최대 팬 소음 (dBA)
    private String fanLifeTime;         // 팬 수명 (예: 40,000시간)


    // 7. 작동 전압 (O/X로 표시되므로 Boolean 사용)

    private Boolean voltageFan12V;      // 팬 12V 작동 전압 지원
    private Boolean voltageLed5V;       // LED 5V 작동 전압 지원


    // 8. 부가 기능 및 LED 시스템

    private Boolean ledLight;           // LED 라이트 유무
    private Boolean pwmSupport;         // PWM 지원 유무
    private String ledColor;            // LED 색상 (예: RGB)
    private Boolean mysticLightSupport; // MYSTIC LIGHT 지원 유무

}