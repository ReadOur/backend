package com.readour.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 페이지 초기 데이터 응답")
public class ChatPageOverviewResponse {

    @Schema(description = "내 채팅방 목록 정보")
    private RoomListPageResponse myRooms;

    @Schema(description = "공개 채팅방 목록 정보")
    private PublicRoomListResponse publicRooms;
}
