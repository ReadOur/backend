package com.readour.chat.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/messages")
@SecurityRequirement(name = "bearerAuth")
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
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = requireUserId(userPrincipal);
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
                                                              @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = requireUserId(userPrincipal);
        chatMessageService.unhideMessage(messageId, userId);
        ApiResponseDto<Void> body = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("메시지 가리기를 해제했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    private Long requireUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 존재하지 않습니다.");
        }
        return userPrincipal.getId();
    }
}
