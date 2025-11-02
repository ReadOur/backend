package com.readour.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// (SD-14: 댓글 작성) 요청 DTO
@Getter
@Setter
public class CommentCreateRequestDto {
    private String content; // (DC-33: Comment.content)
}