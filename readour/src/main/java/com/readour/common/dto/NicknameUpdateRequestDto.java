package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "닉네임 변경 요청 DTO")
public class NicknameUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하이어야 합니다.")
    @Schema(description = "변경할 새 닉네임", example = "새로운닉네임")
    private String nickname;
}