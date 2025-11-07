package com.readour.community.dto;

import com.readour.community.dto.LibraryApiDtos.BookExistResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "특정 도서관의 도서 대출 가능 여부 응답 DTO")
public class LibraryAvailabilityDto {

    @Schema(description = "도서관 코드", example = "141321")
    private String libraryCode;

    @Schema(description = "도서관 이름", example = "부천시립상동도서관")
    private String libraryName;

    @Schema(description = "도서 소장 여부", example = "true")
    private boolean hasBook;

    @Schema(description = "대출 가능 여부", example = "true")
    private boolean loanAvailable;

    public static LibraryAvailabilityDto from(String libraryCode, BookExistResult apiResult) {
        if (apiResult == null) {
            return LibraryAvailabilityDto.builder()
                    .libraryCode(libraryCode)
                    .hasBook(false)
                    .loanAvailable(false)
                    .build();
        }
        return LibraryAvailabilityDto.builder()
                .libraryCode(libraryCode)
                .hasBook("Y".equalsIgnoreCase(apiResult.getHasBook()))
                .loanAvailable("Y".equalsIgnoreCase(apiResult.getLoanAvailable()))
                .build();
    }
}