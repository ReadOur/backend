package com.readour.chat.dto.response;

import com.readour.chat.dto.common.MessageDto;
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
@Schema(description = "채팅 메시지 타임라인 응답 DTO")
public class MessageListResponse {

    @Schema(description = "메시지 목록")
    private List<MessageDto> items;

    @Schema(description = "페이징 정보")
    private Paging paging;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "다음 페이지 정보")
    public static class Paging {

        @Schema(description = "다음 조회를 위한 before 파라미터", example = "2025-01-01T09:00:00", nullable = true)
        private LocalDateTime nextBefore;
    }
}
