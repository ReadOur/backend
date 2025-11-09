package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "개인 일정 생성 요청 DTO")
public class CalendarEventCreateRequestDto {

    @NotBlank(message = "일정 제목은 필수입니다.")
    @Schema(description = "일정 제목", example = "개인 독서 시간")
    private String title;

    @Schema(description = "일정 상세 설명", example = "'아몬드' 1~5챕터 읽기")
    private String description;

    @Schema(description = "장소", example = "집 앞 카페")
    private String location;

    @NotNull(message = "시작 시각은 필수입니다.")
    @Schema(description = "일정 시작 시각", example = "2025-11-10T14:00:00")
    private LocalDateTime startsAt;

    @NotNull(message = "종료 시각은 필수입니다.")
    @Schema(description = "일정 종료 시각", example = "2025-11-10T16:00:00")
    private LocalDateTime endsAt;

    @Schema(description = "하루 종일 여부", example = "false")
    private Boolean allDay = false;
}