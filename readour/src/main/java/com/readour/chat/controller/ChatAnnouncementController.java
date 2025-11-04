package com.readour.chat.controller;

import com.readour.chat.dto.request.ChatAnnouncementCreateRequest;
import com.readour.chat.dto.request.ChatAnnouncementUpdateRequest;
import com.readour.chat.dto.response.ChatAnnouncementListResponse;
import com.readour.chat.dto.response.ChatAnnouncementResponse;
import com.readour.chat.service.ChatAnnouncementService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms/{roomId}/announcements")
public class ChatAnnouncementController {

    private final ChatAnnouncementService chatAnnouncementService;

    @Operation(summary = "채팅 공지 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatAnnouncementResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<ChatAnnouncementResponse>> createAnnouncement(@PathVariable Long roomId,
                                                                                       @RequestHeader("X-User-Id") Long userId,
                                                                                       @Validated @RequestBody ChatAnnouncementCreateRequest request) {
        ChatAnnouncementResponse response = chatAnnouncementService.createAnnouncement(roomId, userId, request);
        ApiResponseDto<ChatAnnouncementResponse> body = ApiResponseDto.<ChatAnnouncementResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("공지를 생성했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 공지 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatAnnouncementListResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<ChatAnnouncementListResponse>> getAnnouncements(@PathVariable Long roomId,
                                                                                         @RequestHeader("X-User-Id") Long userId,
                                                                                         @RequestParam(required = false) Integer page,
                                                                                         @RequestParam(required = false) Integer size) {
        ChatAnnouncementListResponse response = chatAnnouncementService.getAnnouncements(roomId, userId, page, size);
        ApiResponseDto<ChatAnnouncementListResponse> body = ApiResponseDto.<ChatAnnouncementListResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("공지 목록을 조회했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 공지 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatAnnouncementResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{announcementId}")
    public ResponseEntity<ApiResponseDto<ChatAnnouncementResponse>> getAnnouncement(@PathVariable Long roomId,
                                                                                    @PathVariable Long announcementId,
                                                                                    @RequestHeader("X-User-Id") Long userId) {
        ChatAnnouncementResponse response = chatAnnouncementService.getAnnouncement(roomId, userId, announcementId);
        ApiResponseDto<ChatAnnouncementResponse> body = ApiResponseDto.<ChatAnnouncementResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("공지를 조회했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 공지 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 수정 성공",
                    content = @Content(schema = @Schema(implementation = ChatAnnouncementResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{announcementId}")
    public ResponseEntity<ApiResponseDto<ChatAnnouncementResponse>> updateAnnouncement(@PathVariable Long roomId,
                                                                                       @PathVariable Long announcementId,
                                                                                       @RequestHeader("X-User-Id") Long userId,
                                                                                       @Validated @RequestBody ChatAnnouncementUpdateRequest request) {
        ChatAnnouncementResponse response = chatAnnouncementService.updateAnnouncement(roomId, userId, announcementId, request);
        ApiResponseDto<ChatAnnouncementResponse> body = ApiResponseDto.<ChatAnnouncementResponse>builder()
                .status(HttpStatus.OK.value())
                .body(response)
                .message("공지를 수정했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "채팅 공지 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAnnouncement(@PathVariable Long roomId,
                                                                   @PathVariable Long announcementId,
                                                                   @RequestHeader("X-User-Id") Long userId) {
        chatAnnouncementService.deleteAnnouncement(roomId, userId, announcementId);
        ApiResponseDto<Void> body = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("공지를 삭제했습니다.")
                .build();
        return ResponseEntity.ok(body);
    }
}
