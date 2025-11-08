package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "선호 도서관 등록/삭제 요청 DTO")
public class LibraryRegistrationRequestDto {

    @NotBlank(message = "도서관 코드는 필수입니다.")
    @Schema(description = "등록할 도서관 코드 (정보나루 API 기준)", example = "141321")
    private String libraryCode;

    @NotBlank(message = "도서관 이름은 필수입니다.")
    @Schema(description = "등록할 도서관 이름", example = "부천시립상동도서관")
    private String libraryName;
}