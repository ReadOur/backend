package com.readour.community.dto;

import lombok.Getter;
import lombok.Setter;

// (SD-15: 댓글 수정) 요청 DTO
@Getter
@Setter
public class CommentUpdateRequestDto {
    private String content;
}
