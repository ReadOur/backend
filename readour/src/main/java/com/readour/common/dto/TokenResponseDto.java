package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT 토큰 응답 DTO")
public class TokenResponseDto {

    @Builder.Default
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "액세스 토큰 만료 시각")
    private LocalDateTime accessTokenExpiresAt;

    public static TokenResponseDto of(String accessToken,
                                      LocalDateTime accessTokenExpiresAt) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .build();
    }
}
