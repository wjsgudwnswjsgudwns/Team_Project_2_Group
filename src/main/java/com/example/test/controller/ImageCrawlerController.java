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
     * prod.danawa.com/info 링크를 가진 제품의 이미지만 반환
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProductImage(@RequestParam String productName) {
        try {
            String searchUrl = "https://search.danawa.com/dsearch.php?query=" +
                    productName.replace(" ", "+");

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            String imageUrl = "";

            // ✅ prod.danawa.com/info 링크를 가진 제품 찾기
            Elements productItems = doc.select(".prod_item");

            for (Element item : productItems) {
                Element linkElement = item.selectFirst("a[href*='prod.danawa.com/info']");

                if (linkElement != null) {
                    // 해당 제품의 이미지 찾기
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
                        if (imageUrl.startsWith("//")) {
                            imageUrl = "https:" + imageUrl;
                        }
                    }

                    // prod.danawa.com/info 링크를 가진 첫 제품을 찾았으면 중단
                    if (!imageUrl.isEmpty()) {
                        break;
                    }
                }
            }

            // 위 방법으로 못 찾았으면 다른 방법 시도
            if (imageUrl.isEmpty()) {
                Elements allLinks = doc.select("a[href*='prod.danawa.com/info']");
                if (!allLinks.isEmpty()) {
                    Element firstLink = allLinks.first();
                    Element parentItem = firstLink.parent();

                    // 부모 요소를 따라 올라가면서 prod_item 찾기
                    while (parentItem != null && !parentItem.hasClass("prod_item")) {
                        parentItem = parentItem.parent();
                    }

                    if (parentItem != null) {
                        Element imgElement = parentItem.selectFirst(".thumb_image img");
                        if (imgElement == null) {
                            imgElement = parentItem.selectFirst("img");
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
                    }
                }
            }

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