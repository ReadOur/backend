package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.*;
import com.readour.community.service.MyLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Library", description = "내 서재 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyLibraryController {

    private final MyLibraryService myLibraryService;

    private Long getAuthenticatedUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userPrincipal.getId();
    }

    @Operation(summary = "내 서재 조회",
            description = "현재 로그인한 사용자의 서재 정보(위시리스트, 리뷰, 하이라이트의 최근 N개)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-library")
    public ResponseEntity<ApiResponseDto<MyLibraryResponseDto>> getMyLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyLibraryResponseDto libraryData = myLibraryService.getMyLibraryData(userId);
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(libraryData)
                .message("내 서재 정보 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 조회",
            description = "특정 사용자(userId)의 서재 정보(위시리스트, 리뷰, 하이라이트의 최근 N개)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/users/{userId}/library")
    public ResponseEntity<ApiResponseDto<MyLibraryResponseDto>> getUserLibrary(
            @PathVariable Long userId
    ) {
        MyLibraryResponseDto libraryData = myLibraryService.getMyLibraryData(userId);
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(libraryData)
                .message("사용자 서재 정보 조회 성공")
                .build());
    }

    @Operation(summary = "내 서재 - 위시리스트 조회 (페이징)",
            description = "내 위시리스트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-library/wishlist")
    public ResponseEntity<ApiResponseDto<MyLibraryWishlistPageDto>> getMyWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyLibraryWishlistPageDto wishlistPage = myLibraryService.getWishlist(userId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryWishlistPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(wishlistPage)
                .message("사용자 위시리스트 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 - 위시리스트 조회 (페이징)",
            description = "특정 사용자(userId)의 위시리스트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/wishlist")
    public ResponseEntity<ApiResponseDto<MyLibraryWishlistPageDto>> getUserWishlist(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        MyLibraryWishlistPageDto wishlistPage = myLibraryService.getWishlist(userId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryWishlistPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(wishlistPage)
                .message("사용자 위시리스트 조회 성공")
                .build());
    }

    @Operation(summary = "내 서재 - 리뷰 조회 (페이징)",
            description = "내 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-library/reviews")
    public ResponseEntity<ApiResponseDto<MyLibraryReviewPageDto>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyLibraryReviewPageDto reviewPage = myLibraryService.getReviews(userId, pageable); // 👈 [3]
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryReviewPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(reviewPage)
                .message("사용자 리뷰 목록 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 - 리뷰 조회 (페이징)",
            description = "특정 사용자(userId)의 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/reviews")
    public ResponseEntity<ApiResponseDto<MyLibraryReviewPageDto>> getUserReviews(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        MyLibraryReviewPageDto reviewPage = myLibraryService.getReviews(userId, pageable); // 👈 [3]
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryReviewPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(reviewPage)
                .message("사용자 리뷰 목록 조회 성공")
                .build());
    }

    @Operation(summary = "내 서재 - 하이라이트 조회 (페이징)",
            description = "내 하이라이트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-library/highlights")
    public ResponseEntity<ApiResponseDto<MyLibraryHighlightPageDto>> getMyHighlights(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        MyLibraryHighlightPageDto highlightPage = myLibraryService.getHighlights(userId, pageable); // 👈 [3]
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryHighlightPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(highlightPage)
                .message("사용자 하이라이트 목록 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 - 하이라이트 조회 (페이징)",
            description = "특정 사용자(userId)의 하이라이트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/highlights")
    public ResponseEntity<ApiResponseDto<MyLibraryHighlightPageDto>> getUserHighlights(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        MyLibraryHighlightPageDto highlightPage = myLibraryService.getHighlights(userId, pageable); // 👈 [3]
        return ResponseEntity.ok(ApiResponseDto.<MyLibraryHighlightPageDto>builder()
                .status(HttpStatus.OK.value())
                .body(highlightPage)
                .message("사용자 하이라이트 목록 조회 성공")
                .build());
    }
}