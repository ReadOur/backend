package com.readour.chat.dto.response;

import com.readour.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 일정 작성자 응답 DTO")
public class ChatScheduleCreatorResponse {

    @Schema(description = "작성자 ID", example = "1001")
    private Long id;

    @Schema(description = "작성자 닉네임", example = "독서왕")
    private String username;

    @Schema(description = "채팅방 내 역할", example = "OWNER")
    private Role role;
}
