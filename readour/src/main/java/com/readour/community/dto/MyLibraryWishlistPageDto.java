package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@Schema(description = "내 서재 - 위시리스트 페이징 응답 DTO")
public class MyLibraryWishlistPageDto {

    @Schema(description = "서재 소유자 ID")
    private Long userId;

    @Schema(description = "서재 소유자 닉네임")
    private String nickname;

    @Schema(description = "위시리스트 페이징 결과")
    private Page<MyWishlistDto> wishlistPage;

    public static MyLibraryWishlistPageDto from(User user, Page<MyWishlistDto> page) {
        return MyLibraryWishlistPageDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .wishlistPage(page)
                .build();
    }
}