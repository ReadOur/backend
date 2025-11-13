package com.readour.community.dto;

import com.readour.community.enums.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private PostCategory category;
    private Long bookId;
    private Boolean isSpoiler;
    private List<String> warnings;
    private List<Long> attachmentIds;

    @Schema(description = "수정할 모집 인원 (GROUP 전용, 2 이상)", example = "5")
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    private Integer recruitmentLimit;

    @Schema(description = "수정할 채팅방 이름 (GROUP 전용)")
    private String chatRoomName;

    @Schema(description = "수정할 채팅방 설명 (GROUP 전용)")
    private String chatRoomDescription;
}
