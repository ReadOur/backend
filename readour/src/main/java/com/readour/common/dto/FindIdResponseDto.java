package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "아이디(이메일) 찾기 응답 DTO")
public class FindIdResponseDto {

    @Schema(description = "회원 이메일 (로그인 아이디)", example = "readour@gmail.com")
    private final String email;
}
