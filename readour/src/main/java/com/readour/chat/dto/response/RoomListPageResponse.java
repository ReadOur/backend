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
@Schema(description = "내 채팅방 목록 페이지 응답")
public class RoomListPageResponse {

    @Schema(description = "채팅방 목록")
    private List<RoomListItemResponse> items;

    @Schema(description = "페이지 정보")
    private PageInfo page;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지 메타 정보")
    public static class PageInfo {

        @Schema(description = "현재 페이지 번호", example = "0")
        private int number;

        @Schema(description = "페이지 크기", example = "20")
        private int size;

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        private boolean hasNext;
    }
}
