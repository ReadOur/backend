package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.*;
import com.readour.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Page", description = "마이페이지 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyPageController {

    private final CommunityService communityService;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private Long getAuthenticatedUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userPrincipal.getId();
    }

    @Operation(summary = "내 마이페이지 조회 (미리보기)",
            description = "현재 로그인한 사용자의 마이페이지 정보(작성글/댓글/좋아요글 최근 5개)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-page")
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPage(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyPageResponseDto myPageData = communityService.getMyPageData(userId);
        return ResponseEntity.ok(apiResponse(myPageData, "내 마이페이지 정보 조회 성공"));
    }

    @Operation(summary = "특정 사용자 마이페이지 조회 (미리보기)",
            description = "특정 사용자(userId)의 마이페이지 정보(최근 5개)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/users/{userId}/my-page")
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getUserPage(
            @PathVariable Long userId
    ) {
        MyPageResponseDto myPageData = communityService.getMyPageData(userId);
        return ResponseEntity.ok(apiResponse(myPageData, "사용자 마이페이지 정보 조회 성공"));
    }

    // --- '전체보기'를 위한 개별 페이징 API ---

    @Operation(summary = "내 작성 게시글 전체 조회 (페이징)")
    @GetMapping("/my-page/posts")
    public ResponseEntity<ApiResponseDto<MyPagePostsPageDto>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyPagePostsPageDto postPage = communityService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(apiResponse(postPage, "사용자 작성 게시글 조회 성공"));
    }

    @Operation(summary = "특정 사용자 - 작성 게시글 전체 조회 (페이징)")
    @GetMapping("/users/{userId}/my-page/posts")
    public ResponseEntity<ApiResponseDto<MyPagePostsPageDto>> getUserPosts(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MyPagePostsPageDto postPage = communityService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(apiResponse(postPage, "사용자 작성 게시글 조회 성공"));
    }

    @Operation(summary = "내 작성 댓글 전체 조회 (페이징)")
    @GetMapping("/my-page/comments")
    public ResponseEntity<ApiResponseDto<MyPageCommentsPageDto>> getMyComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyPageCommentsPageDto commentPage = communityService.getMyComments(userId, pageable);
        return ResponseEntity.ok(apiResponse(commentPage, "사용자 작성 댓글 조회 성공"));
    }

    @Operation(summary = "특정 사용자 - 작성 댓글 전체 조회 (페이징)")
    @GetMapping("/users/{userId}/my-page/comments")
    public ResponseEntity<ApiResponseDto<MyPageCommentsPageDto>> getUserComments(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MyPageCommentsPageDto commentPage = communityService.getMyComments(userId, pageable);
        return ResponseEntity.ok(apiResponse(commentPage, "사용자 작성 댓글 조회 성공"));
    }

    @Operation(summary = "내가 좋아요 누른 글 전체 조회 (페이징)")
    @GetMapping("/my-page/liked-posts")
    public ResponseEntity<ApiResponseDto<MyPageLikedPostsPageDto>> getMyLikedPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyPageLikedPostsPageDto likedPostPage = communityService.getLikedPosts(userId, pageable);
        return ResponseEntity.ok(apiResponse(likedPostPage, "사용자 좋아요 게시글 조회 성공"));
    }

    @Operation(summary = "특정 사용자 - 좋아요 누른 글 전체 조회 (페이징)")
    @GetMapping("/users/{userId}/my-page/liked-posts")
    public ResponseEntity<ApiResponseDto<MyPageLikedPostsPageDto>> getUserLikedPosts(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MyPageLikedPostsPageDto likedPostPage = communityService.getLikedPosts(userId, pageable);
        return ResponseEntity.ok(apiResponse(likedPostPage, "사용자 좋아요 게시글 조회 성공"));
    }

    // 공통 응답 래퍼
    private <T> ApiResponseDto<T> apiResponse(T body, String message) {
        return ApiResponseDto.<T>builder()
                .status(HttpStatus.OK.value())
                .body(body)
                .message(message)
                .build();
    }
}