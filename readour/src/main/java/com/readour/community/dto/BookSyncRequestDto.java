package com.readour.community.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSyncRequestDto {

    @NotBlank(message = "ISBN is required")
    @Schema(description = "동기화할 도서의 13자리 ISBN", example = "9788936434267")
    private String isbn;
}
