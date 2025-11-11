package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {

    @Email
    @Schema(description = "사용자 이메일", example = "haeda22@naver.com")
    private String email;

    @NotBlank
    @Schema(description = "사용자 비밀번호", example = "password123!")
    private String password;
}
