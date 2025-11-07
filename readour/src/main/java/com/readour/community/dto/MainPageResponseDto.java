package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "메인 페이지 응답 DTO")
public class MainPageResponseDto {

    @Schema(description = "주간 인기 게시글 목록 (좋아요 순, 최대 5개)")
    private List<PostSummaryDto> popularPosts;

    @Schema(description = "모집 게시글 목록 (최신순, 최대 5개)")
    private List<PostSummaryDto> recruitmentPosts;

    @Schema(description = "인기 도서 목록 (회원 맞춤형 또는 전체, 최대 10개)")
    private Page<PopularBookDto> popularBooks;
}
