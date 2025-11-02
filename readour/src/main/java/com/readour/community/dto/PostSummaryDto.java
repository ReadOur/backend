package com.readour.community.dto;

import com.readour.common.entity.User;
import com.readour.community.entity.Post;
import com.readour.community.enums.PostCategory;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {
    private Long postId;
    private String title;
    private PostCategory category;
    private String authorNickname;
    private Integer hit;
    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;

    public static PostSummaryDto fromEntity(Post post, Long likeCount, Long commentCount, Boolean isLiked) {
        User author = post.getUser();

        return PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname(author != null ? author.getNickname() : "탈퇴한 유저")
                .hit(post.getHit())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .build();
    }
}