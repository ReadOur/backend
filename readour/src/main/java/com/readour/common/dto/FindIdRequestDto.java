package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "아이디(이메일) 찾기 요청 DTO")
public class FindIdRequestDto {

    @NotBlank
    @Schema(description = "닉네임", example = "readourUser")
    private String nickname;

    @NotNull
    @Schema(description = "생년월일", example = "1999-04-17")
    private LocalDate birthDate;
}
