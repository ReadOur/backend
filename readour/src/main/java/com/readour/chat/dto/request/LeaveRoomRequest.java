package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 나가기 요청 DTO")
public class LeaveRoomRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Schema(description = "채팅방을 나갈 사용자 ID", example = "2025001")
    private Long userId;
}
