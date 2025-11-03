package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.entity.Book;
import com.readour.community.dto.BookResponseDto;
import com.readour.community.dto.BookSummaryDto;
import com.readour.community.dto.BookSyncRequestDto;
import com.readour.community.service.BookService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "도서 검색 (외부 API) (SD-26)",
            description = "정보나루 API(#16)를 호출하여 키워드로 도서를 검색합니다. (DB 저장 X)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 검색 성공",
                    content = @Content(schema = @Schema(implementation = void.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<BookSummaryDto>>> searchBooks(
            @RequestParam String keyword,
            @ParameterObject Pageable pageable
    ) {
        Page<BookSummaryDto> bookPage = bookService.searchBooksFromApi(keyword, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<BookSummaryDto>>builder()
                .status(HttpStatus.OK.value())
                .body(bookPage)
                .message("도서 검색 성공")
                .build());
    }

    @Operation(summary = "도서 정보 동기화 (DB 저장)",
            description = "ISBN으로 DB를 조회하고, 없으면 외부 API(#6)에서 상세 정보를 가져와 DB에 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DB에서 도서 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = void.class))),
            @ApiResponse(responseCode = "201", description = "API에서 도서 정보를 가져와 DB에 저장 성공",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "ISBN이 요청 본문에 누락됨",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "API에서도 도서 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/sync")
    public ResponseEntity<ApiResponseDto<BookResponseDto>> syncBook(
            @Valid @RequestBody BookSyncRequestDto payload
    ) {
        String isbn = payload.getIsbn();

        if (isbn == null || isbn.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "ISBN is required");
        }

        // DB에 이미 있는지 확인 (findOrCreateBookByIsbn이 이 로직을 포함함)
        boolean existsInDb = bookService.isBookInDb(isbn);

        Book bookEntity = bookService.findOrCreateBookByIsbn(isbn);
        BookResponseDto responseDto = BookResponseDto.fromEntity(bookEntity);

        if (existsInDb) {
            // 이미 DB에 있었던 경우
            return ResponseEntity.ok(ApiResponseDto.<BookResponseDto>builder()
                    .status(HttpStatus.OK.value())
                    .body(responseDto)
                    .message("DB에서 도서 정보 조회 성공")
                    .build());
        } else {
            // API를 통해 새로 생성된 경우
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<BookResponseDto>builder()
                    .status(HttpStatus.CREATED.value())
                    .body(responseDto)
                    .message("API에서 도서 정보를 가져와 DB에 저장 성공")
                    .build());
        }
    }
}
