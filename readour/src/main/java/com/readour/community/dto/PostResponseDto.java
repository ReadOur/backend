package com.readour.community.dto;

import com.readour.community.entity.Post;
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
    private String category;
    private String authorNickname;
    private Long authorId;
    private int hit;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> comments;

    public static PostResponseDto fromEntity(Post post, List<CommentResponseDto> comments, Long likeCount, Long commentCount) {
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorId(post.getUserId())
                .authorNickname("temp_user") // TODO: User 서비스에서 닉네임 조회
                .hit(post.getHit())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }
}