package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mainboard_spec")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MainboardSpec {

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
    private String registeredMonth;     // 등록년월 (예: 2023년 04월)
    private String cpuSocket;           // CPU 소켓 (예: AMD(소켓AM5))
    private String chipset;             // 세부 칩셋 (예: AMD B650)
    private String memoryType;          // 메모리 종류 (예: DDR5)
    private String formFactor;          // 폼팩터 (예: M-ATX)
    private Integer cpuSlotCount;       // CPU 장착수 (예: 1개)


    // 3. 메모리 사양

    private String memorySpeed;         // 메모리 속도 (예: 6400MHz(PC5-51200))
    private Integer memorySlotCount;    // 메모리 슬롯 (예: 4개)
    private String memoryChannel;       // 메모리 채널 (예: 듀얼)
    private String maxMemoryCapacity;   // 메모리 용량 (예: 최대 128GB)
    private Boolean expoSupport;        // 메모리 기술 EXPO 지원 유무 ('O' / 'X')


    // 4. 확장 슬롯 및 PCIe

    private String vgaConnection;       // VGA 연결 (예: PCIe4.0 x16)
    private String vgaSlotLocation;     // VGA 슬롯 위치 (예: 1번슬롯(24.4))
    private Boolean pcie40Support;      // PCIe4.0 지원 유무 ('O' / 'X')
    private Boolean pcie50Support;      // PCIe5.0 지원 유무 ('O' / 'X')

    // PCIe 슬롯 개수
    private Integer pcie16SlotCount;    // PCIe x16 슬롯 개수 (예: 1개)
    private Integer pcie1SlotCount;     // PCIe x1 슬롯 개수 (예: 2개)


    // 5. 저장장치 인터페이스 (SATA/M.2)

    private Boolean sata3Support;       // SATA3(6Gb/s) 지원 유무 ('O' / 'X')
    private Integer m2SlotCount;        // M.2 슬롯 개수 (예: 2개)
    private Integer sata3PortCount;     // SATA3 포트 개수 (예: 4개)

    private Boolean m2Pcie50Support;    // M.2 연결 PCIe5.0 지원 유무 ('O' / 'X')
    private Boolean m2Pcie40Support;    // M.2 연결 PCIe4.0 지원 유무 ('O' / 'X')
    private Boolean m2NVMeSupport;      // M.2 연결 NVMe 지원 유무 ('O' / 'X')

    // M.2 폼팩터 지원
    private Boolean m2FormFactor2242;   // M.2 폼팩터 2242 지원 유무 ('O' / 'X')
    private Boolean m2FormFactor2280;   // M.2 폼팩터 2280 지원 유무 ('O' / 'X')
    private Boolean m2FormFactor2260;   // M.2 폼팩터 2260 지원 유무 ('O' / 'X')

    // RAID 지원
    private Boolean nvmeRaidSupport;    // NVMe RAID 지원 유무 ('O' / 'X')
    private Boolean sataRaidSupport;    // SATA RAID 지원 유무 ('O' / 'X')


    // 6. 출력 단자 (그래픽 출력)

    private Boolean hdmiPort;           // HDMI 포트 유무 ('O' / 'X')
    private Boolean dpPort;             // DP 포트 유무 ('O' / 'X')
    private Boolean dsubPort;           // D-SUB 포트 유무 ('O' / 'X')


    // 7. 후면 단자 (포트)

    private Boolean usb3x10GbpsPort;    // USB3.x 10Gbps 포트 유무 ('O' / 'X')
    private Boolean usb3x5GbpsPort;     // USB3.x 5Gbps 포트 유무 ('O' / 'X')
    private Boolean usb20Port;          // USB 2.0 포트 유무 ('O' / 'X')
    private Boolean rj45Port;           // RJ-45 포트 유무 ('O' / 'X')
    private Boolean ps2Port;            // PS/2 포트 유무 ('O' / 'X')
    private Boolean audioJack;          // 오디오잭 유무 ('O' / 'X')

    private Integer rearUsbATotal;      // 후면 USB A타입 총 개수 (예: 8개)
    private Integer rearUsbA10GCount;   // 후면 USB A타입 10Gbps 개수 (예: 2개)
    private Integer rearUsbA5GCount;    // 후면 USB A타입 5Gbps 개수 (예: 4개)
    private Integer rearUsb20Count;     // 후면 USB2.0 개수 (예: 4개)


    // 8. 네트워크 및 오디오

    private String lanChipset;          // 유선 칩셋 (예: Realtek)
    private String lanSpeed;            // 유선 속도 (예: 2.50Gbps)
    private Integer rj45Count;          // RJ-45 포트 개수 (예: 1개)

    private String audioChipset;        // 오디오 칩셋 (예: Realtek)
    private String analogOutput;        // 아날로그 출력 (예: 7.1채널(8ch))


    // 9. USB/팬 헤더

    private Boolean headerUsb30;        // USB3.0 헤더 지원 유무 ('O' / 'X')
    private Boolean headerUsb20;        // USB2.0 헤더 지원 유무 ('O' / 'X')
    private Boolean headerRgb12V4Pin;   // RGB 12V 4핀 헤더 지원 유무 ('O' / 'X')
    private Boolean headerArgb5V3Pin;   // ARGB 5V 3핀 헤더 지원 유무 ('O' / 'X')

    private Integer headerRgb4PinCount; // RGB 4핀 헤더 개수 (예: 1개)
    private Integer headerArgb3PinCount; // ARGB 3핀 헤더 개수 (예: 3개)
    private Integer headerSystemFanCount;// 시스템팬 4핀 헤더 개수 (예: 3개)

    private Integer headerUsb30Count;   // USB3.0 헤더 개수 (예: 2개)
    private Integer headerUsb20Count;   // USB2.0 헤더 개수 (예: 2개)


    // 10. I/O 헤더 및 특징

    private Boolean headerTpm;          // TPM 헤더 지원 유무 ('O' / 'X')
    private Boolean uefiSupport;        // UEFI 지원 유무 ('O' / 'X')

}