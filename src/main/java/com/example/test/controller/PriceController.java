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
            String searchUrl = "https://search.danawa.com/dsearch.php?query=" +
                    productName.replace(" ", "+");

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Map<String, String> productInfo = new HashMap<>();

            Elements productItems = searchDoc.select(".prod_item");
            Element selectedProduct = null;
            String detailPageUrl = "";
            boolean hasDetailPage = false;

            // prod.danawa.com/info 링크 찾기
            for (Element item : productItems) {
                Element linkElement = item.selectFirst("a[href*='prod.danawa.com/info']");

                if (linkElement != null) {
                    selectedProduct = item;
                    detailPageUrl = linkElement.attr("href");

                    if (!detailPageUrl.startsWith("http")) {
                        detailPageUrl = "https:" + detailPageUrl;
                    }

                    hasDetailPage = true;
                    break;
                }
            }

            if (!hasDetailPage) {
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
                    hasDetailPage = true;
                }
            }

            // ✅ prod.danawa.com/info 링크가 있는 경우 - 상세 페이지에서 가격 가져오기
            if (hasDetailPage && !detailPageUrl.isEmpty()) {
                productInfo.put("링크", detailPageUrl);

                try {
                    Document detailDoc = Jsoup.connect(detailPageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(10000)
                            .get();

                    // 최저가 정보 - 더 많은 셀렉터 시도
                    Element lowestPrice = detailDoc.selectFirst(".lowest_price em");
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".lowest_price");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".prod_pricelist em");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".prc_c");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst("#lowPriceCmpr em");
                    }
                    if (lowestPrice == null) {
                        lowestPrice = detailDoc.selectFirst(".lwst_prc em");
                    }

                    if (lowestPrice != null) {
                        String priceText = lowestPrice.text().trim();
                        productInfo.put("최저가", priceText);
                    }

                } catch (Exception e) {
                    System.out.println("상세 페이지 가격 크롤링 실패: " + e.getMessage());
                }
            }
            // ✅ prod.danawa.com/info 링크가 없는 경우 - 검색 결과 첫 번째 상품에서 가격 추출
            else {
                System.out.println("⚠️ prod.danawa.com/info 링크를 찾을 수 없음. 검색 결과 첫 번째 상품 사용");

                if (!productItems.isEmpty()) {
                    selectedProduct = productItems.first();
                    productInfo.put("링크", searchUrl);
                }
            }

            // 검색 결과에서 가격 추출 (상세 페이지에서 실패했거나 없는 경우)
            if (selectedProduct != null && !productInfo.containsKey("최저가")) {
                System.out.println("🔍 검색 결과에서 가격 추출 시도...");

                // 방법 1: .price_sect 내부 전체 텍스트 확인
                Element priceSect = selectedProduct.selectFirst(".price_sect");
                if (priceSect != null) {
                    System.out.println("price_sect 발견: " + priceSect.text());

                    // em 태그 찾기
                    Element priceEm = priceSect.selectFirst("em");
                    if (priceEm != null) {
                        String priceText = priceEm.text().trim();
                        System.out.println("em 태그 가격: " + priceText);
                        if (!priceText.isEmpty() && !priceText.equals("최저가")) {
                            productInfo.put("최저가", priceText);
                        }
                    }

                    // a 태그 찾기
                    if (!productInfo.containsKey("최저가")) {
                        Element priceA = priceSect.selectFirst("a");
                        if (priceA != null) {
                            String priceText = priceA.text().trim();
                            System.out.println("a 태그 가격: " + priceText);
                            if (!priceText.isEmpty() && !priceText.equals("최저가") && !priceText.equals("가격비교")) {
                                productInfo.put("최저가", priceText);
                            }
                        }
                    }

                    // strong 태그 찾기
                    if (!productInfo.containsKey("최저가")) {
                        Element priceStrong = priceSect.selectFirst("strong");
                        if (priceStrong != null) {
                            String priceText = priceStrong.text().trim();
                            System.out.println("strong 태그 가격: " + priceText);
                            if (!priceText.isEmpty()) {
                                productInfo.put("최저가", priceText);
                            }
                        }
                    }
                }

                // 방법 2: .spec_price 클래스
                if (!productInfo.containsKey("최저가")) {
                    Element specPrice = selectedProduct.selectFirst(".spec_price");
                    if (specPrice != null) {
                        String priceText = specPrice.text().trim();
                        System.out.println("spec_price: " + priceText);
                        if (!priceText.isEmpty()) {
                            productInfo.put("최저가", priceText);
                        }
                    }
                }

                // 방법 3: .lwst_prc 클래스 (lowest price)
                if (!productInfo.containsKey("최저가")) {
                    Element lwstPrc = selectedProduct.selectFirst(".lwst_prc");
                    if (lwstPrc != null) {
                        Element em = lwstPrc.selectFirst("em");
                        if (em != null) {
                            String priceText = em.text().trim();
                            System.out.println("lwst_prc em: " + priceText);
                            if (!priceText.isEmpty()) {
                                productInfo.put("최저가", priceText);
                            }
                        }
                    }
                }

                // 방법 4: data-price 속성 확인
                if (!productInfo.containsKey("최저가")) {
                    Element priceDataElement = selectedProduct.selectFirst("[data-price]");
                    if (priceDataElement != null) {
                        String dataPrice = priceDataElement.attr("data-price");
                        System.out.println("data-price 속성: " + dataPrice);
                        if (!dataPrice.isEmpty()) {
                            productInfo.put("최저가", dataPrice + "원");
                        }
                    }
                }

                // 방법 5: 숫자 패턴이 있는 텍스트 찾기 (마지막 수단)
                if (!productInfo.containsKey("최저가") && priceSect != null) {
                    String fullText = priceSect.text();
                    System.out.println("전체 텍스트: " + fullText);
                    // "123,456원" 또는 "123,456" 패턴 찾기
                    if (fullText.matches(".*\\d{1,3}(,\\d{3})*.*")) {
                        productInfo.put("최저가", fullText.trim());
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

            System.out.println("✅ 최종 가격 정보: " + productInfo);

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