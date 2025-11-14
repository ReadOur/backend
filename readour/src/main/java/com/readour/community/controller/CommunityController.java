package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.*;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.PostSearchType;
import com.readour.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CommunityController {

    private final CommunityService communityService;

    private Long getAuthenticatedUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userPrincipal.getId();
    }

    // (SD-18: 게시글 검색)
    @Operation(summary = "게시글 검색")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 검색 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))), // Page<PostSummaryDto>
            @ApiResponse(responseCode = "400", description = "잘못된 검색 타입 또는 정렬 파라미터",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/posts/search")
    public ResponseEntity<ApiResponseDto<Page<PostSummaryDto>>> searchPosts(
            @RequestParam PostSearchType type,
            @RequestParam String keyword,
            @RequestParam(required = false) PostCategory category,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = (userPrincipal != null) ? userPrincipal.getId() : null;
        Page<PostSummaryDto> postPage = communityService.searchPosts(type, keyword, category, pageable, currentUserId);

        ApiResponseDto<Page<PostSummaryDto>> response = ApiResponseDto.<Page<PostSummaryDto>>builder()
                .status(HttpStatus.OK.value())
                .body(postPage)
                .message("게시글 검색 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    // (SD-19: 게시글 좋아요)
    @Operation(summary = "게시글 좋아요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요/취소 성공. 'liked: true'는 좋아요가 추가된 상태.",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponseDto<Map<String,Boolean>>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        boolean isLiked = communityService.toggleLike(postId, currentUserId);

        ApiResponseDto<Map<String, Boolean>> response = ApiResponseDto.<Map<String, Boolean>>builder()
                .status(HttpStatus.OK.value())
                .body(Map.of("liked", isLiked))
                .message(isLiked ? "게시글 좋아요 성공" : "게시글 좋아요 취소 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    // (SD-11: 게시글 작성)
    @Operation(summary = "게시글 작성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "작성자 또는 관련 도서를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/posts")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> createPost(
            @RequestBody PostCreateRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);

        PostResponseDto createdPost = communityService.createPost(requestDto, currentUserId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(createdPost)
                .message("게시글이 성공적으로 생성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (SD-10: 게시글 조회)
    @Operation(summary = "게시글 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = (userPrincipal != null) ? userPrincipal.getId() : null;
        PostResponseDto postDetail = communityService.getPostDetail(postId, currentUserId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(postDetail)
                .message("게시글 상세 조회 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    // 게시글 조회수 증가 API
    @Operation(summary = "게시글 조회수 증가",
            description = "프론트엔드에서 게시글 상세 페이지 진입 시 1회만 호출해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회수 증가 성공 (게시글이 없어도 200 반환)")
    })
    @PostMapping("/posts/{postId}/view")
    public ResponseEntity<ApiResponseDto<Void>> incrementPostView(@PathVariable Long postId) {
        communityService.incrementPostHit(postId);

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("조회수가 갱신되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    // (SD-09: 게시글 목록 조회)
    @Operation(summary = "게시글 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))) // Page<PostSummaryDto>
    })
    @GetMapping("/posts")
    public ResponseEntity<ApiResponseDto<Page<PostSummaryDto>>> getPostList(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) PostCategory category,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = (userPrincipal != null) ? userPrincipal.getId() : null;

        Page<PostSummaryDto> postPage = communityService.getPostList(pageable, currentUserId, category);

        ApiResponseDto<Page<PostSummaryDto>> response = ApiResponseDto.<Page<PostSummaryDto>>builder()
                .status(HttpStatus.OK.value())
                .body(postPage)
                .message("게시글 목록 조회 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    // (SD-12: 게시글 수정)
    @Operation(summary = "게시글 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한이 없음 (작성자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글 또는 관련 도서를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        PostResponseDto updatedPost = communityService.updatePost(postId, requestDto, currentUserId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(updatedPost)
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    // (SD-13: 게시글 삭제)
    @Operation(summary = "게시글 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공 (Soft delete)",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "삭제 권한이 없음 (작성자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        communityService.deletePost(postId, currentUserId);

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("게시글이 성공적으로 삭제되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    // --- Comment API ---

    // (SD-14: 댓글 작성)
    @Operation(summary = "댓글 작성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글 또는 작성자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> addComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        CommentResponseDto createdComment = communityService.addComment(postId, requestDto, currentUserId);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(createdComment)
                .message("댓글이 성공적으로 작성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (SD-15: 댓글 수정)
    @Operation(summary = "댓글 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한이 없음 (작성자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        CommentResponseDto updatedComment = communityService.updateComment(commentId, requestDto, currentUserId);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(updatedComment)
                .message("댓글이 성공적으로 수정되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    // (SD-16: 댓글 삭제)
    @Operation(summary = "댓글 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공 (Soft delete)",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "삭제 권한이 없음 (작성자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = getAuthenticatedUserId(userPrincipal);
        communityService.deleteComment(commentId, currentUserId);

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .body(null)
                .message("댓글이 성공적으로 삭제되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }
}
