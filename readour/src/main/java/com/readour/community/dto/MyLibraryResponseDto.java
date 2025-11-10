package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "내 서재 조회 응답 DTO (미리보기)")
public class MyLibraryResponseDto {

    @Schema(description = "서재 소유자 ID")
    private Long userId;

    @Schema(description = "서재 소유자 닉네임")
    private String nickname;

    @Schema(description = "위시리스트에 추가한 도서 목록")
    private List<MyWishlistDto> wishlist;

    @Schema(description = "내가 작성한 도서 리뷰 목록")
    private List<MyReviewDto> reviews;

    @Schema(description = "내가 작성한 도서 하이라이트 목록")
    private List<MyHighlightDto> highlights;

    public static MyLibraryResponseDto from(User user, List<MyWishlistDto> wishlist, List<MyReviewDto> reviews, List<MyHighlightDto> highlights) {
        return MyLibraryResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .wishlist(wishlist)
                .reviews(reviews)
                .highlights(highlights)
                .build();
    }
}