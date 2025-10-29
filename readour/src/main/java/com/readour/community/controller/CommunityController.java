package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.community.dto.*;
import com.readour.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // (SD-19: 게시글 좋아요)
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponseDto<Map<String,Boolean>>> likePost(@PathVariable Long postId) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        boolean isLiked = communityService.toggleLike(postId, currentUserId);

        ApiResponseDto<Map<String, Boolean>> response = ApiResponseDto.<Map<String, Boolean>>builder()
                .status(HttpStatus.OK.value())
                .body(Map.of("liked", isLiked))
                .message(isLiked ? "게시글 좋아요 성공" : "게시글 좋아요 취소 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    // (SD-11: 게시글 작성)
    @PostMapping("/posts")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> createPost(@RequestBody PostCreateRequestDto requestDto) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID

        PostResponseDto createdPost = communityService.createPost(requestDto, currentUserId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.CREATED.value()) // 201
                .body(createdPost)
                .message("게시글이 성공적으로 생성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (SD-10: 게시글 조회)
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> getPostDetail(@PathVariable Long postId) {
        PostResponseDto postDetail = communityService.getPostDetail(postId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.OK.value()) // 200
                .body(postDetail)
                .message("게시글 상세 조회 성공")
                .build();

        return ResponseEntity.ok(response); // .ok() is shorthand for status(HttpStatus.OK)
    }

    // (SD-09: 게시글 목록 조회)
    @GetMapping("/posts")
    public ResponseEntity<ApiResponseDto<Page<PostSummaryDto>>> getPostList(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<PostSummaryDto> postPage = communityService.getPostList(pageable);

        ApiResponseDto<Page<PostSummaryDto>> response = ApiResponseDto.<Page<PostSummaryDto>>builder()
                .status(HttpStatus.OK.value()) // 200
                .body(postPage)
                .message("게시글 목록 조회 성공")
                .build();

        return ResponseEntity.ok(response);
    }

    // (SD-12: 게시글 수정)
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> updatePost(@PathVariable Long postId,
                                                                      @RequestBody PostUpdateRequestDto requestDto) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        PostResponseDto updatedPost = communityService.updatePost(postId, requestDto, currentUserId);

        ApiResponseDto<PostResponseDto> response = ApiResponseDto.<PostResponseDto>builder()
                .status(HttpStatus.OK.value()) // 200
                .body(updatedPost)
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    // (SD-13: 게시글 삭제)
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePost(@PathVariable Long postId) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        communityService.deletePost(postId, currentUserId);

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value()) // Or HttpStatus.NO_CONTENT.value() (204) if preferred
                .body(null) // No data body for delete
                .message("게시글이 성공적으로 삭제되었습니다.")
                .build();

        // If using 204, return ResponseEntity.noContent().build(); is simpler,
        // but if you want the consistent wrapper, use 200 OK.
        return ResponseEntity.ok(response);
    }

    // --- Comment API ---

    // (SD-14: 댓글 작성)
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> addComment(@PathVariable Long postId,
                                                                         @RequestBody CommentCreateRequestDto requestDto) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        CommentResponseDto createdComment = communityService.addComment(postId, requestDto, currentUserId);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .status(HttpStatus.CREATED.value()) // 201
                .body(createdComment)
                .message("댓글이 성공적으로 작성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (SD-15: 댓글 수정)
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(@PathVariable Long commentId,
                                                                            @RequestBody CommentUpdateRequestDto requestDto) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        CommentResponseDto updatedComment = communityService.updateComment(commentId, requestDto, currentUserId);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .status(HttpStatus.OK.value()) // 200
                .body(updatedComment)
                .message("댓글이 성공적으로 수정되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    // (SD-16: 댓글 삭제)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(@PathVariable Long commentId) {
        Long currentUserId = 1L; // TODO: Replace with actual authenticated user ID
        communityService.deleteComment(commentId, currentUserId);

        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value()) // Or 204
                .body(null)
                .message("댓글이 성공적으로 삭제되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }
}