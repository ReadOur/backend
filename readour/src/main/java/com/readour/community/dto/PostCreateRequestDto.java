package com.readour.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDto {
    private String title;
    private String content;
    private String category;
    private Long bookId;
    private Boolean isSpoiler;
}