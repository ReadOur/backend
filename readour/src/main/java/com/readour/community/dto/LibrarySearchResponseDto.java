package com.readour.community.dto;

import com.readour.community.dto.LibraryApiDtos.LibInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도서관 검색 결과 항목 DTO")
public class LibrarySearchResponseDto {

    @Schema(description = "도서관 코드", example = "141321")
    private String libraryCode;

    @Schema(description = "도서관 이름", example = "부천시립상동도서관")
    private String libraryName;

    @Schema(description = "도서관 주소", example = "경기도 부천시 ...")
    private String address;

    @Schema(description = "도서관 홈페이지", example = "http://...")
    private String homepage;

    /**
     * LibInfo (외부 API DTO)를 LibrarySearchResponseDto (내부 응답 DTO)로 변환합니다.
     */
    public static LibrarySearchResponseDto from(LibInfo info) {
        return LibrarySearchResponseDto.builder()
                .libraryCode(info.getLibCode())
                .libraryName(info.getLibName())
                .address(info.getAddress())
                .homepage(info.getHomepage())
                .build();
    }
}