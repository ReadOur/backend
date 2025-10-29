package com.readour.chat.controller;

import com.readour.chat.dto.request.DestroyRoomRequest;
import com.readour.chat.dto.request.LeaveRoomRequest;
import com.readour.chat.service.ChatRoomMemberService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;

    @Operation(
            summary = "방 나가기",
            description = "채팅방 내 역할이 member, manager인 경우에만 사용이 가능함 owner의 경우 방 나가기가 아닌 방 폭파를 해야함"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 나가기 성공",
                    content = @Content(schema = @Schema(implementation = void.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponseDto<Void>> leave(@PathVariable Long roomId,
                                                      @Validated @RequestBody LeaveRoomRequest request) {
        chatRoomMemberService.leaveRoom(roomId, request.getUserId());

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("채팅방을 나갔습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "방 폭파",
            description = "owner가 권한 위임을 하지 않고 방을 나가면, 해당 방이 폭파(더 이상 이용 불가능)됨. 프론트에서 경고 메시지 주기"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 폭파 성공",
                    content = @Content(schema = @Schema(implementation = void.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/destroy")
    public ResponseEntity<ApiResponseDto<Void>> destroy(@PathVariable Long roomId,
                                                        @Validated @RequestBody DestroyRoomRequest request) {
        chatRoomMemberService.destroyRoom(roomId, request.getOwnerId(), request.getConfirmDestroy());

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("채팅방이 삭제되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }
}
