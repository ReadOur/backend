package com.readour.chat.controller;

import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.response.MessageListResponse;
import com.readour.chat.service.ChatMessageService;
import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 메시지 타임라인 조회 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageListResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponseDto<MessageListResponse>> getTimeline(@PathVariable Long roomId,
                                                                           @RequestHeader("X-User-Id") Long userId,
                                                                           @RequestParam(required = false)
                                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                           LocalDateTime before,
                                                                           @RequestParam(required = false) Integer limit) {
        MessageListResponse timeline = chatMessageService.getTimeline(roomId, userId, before, limit);

        ApiResponseDto<MessageListResponse> response = ApiResponseDto.<MessageListResponse>builder()
                .status(HttpStatus.OK.value())
                .body(timeline)
                .message("채팅 메시지를 조회했습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "채팅 메시지 전송 / 구현 및 테스트 완료",
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
