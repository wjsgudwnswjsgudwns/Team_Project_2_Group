package com.example.test.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    // 비밀번호 확인용 (현재 비밀번호)
    private String currentPassword;

    // 새 비밀번호 (선택)
    private String newPassword;

    // 새 비밀번호 확인
    private String newPasswordCheck;

    // 닉네임 변경
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, message = "닉네임은 2글자 이상입니다.")
    private String nickname;

    // 이메일 변경
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}