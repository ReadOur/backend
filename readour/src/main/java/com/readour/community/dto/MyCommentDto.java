package com.readour.community.dto;

import com.readour.community.entity.Comment;
import com.readour.community.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "마이페이지 - 작성한 댓글 DTO")
public class MyCommentDto {

    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "댓글 작성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "원본 게시글 ID")
    private Long postId;

    @Schema(description = "원본 게시글 제목")
    private String postTitle;

    public static MyCommentDto fromEntities(Comment comment, Post post) {
        return MyCommentDto.builder()
                .commentId(comment.getCommentId())
                .content(comment.getIsDeleted() ? "(삭제된 댓글)" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPostId())
                .postTitle(post != null ? post.getTitle() : "삭제되거나 찾을 수 없는 게시글")
                .build();
    }
}