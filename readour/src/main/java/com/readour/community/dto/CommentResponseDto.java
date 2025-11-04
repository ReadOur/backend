package com.readour.community.dto;

import com.readour.common.entity.User;
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
        User author = comment.getUser();

        return CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .content(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .authorId(author != null ? author.getId() : null)
                .authorNickname(author != null ? author.getNickname() : "탈퇴한 유저")
                .createdAt(comment.getCreatedAt())
                .build();
    }
}