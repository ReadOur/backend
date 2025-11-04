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
@Schema(description = "채팅 투표 결과 응답 DTO")
public class ChatPollResultResponse {

    @Schema(description = "투표 ID", example = "12")
    private Long pollId;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "총 투표 수", example = "8")
    private long totalVotes;

    @Schema(description = "선택지별 득표 현황")
    private List<ChatPollOptionResponse> options;
}
