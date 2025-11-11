package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "중복 여부 응답 DTO")
public class DuplicateCheckResponseDto {

    @Schema(description = "사용 가능 여부", example = "true")
    private final boolean available;
}
