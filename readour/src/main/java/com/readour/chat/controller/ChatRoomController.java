package com.readour.chat.controller;

import com.readour.chat.dto.request.RoomCreateRequest;
import com.readour.chat.dto.response.ChatPageOverviewResponse;
import com.readour.chat.dto.response.RoomCreateResponse;
import com.readour.chat.dto.response.RoomListPageResponse;
import com.readour.chat.service.ChatRoomService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
@SecurityRequirement(name = "bearerAuth")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 목록(내 채팅방 + 공개 채팅방) / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초기 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatPageOverviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/overview")
    public ResponseEntity<ApiResponseDto<ChatPageOverviewResponse>> getOverview(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                @RequestParam(required = false) String myQuery,
                                                                                @RequestParam(required = false) Integer myPage,
                                                                                @RequestParam(required = false) Integer mySize,
                                                                                @RequestParam(required = false) String publicQuery,
                                                                                @RequestParam(required = false) Integer publicPage,
                                                                                @RequestParam(required = false) Integer publicSize) {
        Long userId = requireUserId(userPrincipal);
        ChatPageOverviewResponse overview = chatRoomService.getChatPageOverview(
                userId,
                myQuery,
                myPage,
                mySize,
                publicQuery,
                publicPage,
                publicSize
        );

        ApiResponseDto<ChatPageOverviewResponse> body = ApiResponseDto.<ChatPageOverviewResponse>builder()
                .status(HttpStatus.OK.value())
                .body(overview)
                .message("채팅 페이지 데이터를 조회했습니다.")
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * 정상동작 확인 완료
     *
     * @param query 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 내 채팅방 목록
     */
    @Operation(summary = "내 채팅방 목록 조회 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomListPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<RoomListPageResponse>> getMyRooms(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                           @RequestParam(required = false) String query,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = requireUserId(userPrincipal);
        RoomListPageResponse response = chatRoomService.getMyRooms(userId, query, page, size);
        ApiResponseDto<RoomListPageResponse> body = ApiResponseDto.<RoomListPageResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("채팅방 목록을 조회했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    /**
     * 정상 동작 확인 완료
     *
     * @param request 채팅방 개설에 필요한 dto
     * @return 생성 결과
     */
    @Operation(summary = "채팅방 개설 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = RoomCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<RoomCreateResponse>> createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RoomCreateRequest request) {

        Long creatorId = requireUserId(userPrincipal);
        RoomCreateResponse response = chatRoomService.createRoom(creatorId, request);

        ApiResponseDto<RoomCreateResponse> body = ApiResponseDto.<RoomCreateResponse>builder()
                .status(HttpStatus.CREATED.value())
                .body(response)
                .message("채팅방이 생성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    private Long requireUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 존재하지 않습니다.");
        }
        return userPrincipal.getId();
    }
}
