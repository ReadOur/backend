package com.readour.community.dto;

import com.readour.community.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private String content;
    private String authorNickname;
    private Long authorId;
    private LocalDateTime createdAt;

    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .content(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .authorId(comment.getUserId())
                .authorNickname("temp_user_comment") // TODO: User 서비스에서 닉네임 조회
                .createdAt(comment.getCreatedAt())
                .build();
    }
}