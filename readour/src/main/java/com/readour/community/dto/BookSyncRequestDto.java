package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "도서 DB 동기화(저장) 요청 DTO")
public class BookSyncRequestDto {

    @NotBlank(message = "ISBN is required")
    @Schema(description = "DB에 저장할 도서의 13자리 ISBN", example = "9788936434267")
    private String isbn;
}