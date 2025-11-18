package com.example.test.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HelpDto {

    @NotEmpty
    private String title;       // 문의 제목

    @NotEmpty
    private String content;     // 문의 내용

    // 비회원 전용 필드 (회원은 세션에서 가져옴)
    @NotEmpty
    private String name;        // 이름

    @NotEmpty
    private String email;       // 이메일

    @NotEmpty
    private String phone;       // 휴대폰 번호

}
