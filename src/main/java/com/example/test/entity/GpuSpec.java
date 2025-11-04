package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gpu_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GpuSpec {

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
    private String registeredMonth;     // 등록년월 (예: 2025년 03월)
    private String chipsetManufacturer; // 칩셋 제조사 (예: NVIDIA)
    private String productSeries;       // 제품 시리즈 (예: RTX 50)
    private String gpuProcessNode;      // GPU 제조 공정 (예: 4nm)
    private String gpuChipset;          // NVIDIA 칩셋 (예: RTX 5070)
    private String interfaceType;       // 인터페이스 (예: PCIe5.0 x16)
    private String recommendedPsu;      // 권장 파워 용량 (예: 정격파워 650W 이상)
    private String powerPort;           // 전원 포트 (예: 16핀(12V2+6) x1)
    private String thickness;           // 두께 (예: 60mm)
    private String length;              // 가로(길이) (예: 331.9mm)


    // 3. 제품 외형/특징

    private Integer fanCount;           // 팬 개수 (예: 3개)
    private Boolean zeroDbTech;         // 제로팬(0-dB기술) 지원 유무 ('O' / 'X')
    private Boolean backplate;          // 백플레이트 지원 유무 ('O' / 'X')
    private Boolean drMos;              // DrMOS 지원 유무 ('O' / 'X')
    private Boolean ledLight;           // LED 라이트 지원 유무 ('O' / 'X')
    private Boolean dualBios;           // Dual BIOS 지원 유무 ('O' / 'X')

    private Boolean ledFront;           // 전면 LED 지원 유무 ('O' / 'X')
    private Boolean ledSide;            // 측면 LED 지원 유무 ('O' / 'X')


    // 4. 칩셋 사양

    private Integer baseClock;          // 베이스 클럭 (MHz)
    private Integer boostClock;         // 부스트 클럭 (MHz)
    private Integer cudaCores;          // 쿠다 프로세서 (CUDA Cores)
    private Integer streamProcessors;   // 스트림 프로세서 (Stream Processors)


    // 5. 메모리 사양

    private String memoryType;          // 메모리 종류 (예: GDDR7)
    private Integer memoryClock;        // 메모리 클럭 (MHz)
    private String memoryCapacity;      // 메모리 용량 (예: 12GB)
    private String memoryBus;           // 메모리 버스 (예: 192-bit)


    // 6. 출력 및 지원 정보

    private Integer displayPortCount;   // DisplayPort 개수 (예: 3개)
    private Integer hdmiCount;          // HDMI 개수 (예: 1개)
    private Boolean support8K;          // 8K 지원 유무 ('O' / 'X')
    private Boolean supportHDR;         // HDR 지원 유무 ('O' / 'X')
    private Boolean supportHDCP23;      // HDCP 2.3 지원 유무 ('O' / 'X')


    // 7. 전원 및 냉각

    private Integer powerConsumption;   // 사용 전력 (W)
    private Boolean coolerVaporChamber; // 냉각 방식 베이퍼챔버 ('O' / 'X')
    private Boolean coolerHeatpipe;     // 냉각 방식 히트파이프 ('O' / 'X')
    private Boolean coolerFan;          // 냉각 방식 쿨링팬 ('O' / 'X')


    // 8. 구성품

    private Boolean includedAdapter;    // 2x8핀 to 16핀 커넥터 포함 유무 ('O' / 'X')
}