package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@Schema(description = "마이페이지 - '좋아요 누른 글' 페이징 응답 DTO")
public class MyPageLikedPostsPageDto {

    @Schema(description = "페이지 소유자 ID")
    private Long userId;

    @Schema(description = "페이지 소유자 닉네임")
    private String nickname;

    @Schema(description = "좋아요 누른 글 페이징 결과")
    private Page<PostSummaryDto> likedPostsPage;

    public static MyPageLikedPostsPageDto from(User user, Page<PostSummaryDto> page) {
        return MyPageLikedPostsPageDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .likedPostsPage(page)
                .build();
    }
}