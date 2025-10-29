package com.readour.chat.controller;

import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.service.ChatMessageService;
import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(
            summary = "채팅 메시지 전송",
            description = """
                    클라이언트가 보낸 채팅 메시지를 DB에 저장하고, Kafka를 통해 실시간으로 전송합니다.
                    - body 필드에는 JSON 형태의 본문이 들어갑니다.
                    - file 전송은 별도 업로드 API를 통해 처리합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponseDto<MessageDto>> send(@PathVariable Long roomId,
                                                           @Validated @RequestBody MessageDto dto) {
        dto.setRoomId(roomId);
        MessageDto saved = chatMessageService.send(dto);

        ApiResponseDto<MessageDto> response = ApiResponseDto.<MessageDto>builder()
                .status(HttpStatus.OK.value())
                .body(saved)
                .message("메시지를 전송했습니다.")
                .build();

        return ResponseEntity.ok(response);
    }
}

