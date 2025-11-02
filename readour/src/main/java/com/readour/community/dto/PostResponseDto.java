package com.readour.community.dto;

import com.readour.common.entity.User;
import com.readour.community.entity.Post;
import com.readour.community.entity.PostWarning;
import com.readour.community.enums.PostCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private PostCategory category;
    private String authorNickname;
    private Long authorId;
    private Integer hit;
    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;
    private List<PostWarning> warnings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> comments;

    public static PostResponseDto fromEntity(Post post, List<CommentResponseDto> comments, Long likeCount, Long commentCount, Boolean isLiked) {
        User author = post.getUser();

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorId(author != null ? author.getId() : null)
                .authorNickname(author != null ? author.getNickname() : "탈퇴한 유저")
                .hit(post.getHit())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .warnings(post.getWarnings())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }
}
