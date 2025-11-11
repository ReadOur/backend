package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 요청 DTO")
public class PasswordResetRequestDto {

    @Email
    @NotBlank
    @Schema(description = "회원 이메일", example = "readour@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "닉네임", example = "readourUser")
    private String nickname;

    @NotNull
    @Schema(description = "생년월일", example = "1999-04-17")
    private LocalDate birthDate;
}
