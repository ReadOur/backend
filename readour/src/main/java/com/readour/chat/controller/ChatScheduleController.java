package com.readour.chat.controller;

import com.readour.chat.dto.request.ChatScheduleCreateRequest;
import com.readour.chat.dto.request.ChatScheduleUpdateRequest;
import com.readour.chat.dto.response.ChatScheduleResponse;
import com.readour.chat.service.ChatScheduleService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms/{roomId}/schedules")
@SecurityRequirement(name = "bearerAuth")
public class ChatScheduleController {

    private final ChatScheduleService chatScheduleService;

    @Operation(summary = "채팅 일정 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<ChatScheduleResponse>> createSchedule(@PathVariable Long roomId,
                                                                               @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                               @Validated @RequestBody ChatScheduleCreateRequest request) {
        Long userId = requireUserId(userPrincipal);
        ChatScheduleResponse response = chatScheduleService.createSchedule(roomId, userId, request);
        ApiResponseDto<ChatScheduleResponse> body = ApiResponseDto.<ChatScheduleResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("일정을 생성했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 일정 참여 추가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 참여 추가 성공",
                    content = @Content(schema = @Schema(implementation = ChatScheduleResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{scheduleId}/participants")
    public ResponseEntity<ApiResponseDto<ChatScheduleResponse>> addParticipant(@PathVariable Long roomId,
                                                                               @PathVariable Long scheduleId,
                                                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = requireUserId(userPrincipal);
        ChatScheduleResponse response = chatScheduleService.addParticipant(roomId, scheduleId, userId);
        ApiResponseDto<ChatScheduleResponse> body = ApiResponseDto.<ChatScheduleResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("일정에 참여했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 일정 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 수정 성공",
                    content = @Content(schema = @Schema(implementation = ChatScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDto<ChatScheduleResponse>> updateSchedule(@PathVariable Long roomId,
                                                                               @PathVariable Long scheduleId,
                                                                               @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                               @Validated @RequestBody ChatScheduleUpdateRequest request) {
        Long userId = requireUserId(userPrincipal);
        ChatScheduleResponse response = chatScheduleService.updateSchedule(roomId, userId, scheduleId, request);
        ApiResponseDto<ChatScheduleResponse> body = ApiResponseDto.<ChatScheduleResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("일정을 수정했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 일정 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteSchedule(@PathVariable Long roomId,
                                                               @PathVariable Long scheduleId,
                                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = requireUserId(userPrincipal);
        chatScheduleService.deleteSchedule(roomId, userId, scheduleId);
        ApiResponseDto<Void> body = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("일정을 삭제했습니다.")
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
