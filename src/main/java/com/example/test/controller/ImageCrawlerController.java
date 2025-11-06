package com.example.test.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageCrawlerController {

    /**
     * 다나와에서 제품 이미지를 크롤링하는 엔드포인트
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProductImage(@RequestParam String productName) {
        try {
            String searchUrl = "https://search.danawa.com/dsearch.php?query=" +
                    productName.replace(" ", "+");

            Document searchDoc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            String imageUrl = "";
            Elements productItems = searchDoc.select(".prod_item");
            Element selectedProduct = null;
            boolean hasDetailPage = false;

            // ✅ prod.danawa.com/info 링크를 가진 제품 찾기
            for (Element item : productItems) {
                Element linkElement = item.selectFirst("a[href*='prod.danawa.com/info']");

                if (linkElement != null) {
                    selectedProduct = item;
                    hasDetailPage = true;

                    // 상세 페이지 URL 가져오기
                    String detailPageUrl = linkElement.attr("href");
                    if (!detailPageUrl.startsWith("http")) {
                        detailPageUrl = "https:" + detailPageUrl;
                    }

                    // 상세 페이지에서 이미지 가져오기 시도
                    try {
                        Document detailDoc = Jsoup.connect(detailPageUrl)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                .timeout(10000)
                                .get();

                        Element imgElement = detailDoc.selectFirst(".thumb_image img");
                        if (imgElement == null) {
                            imgElement = detailDoc.selectFirst("#baseImage");
                        }
                        if (imgElement == null) {
                            imgElement = detailDoc.selectFirst("img[src*='img.danawa.com']");
                        }

                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (imageUrl.isEmpty()) {
                                imageUrl = imgElement.attr("data-original");
                            }
                            if (imageUrl.isEmpty()) {
                                imageUrl = imgElement.attr("data-src");
                            }
                            if (imageUrl.startsWith("//")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("상세 페이지 이미지 크롤링 실패: " + e.getMessage());
                    }

                    // 상세 페이지에서 실패하면 검색 결과에서 추출
                    if (imageUrl.isEmpty()) {
                        Element imgElement = item.selectFirst(".thumb_image img");
                        if (imgElement == null) {
                            imgElement = item.selectFirst("img");
                        }

                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (imageUrl.isEmpty()) {
                                imageUrl = imgElement.attr("data-original");
                            }
                            if (imageUrl.isEmpty()) {
                                imageUrl = imgElement.attr("data-src");
                            }
                            if (imageUrl.isEmpty()) {
                                imageUrl = imgElement.attr("original");
                            }
                            if (imageUrl.startsWith("//")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                    }

                    if (!imageUrl.isEmpty()) {
                        break;
                    }
                }
            }

            // ✅ prod.danawa.com/info 링크를 못 찾았으면 검색 결과 첫 번째 상품 사용
            if (!hasDetailPage && !productItems.isEmpty()) {
                System.out.println("⚠️ prod.danawa.com/info 링크를 찾을 수 없음. 검색 결과 첫 번째 상품 사용");

                selectedProduct = productItems.first();

                Element imgElement = selectedProduct.selectFirst(".thumb_image img");
                if (imgElement == null) {
                    imgElement = selectedProduct.selectFirst("img");
                }

                if (imgElement != null) {
                    imageUrl = imgElement.attr("src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imgElement.attr("data-original");
                    }
                    if (imageUrl.isEmpty()) {
                        imageUrl = imgElement.attr("data-src");
                    }
                    if (imageUrl.isEmpty()) {
                        imageUrl = imgElement.attr("original");
                    }
                    if (imageUrl.startsWith("//")) {
                        imageUrl = "https:" + imageUrl;
                    }
                }
            }

            System.out.println("✅ 이미지 URL: " + imageUrl);

            return ResponseEntity.ok(Map.of(
                    "success", !imageUrl.isEmpty(),
                    "imageUrl", imageUrl,
                    "productName", productName
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "imageUrl", "",
                    "error", e.getMessage()
            ));
        }
    }
}