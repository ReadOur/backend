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
@Schema(description = "채팅 투표 선택지 응답 DTO")
public class ChatPollOptionResponse {

    @Schema(description = "선택지 식별자", example = "opt_1")
    private String id;

    @Schema(description = "선택지 텍스트", example = "7월 15일")
    private String text;

    @Schema(description = "선택지에 대한 득표 수", example = "3")
    private long voteCount;
}
