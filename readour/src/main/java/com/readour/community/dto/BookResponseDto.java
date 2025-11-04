package com.readour.community.dto;

import com.readour.common.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도서 정보 응답 DTO")
public class BookResponseDto {

    @Schema(description = "DB에 저장된 도서 ID", example = "1")
    private Long bookId;

    @Schema(description = "13자리 ISBN", example = "9788936434267")
    private String isbn13;

    @Schema(description = "도서명", example = "아몬드")
    private String bookname;

    @Schema(description = "저자명", example = "손원평 (지은이)")
    private String authors;

    @Schema(description = "출판사", example = "창비")
    private String publisher;

    @Schema(description = "출판년도", example = "2017")
    private Integer publicationYear;

    @Schema(description = "책 소개", example = "제10회 창비청소년문학상을 수상하며...")
    private String description;

    @Schema(description = "책 표지 이미지 URL", example = "http://image.aladin.co.kr/...")
    private String bookImageUrl;

    public static BookResponseDto fromEntity(Book book) {
        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .isbn13(book.getIsbn13())
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .description(book.getDescription())
                .bookImageUrl(book.getBookImageUrl())
                .build();
    }
}