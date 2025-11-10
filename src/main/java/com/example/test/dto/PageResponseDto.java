package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {

    private List<T> content; // 실제 데이터

    private int pageNumber; // 현재 페이지

    private int pageSize; // 페이지 크기

    private long totalElements; // 전체 글 갯수

    private int totalPages; // 전체 페이지 갯수

    private boolean first; // 첫 페이지 여부

    private boolean last; // 마지막 페이지 여부

    private boolean empty; // 빈 페이지 여부
}
