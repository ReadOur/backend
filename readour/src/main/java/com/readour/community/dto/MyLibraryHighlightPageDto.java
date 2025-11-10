package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@Schema(description = "내 서재 - 하이라이트 페이징 응답 DTO")
public class MyLibraryHighlightPageDto {

    @Schema(description = "서재 소유자 ID")
    private Long userId;

    @Schema(description = "서재 소유자 닉네임")
    private String nickname;

    @Schema(description = "하이라이트 페이징 결과")
    private Page<MyHighlightDto> highlightPage;

    public static MyLibraryHighlightPageDto from(User user, Page<MyHighlightDto> page) {
        return MyLibraryHighlightPageDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .highlightPage(page)
                .build();
    }
}