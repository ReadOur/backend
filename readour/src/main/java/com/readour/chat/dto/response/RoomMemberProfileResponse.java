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
@Schema(description = "채팅방 멤버 프로필 응답")
public class RoomMemberProfileResponse {

    @Schema(description = "사용자 ID", example = "2025002")
    private Long userId;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "채팅방 내 역할", example = "MANAGER")
    private String role;
}
