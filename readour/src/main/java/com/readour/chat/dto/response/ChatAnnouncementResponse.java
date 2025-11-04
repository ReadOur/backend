package com.readour.chat.dto.response;

import com.readour.chat.entity.ChatAnnouncement;
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
@Schema(description = "채팅 공지 상세 응답 DTO")
public class ChatAnnouncementResponse {

    @Schema(description = "공지 ID", example = "12")
    private Long id;

    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @Schema(description = "공지 제목", example = "이번 주 독서 모임 안내")
    private String title;

    @Schema(description = "공지 내용", example = "이번 주 모임은 온라인으로 진행됩니다.")
    private String content;

    @Schema(description = "생성 일시", example = "2024-10-21T10:15:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-10-21T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "작성자 정보")
    private ChatAnnouncementAuthorResponse author;

    public static ChatAnnouncementResponse from(ChatAnnouncement entity,
                                                ChatAnnouncementAuthorResponse author) {
        return ChatAnnouncementResponse.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .author(author)
                .build();
    }
}
