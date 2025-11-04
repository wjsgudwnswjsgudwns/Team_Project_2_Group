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
    private Long id;

    @Column(nullable = false)
    private String name;

    private String manufacturer;
    private String price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    
    // 핵심: JSON으로 유연하게 저장
    @Column(columnDefinition = "JSON")
    private String specs;  // JSON 문자열로 저장
}