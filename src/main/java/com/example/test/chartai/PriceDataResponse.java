package com.example.test.chartai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDataResponse {

    private String productName; // 상품명

    private List<PriceData> priceHistory; // 월별 가격 리스트
}
