package com.example.test.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDTO {
    private String title;
    private String description;
    private String link;
    private LocalDateTime publishedDate;
    private String source;
    private String imageUrl;
    private String fullContent;
}