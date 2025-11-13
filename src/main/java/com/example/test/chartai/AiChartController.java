package com.example.test.chartai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/chart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AiChartController {

    @Autowired
    private AiPriceService aiPriceService;

    // 제품 이름을 받아서 가격 히스토리를 반환하는 API
    @PostMapping("/history")
    public ResponseEntity<?> getPriceHistory(
            @RequestBody PriceDataRequest request) {

        try {
            // 1. GeminiService를 통해 가격 데이터 조회
            List<PriceData> priceHistory = aiPriceService.getPriceHistory(request.getProductName());

            // 2. 응답 객체 생성
            PriceDataResponse response = new PriceDataResponse(
                    request.getProductName(),  // 제품 이름
                    priceHistory               // 가격 데이터 리스트
            );

            // 3. HTTP 200 OK와 함께 응답 반환
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // ⭐ 예외 처리 추가
            Map<String, String> errorResponse = new HashMap<>();

            // 출시되지 않은 제품 예외
            if (e.getMessage() != null && e.getMessage().contains("출시되지 않았습니다")) {
                errorResponse.put("error", "NOT_RELEASED");
                errorResponse.put("message", "이 제품은 아직 출시되지 않았거나 가격 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(400).body(errorResponse);
            }

            // 기타 예외
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", "가격 데이터를 가져오는데 실패했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
