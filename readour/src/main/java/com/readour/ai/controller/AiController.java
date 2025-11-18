package com.readour.ai.controller;

import com.readour.ai.dto.AiCommandRequest;
import com.readour.ai.dto.AiJobResponse;
import com.readour.ai.service.AiService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms/{roomId}/ai")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "채팅방 AI 명령 실행", description = "공개방 요약, 모임방 토론 질문/요점/마감문 생성 명령을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 작업 완료",
                    content = @Content(schema = @Schema(implementation = AiJobResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "리소스 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponseDto<AiJobResponse>> runCommand(@PathVariable Long roomId,
                                                                    @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                    @Valid @RequestBody AiCommandRequest request) {
        Long requesterId = requireUserId(userPrincipal);
        AiJobResponse result = aiService.executeRoomCommand(roomId, requesterId, request);

        ApiResponseDto<AiJobResponse> response = ApiResponseDto.<AiJobResponse>builder()
                .status(HttpStatus.OK.value())
                .body(result)
                .message("AI 작업을 완료했습니다.")
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
