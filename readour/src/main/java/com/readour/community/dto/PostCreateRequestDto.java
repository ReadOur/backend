package com.readour.community.dto;

import com.readour.community.enums.PostCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class PostCreateRequestDto {
    private String title;
    private String content;
    private PostCategory category;
    private Long bookId;
    private Boolean isSpoiler;
    private List<String> warnings;
    private List<Long> attachmentIds;
}
