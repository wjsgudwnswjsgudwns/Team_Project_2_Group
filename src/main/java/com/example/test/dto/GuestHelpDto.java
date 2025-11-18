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
public class GuestHelpDto {

    @NotEmpty(message = "이름을 입력해주세요.")
    private String name;

    @NotEmpty(message = "휴대폰 번호를 입력해주세요.")
    private String phone;
}
