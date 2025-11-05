package com.example.test.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/price")
@CrossOrigin(origins = "http://localhost:3000")
public class PriceController {

    @GetMapping("/product-info")
    public ResponseEntity<?> getProductInfo(@RequestParam String productName) {
        try {
            // 1단계: 검색 결과에서 제품 찾기
            String searchUrl = "https://search.danawa.com/dsearch.php?query=" +
                    productName.replace(" ", "+");

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Map<String, String> productInfo = new HashMap<>();

            // ✅ prod.danawa.com/info 링크를 가진 제품만 찾기
            Elements productItems = searchDoc.select(".prod_item");

            Element selectedProduct = null;
            String detailPageUrl = "";

            // 모든 검색 결과를 순회하면서 prod.danawa.com/info 링크 찾기
            for (Element item : productItems) {
                Element linkElement = item.selectFirst("a[href*='prod.danawa.com/info']");

                if (linkElement != null) {
                    selectedProduct = item;
                    detailPageUrl = linkElement.attr("href");

                    if (!detailPageUrl.startsWith("http")) {
                        detailPageUrl = "https:" + detailPageUrl;
                    }

                    // prod.danawa.com/info 링크를 찾았으면 중단
                    break;
                }
            }

            // prod.danawa.com/info 링크를 못 찾았으면 다른 방법 시도
            if (selectedProduct == null) {
                // .prod_name a 중에서 prod.danawa.com/info 링크 찾기
                Elements allLinks = searchDoc.select("a[href*='prod.danawa.com/info']");
                if (!allLinks.isEmpty()) {
                    Element firstLink = allLinks.first();
                    selectedProduct = firstLink.parent();
                    while (selectedProduct != null && !selectedProduct.hasClass("prod_item")) {
                        selectedProduct = selectedProduct.parent();
                    }

                    detailPageUrl = firstLink.attr("href");
                    if (!detailPageUrl.startsWith("http")) {
                        detailPageUrl = "https:" + detailPageUrl;
                    }
                }
            }

            if (selectedProduct != null && !detailPageUrl.isEmpty()) {
                productInfo.put("링크", detailPageUrl);

                // 2단계: 상세 페이지에서 가격 정보 크롤링
                try {
                    Document detailDoc = Jsoup.connect(detailPageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(10000)
                            .get();

                    // 최저가 정보
                    Element lowestPrice = detailDoc.selectFirst(".lowest_price");
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".prod_pricelist em");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".prc_c");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst("#lowPriceCmpr em");
                    }

                    if (lowestPrice != null) {
                        String priceText = lowestPrice.text().trim();
                        productInfo.put("최저가", priceText);
                    }

                    // 제품 이미지
                    Element imgElement = detailDoc.selectFirst(".thumb_image img");
                    if (imgElement == null) {
                        imgElement = detailDoc.selectFirst("#baseImage");
                    }
                    if (imgElement == null) {
                        imgElement = detailDoc.selectFirst("img[src*='img.danawa.com']");
                    }

                    if (imgElement != null) {
                        String imageUrl = imgElement.attr("src");
                        if (imageUrl.isEmpty()) {
                            imageUrl = imgElement.attr("data-original");
                        }
                        if (imageUrl.isEmpty()) {
                            imageUrl = imgElement.attr("data-src");
                        }
                        if (imageUrl.startsWith("//")) {
                            imageUrl = "https:" + imageUrl;
                        }
                        if (!imageUrl.isEmpty()) {
                            productInfo.put("이미지", imageUrl);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("상세 페이지 크롤링 실패: " + e.getMessage());
                }

                // 상세 페이지에서 실패했으면 검색 결과에서 추출
                if (!productInfo.containsKey("이미지")) {
                    Element imgElement = selectedProduct.selectFirst(".thumb_image img");
                    if (imgElement == null) {
                        imgElement = selectedProduct.selectFirst("img");
                    }

                    if (imgElement != null) {
                        String imageUrl = imgElement.attr("src");
                        if (imageUrl.isEmpty()) {
                            imageUrl = imgElement.attr("data-original");
                        }
                        if (imageUrl.isEmpty()) {
                            imageUrl = imgElement.attr("data-src");
                        }
                        if (imageUrl.startsWith("//")) {
                            imageUrl = "https:" + imageUrl;
                        }
                        if (!imageUrl.isEmpty()) {
                            productInfo.put("이미지", imageUrl);
                        }
                    }
                }

                if (!productInfo.containsKey("최저가")) {
                    Element priceElement = selectedProduct.selectFirst(".price_sect a");
                    if (priceElement == null) {
                        priceElement = selectedProduct.selectFirst(".price_sect em");
                    }

                    if (priceElement != null) {
                        String priceText = priceElement.text().trim();
                        if (!priceText.isEmpty() && !priceText.equals("최저가")) {
                            productInfo.put("최저가", priceText);
                        }
                    }
                }
            }

            // 기본값 설정
            if (!productInfo.containsKey("최저가") || productInfo.get("최저가").isEmpty()) {
                productInfo.put("최저가", "가격 정보 없음");
            }
            if (!productInfo.containsKey("링크") || productInfo.get("링크").isEmpty()) {
                productInfo.put("링크", searchUrl);
            }

            return ResponseEntity.ok(Map.of(
                    "success", !productInfo.isEmpty(),
                    "info", productInfo,
                    "productName", productName
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "info", new HashMap<String, String>() {{
                        put("최저가", "가격 정보 없음");
                        put("링크", "https://search.danawa.com/dsearch.php?query=" +
                                productName.replace(" ", "+"));
                    }},
                    "error", e.getMessage()
            ));
        }
    }
}