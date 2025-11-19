package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindAccountDto {
    private String email;              // 이메일
    private String username;           // 아이디
    private String verificationCode;   // 인증 코드
    private String newPassword;        // 새 비밀번호
    private String newPasswordCheck;   // 새 비밀번호 확인
}