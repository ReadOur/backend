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
@Schema(description = "채팅 일정 수정 요청 DTO")
public class ChatScheduleUpdateRequest {

    @NotBlank(message = "title은 필수입니다.")
    @Schema(description = "수정할 일정 제목", example = "정기 독서 모임 (변경)")
    private String title;

    @Schema(description = "수정할 일정 설명", example = "오프라인으로 변경됩니다.")
    private String description;

    @NotNull(message = "startAt은 필수입니다.")
    @Schema(description = "수정할 일정 시작 시각", example = "2024-07-15T19:00:00")
    private LocalDateTime startAt;

    @NotNull(message = "endAt은 필수입니다.")
    @Schema(description = "수정할 일정 종료 시각", example = "2024-07-15T21:30:00")
    private LocalDateTime endAt;
}
