package com.readour.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "채팅 투표 상세 응답 DTO")
public class ChatPollResponse {

    @Schema(description = "투표 ID", example = "12")
    private Long id;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "투표 질문", example = "이번 주 모임 날짜를 선택해주세요.")
    private String question;

    @Schema(description = "투표 설명", example = "복수 선택 가능")
    private String description;

    @Schema(description = "복수 선택 가능 여부", example = "false")
    private Boolean multipleChoice;

    @Schema(description = "투표 종료 시각", example = "2024-07-14T23:59:00", nullable = true)
    private LocalDateTime closesAt;

    @Schema(description = "생성 시각", example = "2024-07-10T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2024-07-11T09:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "투표 선택지 목록")
    private List<ChatPollOptionResponse> options;

    @Schema(description = "투표 생성자 정보")
    private ChatScheduleCreatorResponse creator;

    public static ChatPollResponse from(Long id,
                                        Long roomId,
                                        String question,
                                        String description,
                                        boolean multipleChoice,
                                        LocalDateTime closesAt,
                                        LocalDateTime createdAt,
                                        LocalDateTime updatedAt,
                                        List<ChatPollOptionResponse> options,
                                        ChatScheduleCreatorResponse creator) {
        return ChatPollResponse.builder()
                .id(id)
                .roomId(roomId)
                .question(question)
                .description(description)
                .multipleChoice(multipleChoice)
                .closesAt(closesAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .options(options)
                .creator(creator)
                .build();
    }
}
