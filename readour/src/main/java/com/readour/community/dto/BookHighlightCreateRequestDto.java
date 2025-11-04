package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "책 하이라이트 작성 요청 DTO")
public class BookHighlightCreateRequestDto {

    @NotBlank(message = "하이라이트 내용은 필수입니다.")
    @Schema(description = "하이라이트 내용 (인용구)", example = "우리는 모두 연결되어 있다.")
    private String content;

    @Schema(description = "인용구가 있는 페이지 번호", example = "123")
    private Integer pageNumber;
}