package com.example.test.chartai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceData {

    private String month; // 2025년 11월 형식

    private Double price; // 가격
}
