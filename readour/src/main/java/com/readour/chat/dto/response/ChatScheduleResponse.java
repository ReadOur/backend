package com.readour.chat.dto.response;

import com.readour.common.entity.CalendarEvent;
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
@Schema(description = "채팅 일정 응답 DTO")
public class ChatScheduleResponse {

    @Schema(description = "캘린더 ID", example = "301")
    private Long calendarId;

    @Schema(description = "이벤트 ID", example = "42")
    private Long eventId;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "일정 제목", example = "정기 독서 모임")
    private String title;

    @Schema(description = "일정 설명", example = "7월 세번째 모임 - 온라인 진행")
    private String description;

    @Schema(description = "일정 시작 시각", example = "2024-07-15T20:00:00")
    private LocalDateTime startAt;

    @Schema(description = "일정 종료 시각", example = "2024-07-15T22:00:00")
    private LocalDateTime endAt;

    @Schema(description = "생성 시각", example = "2024-07-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2024-07-05T09:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "일정 작성자 정보")
    private ChatScheduleCreatorResponse creator;

    public static ChatScheduleResponse from(CalendarEvent event,
                                            Long roomId,
                                            ChatScheduleCreatorResponse creator) {
        return ChatScheduleResponse.builder()
                .calendarId(event.getCalendarId())
                .eventId(event.getEventId())
                .roomId(roomId)
                .title(event.getTitle())
                .description(event.getDescription())
                .startAt(event.getStartsAt())
                .endAt(event.getEndsAt())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .creator(creator)
                .build();
    }
}
