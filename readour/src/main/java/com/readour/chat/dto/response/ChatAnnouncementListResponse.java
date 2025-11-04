package com.readour.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 공지 목록 응답 DTO")
public class ChatAnnouncementListResponse {

    @Schema(description = "공지 목록")
    private List<ChatAnnouncementSummaryResponse> items;

    @Schema(description = "페이지 정보")
    private PageInfo page;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지 정보 DTO")
    public static class PageInfo {
        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        private int page;

        @Schema(description = "페이지 크기", example = "10")
        private int size;

        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;

        @Schema(description = "전체 항목 수", example = "25")
        private long totalElements;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;
    }
}
