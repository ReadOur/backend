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
@Schema(description = "공개 채팅방 목록 페이지 응답")
public class PublicRoomListResponse {

    @Schema(description = "공개 채팅방 목록")
    private List<PublicRoomListItemResponse> items;

    @Schema(description = "페이지 정보")
    private RoomListPageResponse.PageInfo page;
}
