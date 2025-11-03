package com.readour.chat.dto.response;

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
@Schema(description = "공개 채팅방 목록 항목")
public class PublicRoomListItemResponse {

    @Schema(description = "채팅방 ID", example = "301")
    private Long roomId;

    @Schema(description = "채팅방 이름", example = "공개 독서 모임")
    private String name;

    @Schema(description = "채팅방 소개", example = "누구나 참여 가능한 독서 토론방", nullable = true)
    private String description;

    @Schema(description = "현재 참여 인원 수", example = "12")
    private long memberCount;

    @Schema(description = "요청 사용자가 이미 참여 중인지 여부", example = "false")
    private boolean joined;

    @Schema(description = "최근 갱신 시각", example = "2025-01-01T09:00:00", nullable = true)
    private LocalDateTime updatedAt;
}
