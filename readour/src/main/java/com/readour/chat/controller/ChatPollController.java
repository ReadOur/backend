package com.readour.chat.controller;

import com.readour.chat.dto.request.ChatPollCreateRequest;
import com.readour.chat.dto.request.ChatPollVoteRequest;
import com.readour.chat.dto.response.ChatPollResponse;
import com.readour.chat.dto.response.ChatPollResultResponse;
import com.readour.chat.service.ChatPollService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms/{roomId}/polls")
public class ChatPollController {

    private final ChatPollService chatPollService;

    @Operation(summary = "채팅 투표 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "투표 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatPollResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<ChatPollResponse>> createPoll(@PathVariable Long roomId,
                                                                       @RequestHeader("X-User-Id") Long userId,
                                                                       @Validated @RequestBody ChatPollCreateRequest request) {
        ChatPollResponse response = chatPollService.createPoll(roomId, userId, request);
        ApiResponseDto<ChatPollResponse> body = ApiResponseDto.<ChatPollResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("투표를 생성했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 투표 참여 또는 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "투표 참여/수정 성공",
                    content = @Content(schema = @Schema(implementation = ChatPollResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{pollId}/votes")
    public ResponseEntity<ApiResponseDto<ChatPollResultResponse>> vote(@PathVariable Long roomId,
                                                                       @PathVariable Long pollId,
                                                                       @RequestHeader("X-User-Id") Long userId,
                                                                       @Validated @RequestBody ChatPollVoteRequest request) {
        ChatPollResultResponse response = chatPollService.vote(roomId, pollId, userId, request);
        ApiResponseDto<ChatPollResultResponse> body = ApiResponseDto.<ChatPollResultResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("투표를 반영했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 투표 결과 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "투표 결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatPollResultResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{pollId}/results")
    public ResponseEntity<ApiResponseDto<ChatPollResultResponse>> getResult(@PathVariable Long roomId,
                                                                            @PathVariable Long pollId,
                                                                            @RequestHeader("X-User-Id") Long userId) {
        ChatPollResultResponse response = chatPollService.getResult(roomId, pollId, userId);
        ApiResponseDto<ChatPollResultResponse> body = ApiResponseDto.<ChatPollResultResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("투표 결과를 조회했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }
}
