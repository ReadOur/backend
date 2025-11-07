package com.readour.community.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.community.dto.*;
import com.readour.common.entity.Book;
import com.readour.community.service.BookService;
import com.readour.community.dto.LibraryAvailabilityDto;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // (SD-26: 도서 검색)
    @Operation(summary = "도서 검색 (외부 API)",
            description = "정보나루 API(#16)를 호출하여 키워드로 도서를 검색합니다. (DB 저장 X)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 검색 성공",
                    content = @Content(schema = @Schema(implementation = void.class)))
    })
    @GetMapping("/books/search")
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


    @PostMapping("/books/sync")
    public ResponseEntity<ApiResponseDto<Book>> syncBook(@RequestBody Map<String, String> payload) {
        String isbn = payload.get("isbn");
        if (isbn == null || isbn.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "ISBN is required");
        }

        // DB에 이미 있는지 확인 (findOrCreateBookByIsbn이 이 로직을 포함함)
        boolean existsInDb = bookService.isBookInDb(isbn);

        Book book = bookService.findOrCreateBookByIsbn(isbn);

        if (existsInDb) {
            // 이미 DB에 있었던 경우
            return ResponseEntity.ok(ApiResponseDto.<Book>builder()
                    .status(HttpStatus.OK.value())
                    .body(book)
                    .message("DB에서 도서 정보 조회 성공")
                    .build());
        } else {
            // API를 통해 새로 생성된 경우
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<Book>builder()
                    .status(HttpStatus.CREATED.value())
                    .body(book)
                    .message("API에서 도서 정보를 가져와 DB에 저장 성공")
                    .build());
        }
    }

    // (SD-27) 책 리뷰 작성
    @Operation(summary = "책 리뷰 작성", description = "특정 책(bookId)에 대한 리뷰를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "404", description = "책 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "리뷰 중복 작성 (사용자당 책 1개)")
    })
    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<ApiResponseDto<BookReviewResponseDto>> addBookReview(
            @PathVariable Long bookId,
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody BookReviewCreateRequestDto requestDto
    ) {
        BookReviewResponseDto review = bookService.addBookReview(bookId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<BookReviewResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(review)
                .message("리뷰가 성공적으로 작성되었습니다.")
                .build());
    }

    //  (SD-27) 책 리뷰 조회
    @Operation(summary = "책 리뷰 조회", description = "특정 책(bookId)의 모든 리뷰를 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음")
    })
    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<ApiResponseDto<Page<BookReviewResponseDto>>> getBookReviews(
            @PathVariable Long bookId,
            @ParameterObject Pageable pageable
    ) {
        Page<BookReviewResponseDto> reviewPage = bookService.getBookReviews(bookId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<BookReviewResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .body(reviewPage)
                .message("리뷰 목록 조회 성공")
                .build());
    }

    // (SD-28) 책 리뷰 수정
    @Operation(summary = "책 리뷰 수정", description = "자신이 작성한 리뷰(reviewId)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자 아님)"),
            @ApiResponse(responseCode = "404", description = "리뷰 또는 사용자를 찾을 수 없음")
    })
    @PutMapping("/books/reviews/{reviewId}")
    public ResponseEntity<ApiResponseDto<BookReviewResponseDto>> updateBookReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody BookReviewUpdateRequestDto requestDto
    ) {
        BookReviewResponseDto review = bookService.updateBookReview(reviewId, userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.<BookReviewResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(review)
                .message("리뷰가 성공적으로 수정되었습니다.")
                .build());
    }

    // (SD-29) 책 리뷰 삭제
    @Operation(summary = "책 리뷰 삭제", description = "자신이 작성한 리뷰(reviewId)를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자 아님)")
    })
    @DeleteMapping("/books/reviews/{reviewId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBookReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
        bookService.deleteBookReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("리뷰가 성공적으로 삭제되었습니다.")
                .build());
    }

    // (SD-31) 책 하이라이트 작성
    @Operation(summary = "책 하이라이트 작성", description = "특정 책(bookId)에 대한 하이라이트(인용구)를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "하이라이트 작성 성공"),
            @ApiResponse(responseCode = "404", description = "책 또는 사용자를 찾을 수 없음")
    })
    @PostMapping("/books/{bookId}/highlights")
    public ResponseEntity<ApiResponseDto<BookHighlightResponseDto>> addBookHighlight(
            @PathVariable Long bookId,
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody BookHighlightCreateRequestDto requestDto
    ) {
        BookHighlightResponseDto highlight = bookService.addBookHighlight(bookId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<BookHighlightResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(highlight)
                .message("하이라이트가 성공적으로 작성되었습니다.")
                .build());
    }

    // (SD-31) 책 하이라이트 조회
    @Operation(summary = "책 하이라이트 조회", description = "특정 책(bookId)의 모든 하이라이트를 오래된순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하이라이트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음")
    })
    @GetMapping("/books/{bookId}/highlights")
    public ResponseEntity<ApiResponseDto<Page<BookHighlightResponseDto>>> getBookHighlights(
            @PathVariable Long bookId,
            @ParameterObject Pageable pageable
    ) {
        Page<BookHighlightResponseDto> highlightPage = bookService.getBookHighlights(bookId, pageable);
        return ResponseEntity.ok(ApiResponseDto.<Page<BookHighlightResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .body(highlightPage)
                .message("하이라이트 목록 조회 성공")
                .build());
    }

    // (SD-32) 책 하이라이트 수정
    @Operation(summary = "책 하이라이트 수정", description = "자신이 작성한 하이라이트(highlightId)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하이라이트 수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자 아님)"),
            @ApiResponse(responseCode = "404", description = "하이라이트 또는 사용자를 찾을 수 없음")
    })
    @PutMapping("/books/highlights/{highlightId}")
    public ResponseEntity<ApiResponseDto<BookHighlightResponseDto>> updateBookHighlight(
            @PathVariable Long highlightId,
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody BookHighlightUpdateRequestDto requestDto
    ) {
        BookHighlightResponseDto highlight = bookService.updateBookHighlight(highlightId, userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.<BookHighlightResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(highlight)
                .message("하이라이트가 성공적으로 수정되었습니다.")
                .build());
    }

    // (SD-33) 책 하이라이트 삭제
    @Operation(summary = "책 하이라이트 삭제", description = "자신이 작성한 하이라이트(highlightId)를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하이라이트 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자 아님)")
    })
    @DeleteMapping("/books/highlights/{highlightId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBookHighlight(
            @PathVariable Long highlightId,
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
        bookService.deleteBookHighlight(highlightId, userId);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("하이라이트가 성공적으로 삭제되었습니다.")
                .build());
    }

    // (SD-34) 선호 도서관 대출 가능 여부 조회
    @Operation(summary = "선호 도서관 대출 가능 여부 조회",
            description = "특정 책(isbn13)에 대해, 사용자가 선호 등록한 모든 도서관의 소장/대출 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대출 가능 여부 조회 성공"),
            @ApiResponse(responseCode = "400", description = "ISBN이 누락됨")
    })
    @GetMapping("/books/availability")
    public ResponseEntity<ApiResponseDto<List<LibraryAvailabilityDto>>> getBookAvailability(
            @RequestParam("isbn13") String isbn13,
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
        List<LibraryAvailabilityDto> availabilityList = bookService.checkBookAvailability(userId, isbn13);

        return ResponseEntity.ok(ApiResponseDto.<List<LibraryAvailabilityDto>>builder()
                .status(HttpStatus.OK.value())
                .body(availabilityList)
                .message("도서 대출 가능 여부 조회 성공")
                .build());
    }
}