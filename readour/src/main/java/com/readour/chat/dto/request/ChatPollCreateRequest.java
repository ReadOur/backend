package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 투표 생성 요청 DTO")
public class ChatPollCreateRequest {

    @NotBlank(message = "question은 필수입니다.")
    @Schema(description = "투표 질문", example = "이번 주 모임 날짜를 선택해주세요.")
    private String question;

    @Schema(description = "투표 설명", example = "복수 선택이 가능합니다.")
    private String description;

    @NotEmpty(message = "options는 한 개 이상이어야 합니다.")
    @Schema(description = "투표 선택지 목록", example = "[\"7월 15일\", \"7월 17일\", \"7월 18일\"]")
    private List<@NotBlank(message = "선택지는 비어 있을 수 없습니다.") String> options;

    @Schema(description = "복수 선택 가능 여부", example = "false")
    private Boolean multipleChoice;

    @Schema(description = "투표 종료 시각", example = "2024-07-14T23:59:00", nullable = true)
    private LocalDateTime closesAt;
}
