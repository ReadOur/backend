package com.readour.chat.dto.event;

import com.readour.chat.dto.response.ChatAnnouncementResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 공지 이벤트 DTO")
public class ChatAnnouncementEvent {

    @Schema(description = "이벤트 액션", example = "CREATED")
    private String action;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "이벤트를 발생시킨 사용자 ID", example = "1001")
    private Long actorId;

    @Schema(description = "공지 ID", example = "12")
    private Long announcementId;

    @Schema(description = "공지 상세 정보", nullable = true)
    private ChatAnnouncementResponse announcement;
}
