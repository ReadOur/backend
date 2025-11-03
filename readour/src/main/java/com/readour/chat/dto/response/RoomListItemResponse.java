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
@Schema(description = "내 채팅방 목록 항목")
public class RoomListItemResponse {

    @Schema(description = "채팅방 ID", example = "42")
    private Long roomId;

    @Schema(description = "채팅방 이름", example = "프로젝트 A")
    private String name;

    @Schema(description = "마지막 메시지 정보", nullable = true)
    private LastMessage lastMsg;

    @Schema(description = "읽지 않은 메시지 수", example = "3")
    private long unreadCount;

    @Schema(description = "채팅방 갱신 시각", example = "2025-01-01T10:05:00", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "상단 고정 여부", example = "true")
    private boolean pinned;

    @Schema(description = "핀 순서", example = "1", nullable = true)
    private Integer pinOrder;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마지막 메시지 요약")
    public static class LastMessage {

        @Schema(description = "마지막 메시지 ID", example = "987")
        private Long id;

        @Schema(description = "마지막 메시지 미리보기", example = "오늘 회의 안건 공유드립니다")
        private String preview;

        @Schema(description = "마지막 메시지 생성 시각", example = "2025-01-01T09:55:00")
        private LocalDateTime createdAt;
    }
}
