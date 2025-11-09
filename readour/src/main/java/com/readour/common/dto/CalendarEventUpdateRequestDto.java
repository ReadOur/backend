package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "개인 일정 수정 요청 DTO")
public class CalendarEventUpdateRequestDto {

    @Schema(description = "일정 제목", example = "개인 독서 시간 (변경)")
    private String title;

    @Schema(description = "일정 상세 설명", example = "'아몬드' 1~7챕터 읽기")
    private String description;

    @Schema(description = "장소", example = "집")
    private String location;

    @Schema(description = "일정 시작 시각", example = "2025-11-10T15:00:00")
    private LocalDateTime startsAt;

    @Schema(description = "일정 종료 시각", example = "2025-11-10T17:00:00")
    private LocalDateTime endsAt;

    @Schema(description = "하루 종일 여부", example = "false")
    private Boolean allDay;
}