package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.community.dto.MyHighlightDto;
import com.readour.community.dto.MyLibraryResponseDto;
import com.readour.community.dto.MyReviewDto;
import com.readour.community.dto.MyWishlistDto;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Library", description = "내 서재 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyLibraryController {

    private final MyLibraryService myLibraryService;

    @Operation(summary = "내 서재 조회",
            description = "현재 로그인한 사용자의 서재 정보(위시리스트, 리뷰, 하이라이트의 최근 N개)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/my-library")
    public ResponseEntity<ApiResponseDto<MyLibraryResponseDto>> getMyLibrary(
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
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

    @Operation(summary = "특정 사용자 서재 - 위시리스트 조회 (페이징)",
            description = "특정 사용자(userId)의 위시리스트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/wishlist")
    public ResponseEntity<ApiResponseDto<Page<MyWishlistDto>>> getUserWishlist(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<MyWishlistDto> wishlistPage = myLibraryService.getWishlist(userId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<MyWishlistDto>>builder()
                .status(HttpStatus.OK.value())
                .body(wishlistPage)
                .message("사용자 위시리스트 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 - 리뷰 조회 (페이징)",
            description = "특정 사용자(userId)의 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/reviews")
    public ResponseEntity<ApiResponseDto<Page<MyReviewDto>>> getUserReviews(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<MyReviewDto> reviewPage = myLibraryService.getReviews(userId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<MyReviewDto>>builder()
                .status(HttpStatus.OK.value())
                .body(reviewPage)
                .message("사용자 리뷰 목록 조회 성공")
                .build());
    }

    @Operation(summary = "특정 사용자 서재 - 하이라이트 조회 (페이징)",
            description = "특정 사용자(userId)의 하이라이트 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/library/highlights")
    public ResponseEntity<ApiResponseDto<Page<MyHighlightDto>>> getUserHighlights(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<MyHighlightDto> highlightPage = myLibraryService.getHighlights(userId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<MyHighlightDto>>builder()
                .status(HttpStatus.OK.value())
                .body(highlightPage)
                .message("사용자 하이라이트 목록 조회 성공")
                .build());
    }
}