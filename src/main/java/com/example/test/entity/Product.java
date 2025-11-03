package com.example.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 고유 식별자

    @Column(nullable = false)
    private String name; // 상품명 (예: '인텔 코어 i9-14900K', '삼성전자 DDR5-5600')

    private String manufacturer; // 제조사

    private Integer price; // 가격 (원)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category; // 카테고리

}
