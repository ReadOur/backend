package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 투표 참여 요청 DTO")
public class ChatPollVoteRequest {

    @NotEmpty(message = "selections는 한 개 이상이어야 합니다.")
    @Schema(description = "선택한 투표 옵션 ID 목록", example = "[\"opt_1\", \"opt_3\"]")
    private List<String> selections;
}
