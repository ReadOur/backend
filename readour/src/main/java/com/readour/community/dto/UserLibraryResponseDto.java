package com.readour.community.dto;

import com.readour.community.entity.UserInterestedLibrary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자 선호 도서관 응답 DTO")
public class UserLibraryResponseDto {

    @Schema(description = "도서관 코드", example = "141321")
    private String libraryCode;

    @Schema(description = "도서관 이름", example = "부천시립상동도서관")
    private String libraryName;

    @Schema(description = "등록 시각")
    private LocalDateTime createdAt;

    public static UserLibraryResponseDto fromEntity(UserInterestedLibrary entity) {
        return UserLibraryResponseDto.builder()
                .libraryCode(entity.getLibraryCode())
                .libraryName(entity.getLibraryName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}