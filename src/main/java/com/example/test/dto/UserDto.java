package com.example.test.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Size(min = 4, message = "아이디는 4글자 이상입니다.")
    private String username;

    @Size(min = 8, max = 16, message = "비밀번호는 8글자 이상 16글자 이하입니다.")
    private String password;

    @Size(min = 8, max = 16, message = "비밀번호는 8글자 이상 16글자 이하입니다.")
    private String passwordCheck;

    @Size(min = 2, message = "닉네임은 2글자 이상입니다.")
    private String nickname;

    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;

}
