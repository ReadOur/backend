package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.community.service.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Recruitment", description = "모임 모집 지원/취소 API")
@RestController
@RequestMapping("/api/community/recruitments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    private Long getAuthenticatedUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userPrincipal.getId();
    }

    @Operation(summary = "모임 지원 토글 (지원/취소)",
            description = "모집 중인 게시글(postId)에 참가 신청하거나 취소합니다. (좋아요/위시리스트와 동일한 방식)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공. 'isApplied: true'는 지원 완료 상태."),
            @ApiResponse(responseCode = "403", description = "모집 중이 아니거나, 생성자 본인일 경우"),
            @ApiResponse(responseCode = "404", description = "모집 글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "모집 인원이 마감됨 (Conflict)")
    })
    @PostMapping("/{postId}/apply-toggle")
    public ResponseEntity<ApiResponseDto<Map<String, Boolean>>> toggleRecruitment(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        boolean isApplied = recruitmentService.toggleRecruitment(postId, userId);

        String message = isApplied ? "모임 지원이 완료되었습니다." : "모임 지원이 취소되었습니다.";

        return ResponseEntity.ok(ApiResponseDto.<Map<String, Boolean>>builder()
                .status(HttpStatus.OK.value())
                .body(Map.of("isApplied", isApplied))
                .message(message)
                .build());
    }
}