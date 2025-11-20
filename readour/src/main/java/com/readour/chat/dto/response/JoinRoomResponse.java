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
@Schema(description = "채팅방 참여 응답")
public class JoinRoomResponse {

    @Schema(description = "갱신된 내 채팅방 목록")
    private RoomListPageResponse rooms;

    @Schema(description = "초기 채팅 메시지 타임라인")
    private MessageListResponse timeline;
}
