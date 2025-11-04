package com.readour.chat.controller;

import com.readour.chat.dto.request.BulkLeaveRoomsRequest;
import com.readour.chat.dto.request.DestroyRoomRequest;
import com.readour.chat.dto.request.JoinRoomRequest;
import com.readour.chat.dto.request.KickMemberRequest;
import com.readour.chat.dto.request.LeaveRoomRequest;
import com.readour.chat.dto.request.PinReorderRequest;
import com.readour.chat.dto.request.PinRoomRequest;
import com.readour.chat.dto.response.RoomListPageResponse;
import com.readour.chat.dto.response.RoomMemberProfileResponse;
import com.readour.chat.service.ChatRoomMemberService;
import com.readour.chat.service.ChatRoomService;
import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatRoomService chatRoomService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    @Operation(summary = "방 나가기 / 구현 및 테스트 완료", description = "멤버/매니저만 방 나가기 가능, 방장은 폭파를 사용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 나가기 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> leave(@PathVariable Long roomId,
                                                                      @Validated @RequestBody LeaveRoomRequest request) {
        chatRoomMemberService.leaveRoom(roomId, request.getUserId());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방을 나갔습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "방 폭파 / 구현 및 테스트 완료", description = "방장만 수행 가능, 수행 시 모든 멤버 비활성화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 폭파 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/destroy")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> destroy(@PathVariable Long roomId,
                                                                        @Validated @RequestBody DestroyRoomRequest request) {
        chatRoomMemberService.destroyRoom(roomId, request.getOwnerId(), request.getConfirmDestroy());
        RoomListPageResponse rooms = fetchMyRooms(request.getOwnerId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방이 삭제되었습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 참여 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 참여 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> join(@PathVariable Long roomId,
                                                                     @Valid @RequestBody JoinRoomRequest request) {
        chatRoomMemberService.joinRoom(roomId, request.getUserId());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방에 참여했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 일괄 나가기 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일괄 나가기 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/leave/bulk")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> leaveBulk(@Valid @RequestBody BulkLeaveRoomsRequest request) {
        chatRoomMemberService.leaveRooms(request.getUserId(), request.getRoomIds());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("선택한 채팅방에서 나갔습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 핀 설정 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "핀 설정 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/pin")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> pin(@PathVariable Long roomId,
                                                                    @Valid @RequestBody PinRoomRequest request) {
        chatRoomMemberService.pinRoom(roomId, request.getUserId());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방을 상단에 고정했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 강퇴")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "강퇴 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{roomId}/kick")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> kick(@PathVariable Long roomId,
                                                                     @Valid @RequestBody KickMemberRequest request) {
        chatRoomMemberService.kickMember(roomId, request.getUserId(), request.getTargetUserId(), request.getReason());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("사용자를 강퇴했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 멤버 프로필 조회 / 구현 완료 예외 처리 추가 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomMemberProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{roomId}/members/{targetUserId}")
    public ResponseEntity<ApiResponseDto<RoomMemberProfileResponse>> getMemberProfile(@PathVariable Long roomId,
                                                                                      @PathVariable Long targetUserId,
                                                                                      @RequestParam("requesterId") Long requesterId) {
        RoomMemberProfileResponse profile = chatRoomMemberService.getMemberProfile(roomId, requesterId, targetUserId);

        ApiResponseDto<RoomMemberProfileResponse> body = ApiResponseDto.<RoomMemberProfileResponse>builder()
                .status(HttpStatus.OK.value())
                .body(profile)
                .message("사용자 프로필을 조회했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅방 핀 해제 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "핀 해제 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{roomId}/pin")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> unpin(@PathVariable Long roomId,
                                                                      @Valid @RequestBody PinRoomRequest request) {
        chatRoomMemberService.unpinRoom(roomId, request.getUserId());
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방 상단 고정을 해제했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "핀 순서 변경 / 일단 미사용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "핀 순서 변경 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/pins/reorder")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> reorderPins(@Valid @RequestBody PinReorderRequest request) {
        Map<Long, Integer> desiredOrders = toOrderMap(request);
        chatRoomMemberService.reorderPins(request.getUserId(), desiredOrders);
        RoomListPageResponse rooms = fetchMyRooms(request.getUserId(), request.getQuery(), request.getPage(), request.getSize());

        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(rooms)
                .message("채팅방 핀 순서를 변경했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    private Map<Long, Integer> toOrderMap(PinReorderRequest request) {
        Map<Long, Integer> orders = new LinkedHashMap<>();
        for (PinReorderRequest.Order order : request.getOrders()) {
            Long roomId = order.getRoomId();
            Integer value = order.getPinOrder();
            if (orders.putIfAbsent(roomId, value) != null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "orders에 중복된 roomId가 포함되어 있습니다.");
            }
        }
        return orders;
    }

    private RoomListPageResponse fetchMyRooms(Long userId, String query, Integer page, Integer size) {
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId는 필수입니다.");
        }
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : size;
        return chatRoomService.getMyRooms(userId, query, resolvedPage, resolvedSize);
    }
}
