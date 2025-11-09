package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "내 서재 조회 응답 DTO")
public class MyLibraryResponseDto {

    @Schema(description = "위시리스트에 추가한 도서 목록")
    private List<MyWishlistDto> wishlist;

    @Schema(description = "내가 작성한 도서 리뷰 목록")
    private List<MyReviewDto> reviews;

    @Schema(description = "내가 작성한 도서 하이라이트 목록")
    private List<MyHighlightDto> highlights;
}