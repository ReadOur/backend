package com.readour.chat.dto.response;

import com.readour.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 생성 응답 DTO")
public class RoomCreateResponse {

    @Schema(description = "채팅방 ID", example = "101")
    private Long id;

    @Schema(description = "채팅방 범위", example = "PUBLIC")
    private String scope;

    @Schema(description = "채팅방 이름", example = "독서 실시간 토론방")
    private String name;

    @Schema(description = "채팅방 설명", example = "누구나 참여 가능한 공개 토론방", nullable = true)
    private String description;

    @Schema(description = "채팅방 생성자 ID", example = "2025001")
    private Long createdBy;

    @Schema(description = "채팅방 생성 시각", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    public static RoomCreateResponse fromEntity(ChatRoom room) {
        return RoomCreateResponse.builder()
                .id(room.getId())
                .scope(room.getScope())
                .name(room.getName())
                .description(room.getDescription())
                .createdBy(room.getCreatedBy())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
