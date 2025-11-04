package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "책 하이라이트 수정 요청 DTO")
public class BookHighlightUpdateRequestDto {

    @NotBlank(message = "하이라이트 내용은 필수입니다.")
    @Schema(description = "수정된 하이라이트 내용", example = "우리는 모두 연결되어 있었다.")
    private String content;

    @Schema(description = "수정된 페이지 번호", example = "124")
    private Integer pageNumber;
}
