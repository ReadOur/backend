package com.readour.chat.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/messages")
public class ChatMessageModerationController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 메시지 가리기 / 구현")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 가리기 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{messageId}/hide")
    public ResponseEntity<ApiResponseDto<Void>> hideMessage(@PathVariable Long messageId,
                                                            @RequestHeader("X-User-Id") Long userId) {
        chatMessageService.hideMessage(messageId, userId);
        ApiResponseDto<Void> body = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("메시지를 가렸습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 메시지 가리기 해제 / 구현")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 가리기 해제 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{messageId}/hide")
    public ResponseEntity<ApiResponseDto<Void>> unhideMessage(@PathVariable Long messageId,
                                                              @RequestHeader("X-User-Id") Long userId) {
        chatMessageService.unhideMessage(messageId, userId);
        ApiResponseDto<Void> body = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("메시지 가리기를 해제했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }
}
