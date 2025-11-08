package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.community.dto.RegionDto;
import com.readour.community.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Regions", description = "지역 코드 조회 헬퍼 API")
@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Validated
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "이름으로 지역 검색",
            description = "이름(예: '강남', '서울', '종로')으로 지역 코드/이름을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지역 검색 성공 (결과가 없으면 빈 리스트)"),
            @ApiResponse(responseCode = "400", description = "검색어(name)가 누락됨",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<List<RegionDto>>> searchRegions(
            @RequestParam @NotBlank(message = "검색어(name)는 필수입니다.") String name
    ) {
        List<RegionDto> regions = regionService.searchRegionsByName(name);
        return ResponseEntity.ok(apiResponse(regions, "지역 검색 성공"));
    }

    @Operation(summary = "광역시/도 (대분류) 목록 조회",
            description = "도서관 검색 시 사용할 대분류 지역 코드/이름 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대분류 지역 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<RegionDto>>> getMainRegions() {
        List<RegionDto> regions = regionService.getMainRegions();
        return ResponseEntity.ok(apiResponse(regions, "대분류 지역 목록 조회 성공"));
    }

    @Operation(summary = "시/군/구 (세부) 목록 조회",
            description = "특정 광역시/도(regionCode)에 속한 세부 지역 코드/이름 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세부 지역 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 상위 지역 코드",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{parentCode}/details")
    public ResponseEntity<ApiResponseDto<List<RegionDto>>> getDetailedRegions(
            @PathVariable String parentCode
    ) {
        List<RegionDto> regions = regionService.getDetailedRegions(parentCode);
        return ResponseEntity.ok(apiResponse(regions, "세부 지역 목록 조회 성공"));
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