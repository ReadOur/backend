package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "마이페이지 조회 응답 DTO (미리보기)")
public class MyPageResponseDto {

    @Schema(description = "페이지 소유자 ID")
    private Long userId;

    @Schema(description = "페이지 소유자 닉네임")
    private String nickname;

    @Schema(description = "내가 쓴 게시글 (미리보기)")
    private List<PostSummaryDto> myPosts;

    @Schema(description = "내가 쓴 댓글 (미리보기)")
    private List<MyCommentDto> myComments;

    @Schema(description = "좋아요 누른 글 (미리보기)")
    private List<PostSummaryDto> likedPosts;

    public static MyPageResponseDto from(User user, List<PostSummaryDto> myPosts, List<MyCommentDto> myComments, List<PostSummaryDto> likedPosts) {
        return MyPageResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .myPosts(myPosts)
                .myComments(myComments)
                .likedPosts(likedPosts)
                .build();
    }
}