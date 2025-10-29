package com.readour.chat.dto.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅 메시지 DTO (요청/응답 공통)")
public class MessageDto {

    @Schema(description = "메시지 ID / request시에는 null", example = "1", nullable = true)
    private Long id;

    @NotNull(message = "roomId는 필수입니다.")
    @Schema(description = "채팅방 ID", example = "101")
    private Long roomId;

    @NotNull(message = "senderId는 필수입니다.")
    @Schema(description = "보낸 사람 ID", example = "2025001")
    private Long senderId;

    @NotBlank(message = "type은 필수입니다.")
    @Schema(description = "메시지 타입 (TEXT, IMAGE, POLL 등)", example = "TEXT")
    private String type;

    @NotNull(message = "body는 필수입니다.")
    @Schema(description = "메시지 본문 (JSON 객체)", example = "{\"text\":\"안녕하세요\"}")
    private JsonNode body;

    @Schema(description = "답장 대상 메시지 ID", example = "1234", nullable = true)
    private Long replyToMsgId;

    @Schema(description = "작성 시각 / request시에는 null", example = "2025-10-21T15:42:00", nullable = true)
    private LocalDateTime createdAt;

    @Schema(description = "삭제 시각", example = "2025-10-22T09:00:00", nullable = true)
    private LocalDateTime deletedAt;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static MessageDto fromEntity(ChatMessage entity) {
        return MessageDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .senderId(entity.getSenderId())
                .type(entity.getType())
                .body(parseBody(entity.getBody()))
                .replyToMsgId(entity.getReplyToMsgId())
                .createdAt(entity.getCreatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .type(type)
                .body(writeBody(body))
                .replyToMsgId(replyToMsgId)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .build();
    }

    private static JsonNode parseBody(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid message body JSON stored in database", e);
        }
    }

    private static String writeBody(JsonNode body) {
        if (body == null || body.isNull()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize message body", e);
        }
    }
}
