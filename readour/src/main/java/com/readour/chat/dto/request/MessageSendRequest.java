package com.readour.chat.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 DTO (JWT 사용자 기준)")
public class MessageSendRequest {

    @NotBlank(message = "type은 필수입니다.")
    @Schema(description = "메시지 타입 (TEXT, IMAGE 등)", example = "TEXT")
    private String type;

    @NotNull(message = "body는 필수입니다.")
    @Schema(description = "메시지 내용(JSON 객체)", example = "{\"text\":\"안녕하세요\"}")
    private JsonNode body;

    @Schema(description = "답장 대상 메시지 ID", example = "123")
    private Long replyToMsgId;
}
