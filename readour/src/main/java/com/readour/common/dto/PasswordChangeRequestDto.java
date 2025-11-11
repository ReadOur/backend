package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordChangeRequestDto {

    @NotBlank
    @Schema(description = "현재 비밀번호", example = "currentPassword1!")
    private String currentPassword;

    @NotBlank
    @Schema(description = "새 비밀번호", example = "newPassword2@")
    private String newPassword;
}
