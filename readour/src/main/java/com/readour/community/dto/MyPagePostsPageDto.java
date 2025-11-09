package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@Schema(description = "마이페이지 - '내가 쓴 글' 페이징 응답 DTO")
public class MyPagePostsPageDto {

    @Schema(description = "페이지 소유자 ID")
    private Long userId;

    @Schema(description = "페이지 소유자 닉네임")
    private String nickname;

    @Schema(description = "게시글 페이징 결과")
    private Page<PostSummaryDto> postPage;

    public static MyPagePostsPageDto from(User user, Page<PostSummaryDto> page) {
        return MyPagePostsPageDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .postPage(page)
                .build();
    }
}