package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.MainPageResponseDto;
import com.readour.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Main Page", description = "메인 페이지 API")
@RestController
@RequestMapping("/api/main-page")
@RequiredArgsConstructor
public class MainPageController {

    private final CommunityService communityService;

    @Operation(summary = "메인 페이지 데이터 조회",
            description = "메인 페이지에 필요한 모든 데이터(인기글, 모집글, 추천도서)를 조회합니다. (비회원/회원 겸용)")
    @GetMapping
    public ResponseEntity<ApiResponseDto<MainPageResponseDto>> getMainPageData(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MainPageResponseDto mainPageData = communityService.getMainPageData(currentUser);

        return ResponseEntity.ok(ApiResponseDto.<MainPageResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(mainPageData)
                .message("메인 페이지 데이터 조회 성공")
                .build());
    }
}