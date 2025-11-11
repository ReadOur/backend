package com.readour.common.dto;

import com.readour.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {

    @Email
    @Schema(description = "이메일", example = "readour@gmail.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @Schema(description = "닉네임", example = "readourUser")
    private String nickname;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "생년월일", example = "1999-04-17")
    private LocalDate birthDate;
}

