package com.readour.common.dto;

import com.readour.common.entity.CalendarEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "일정 응답 DTO")
public class CalendarEventResponseDto {

    @Schema(description = "일정 ID", example = "1")
    private Long eventId;

    @Schema(description = "캘린더 ID", example = "10")
    private Long calendarId;

    @Schema(description = "일정 제목", example = "개인 독서 시간")
    private String title;

    @Schema(description = "일정 상세 설명", example = "'아몬드' 1~5챕터 읽기")
    private String description;

    @Schema(description = "장소", example = "집 앞 카페")
    private String location;

    @Schema(description = "일정 시작 시각")
    private LocalDateTime startsAt;

    @Schema(description = "일정 종료 시각")
    private LocalDateTime endsAt;

    @Schema(description = "하루 종일 여부")
    private Boolean allDay;

    @Schema(description = "생성자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static CalendarEventResponseDto fromEntity(CalendarEvent event) {
        return CalendarEventResponseDto.builder()
                .eventId(event.getEventId())
                .calendarId(event.getCalendarId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startsAt(event.getStartsAt())
                .endsAt(event.getEndsAt())
                .allDay(event.getAllDay())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}