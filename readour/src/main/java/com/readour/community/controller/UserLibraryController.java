package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.LibraryRegistrationRequestDto;
import com.readour.community.dto.LibrarySearchResponseDto;
import com.readour.community.dto.UserLibraryResponseDto;
import com.readour.community.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Libraries", description = "사용자 선호 도서관 관리 API")
@RestController
@RequestMapping("/api/user/libraries")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class UserLibraryController {

    private final BookService bookService;

    private Long getAuthenticatedUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userPrincipal.getId();
    }

    @Operation(summary = "선호 도서관 등록을 위한 도서관 검색",
            description = "지역 코드를 기준으로 공공 도서관 목록을 검색합니다. (API #1)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "필수 파라미터(region) 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "502", description = "외부 도서관 API 호출 실패 (Bad Gateway)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<LibrarySearchResponseDto>>> searchLibraries(
            @RequestParam @NotBlank(message = "지역 코드는 필수입니다.")
            @Schema(description = "지역 코드 (예: '11' 서울)", example = "11")
            String region,

            @RequestParam(required = false)
            @Schema(description = "세부 지역 코드 (예: '11010' 종로구)", example = "11010")
            String dtlRegion,

            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<LibrarySearchResponseDto> libraryPage = bookService.searchLibraries(region, dtlRegion, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<LibrarySearchResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .body(libraryPage)
                .message("도서관 목록 검색 성공")
                .build());
    }

    @Operation(summary = "사용자 선호 도서관 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문 유효성 검사 실패 (코드/이름 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "등록 개수 초과 (최대 3개)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 등록된 도서관 (데이터 충돌)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserLibraryResponseDto>> registerLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody LibraryRegistrationRequestDto requestDto
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        UserLibraryResponseDto responseDto = bookService.registerInterestedLibrary(userId, requestDto.getLibraryCode(), requestDto.getLibraryName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<UserLibraryResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(responseDto)
                .message("선호 도서관이 등록되었습니다.")
                .build());
    }

    @Operation(summary = "사용자 선호 도서관 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "삭제할 도서관이 등록 목록에 없음 (Not Found)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{libraryCode}")
    public ResponseEntity<ApiResponseDto<Void>> deleteLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String libraryCode
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        bookService.deleteInterestedLibrary(userId, libraryCode);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("선호 도서관이 삭제되었습니다.")
                .build());
    }

    @Operation(summary = "사용자 선호 도서관 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (등록된 도서관이 없으면 빈 리스트 반환)")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserLibraryResponseDto>>> getLibraries(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = getAuthenticatedUserId(userPrincipal);
        List<UserLibraryResponseDto> libraries = bookService.getInterestedLibraries(userId);
        return ResponseEntity.ok(ApiResponseDto.<List<UserLibraryResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .body(libraries)
                .message("선호 도서관 목록 조회 성공")
                .build());
    }
}