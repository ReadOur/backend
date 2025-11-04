package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 일정 생성 요청 DTO")
public class ChatScheduleCreateRequest {

    @NotBlank(message = "title은 필수입니다.")
    @Schema(description = "일정 제목", example = "정기 독서 모임")
    private String title;

    @Schema(description = "일정 설명", example = "7월 세번째 모임 - 온라인 진행")
    private String description;

    @NotNull(message = "startAt은 필수입니다.")
    @Schema(description = "일정 시작 시각", example = "2024-07-15T20:00:00")
    private LocalDateTime startAt;

    @NotNull(message = "endAt은 필수입니다.")
    @Schema(description = "일정 종료 시각", example = "2024-07-15T22:00:00")
    private LocalDateTime endAt;
}
