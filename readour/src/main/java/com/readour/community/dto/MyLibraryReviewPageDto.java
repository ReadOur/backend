package com.readour.community.dto;

import com.readour.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@Schema(description = "내 서재 - 리뷰 페이징 응답 DTO")
public class MyLibraryReviewPageDto {

    @Schema(description = "서재 소유자 ID")
    private Long userId;

    @Schema(description = "서재 소유자 닉네임")
    private String nickname;

    @Schema(description = "리뷰 페이징 결과")
    private Page<MyReviewDto> reviewPage;

    public static MyLibraryReviewPageDto from(User user, Page<MyReviewDto> page) {
        return MyLibraryReviewPageDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .reviewPage(page)
                .build();
    }
}