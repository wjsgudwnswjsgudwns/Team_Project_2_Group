package com.example.test.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private String price;
    private Integer quantity;
    private String manufacturer;
    private String category;
}
