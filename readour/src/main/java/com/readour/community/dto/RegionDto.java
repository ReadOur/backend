package com.readour.community.dto;

import com.readour.community.entity.LibraryRegion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "지역 코드/이름 응답 DTO")
public class RegionDto {

    @Schema(description = "지역 코드 (API 요청용)", example = "11")
    private String code;

    @Schema(description = "지역 이름 (UI 표시용)", example = "서울")
    private String name;

    @Schema(description = "상위 지역 코드 (세부 지역인 경우)", example = "11", nullable = true)
    private String parentCode;

    public static RegionDto fromEntity(LibraryRegion entity) {
        return new RegionDto(entity.getCode(), entity.getName(), entity.getParentCode());
    }
}