package com.readour.chat.dto.event;

import com.readour.chat.dto.response.ChatPollResponse;
import com.readour.chat.dto.response.ChatPollResultResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 투표 이벤트 DTO")
public class ChatPollEvent {

    @Schema(description = "이벤트 액션", example = "CREATED")
    private String action;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "투표 ID", example = "12")
    private Long pollId;

    @Schema(description = "이벤트 발생자 ID", example = "1001")
    private Long actorId;

    @Schema(description = "투표 상세", nullable = true)
    private ChatPollResponse poll;

    @Schema(description = "투표 결과", nullable = true)
    private ChatPollResultResponse result;
}
