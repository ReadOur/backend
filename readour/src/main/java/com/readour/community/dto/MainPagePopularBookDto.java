package com.readour.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "메인 페이지 인기 도서 응답 DTO (조회 기준 포함)")
public class MainPagePopularBookDto {

    @Schema(description = "인기 도서 조회 기준 (예: '30대 남성', '전체')", example = "30대 남성")
    private String criteria;

    @Schema(description = "인기 도서 페이징 결과")
    private Page<PopularBookDto> popularBooks;
}