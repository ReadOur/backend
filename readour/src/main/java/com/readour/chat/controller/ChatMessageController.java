package com.readour.chat.controller;

import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.response.MessageListResponse;
import com.readour.chat.dto.request.MessageSendRequest;
import com.readour.chat.service.ChatMessageService;
import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
@SecurityRequirement(name = "bearerAuth")
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
                                                                           @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                           @RequestParam(required = false)
                                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                           LocalDateTime before,
                                                                           @RequestParam(required = false) Integer limit) {
        Long userId = requireUserId(userPrincipal);
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
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @Validated @RequestBody MessageSendRequest request) {
        Long userId = requireUserId(userPrincipal);
        MessageDto saved = chatMessageService.send(MessageDto.builder()
                .roomId(roomId)
                .senderId(userId)
                .type(request.getType())
                .body(request.getBody())
                .replyToMsgId(request.getReplyToMsgId())
                .build());

        ApiResponseDto<MessageDto> response = ApiResponseDto.<MessageDto>builder()
                .status(HttpStatus.OK.value())
                .body(saved)
                .message("메시지를 전송했습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅 파일 메시지 전송")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅방 또는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(value = "/{roomId}/messages/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<MessageDto>> sendFile(@PathVariable Long roomId,
                                                               @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @RequestParam("file") MultipartFile file) {
        Long userId = requireUserId(userPrincipal);
        MessageDto saved = chatMessageService.sendFile(roomId, userId, file);

        ApiResponseDto<MessageDto> response = ApiResponseDto.<MessageDto>builder()
                .status(HttpStatus.OK.value())
                .body(saved)
                .message("파일 메시지를 전송했습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    private Long requireUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 존재하지 않습니다.");
        }
        return userPrincipal.getId();
    }
}
