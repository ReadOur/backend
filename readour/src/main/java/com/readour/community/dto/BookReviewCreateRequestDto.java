package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "책 리뷰 작성 요청 DTO")
public class BookReviewCreateRequestDto {

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Schema(description = "리뷰 내용", example = "정말 감명깊은 책입니다.")
    private String content;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하이어야 합니다.")
    @Schema(description = "평점 (1~5)", example = "5")
    private Integer rating;
}
