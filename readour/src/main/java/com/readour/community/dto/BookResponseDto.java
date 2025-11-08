package com.readour.community.dto;

import com.readour.community.entity.Book;
import com.readour.community.dto.AverageRatingProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도서 정보 응답 DTO (DB 저장 후 반환)")
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

    @Schema(description = "평균 평점 (리뷰가 없으면 null)", example = "4.5")
    private Double averageRating;

    @Schema(description = "리뷰 수", example = "15")
    private Long reviewCount;

    @Schema(description = "현재 사용자가 위시리스트에 추가했는지 여부", example = "true")
    private Boolean isWishlisted;

    public static BookResponseDto fromEntity(Book book, AverageRatingProjection ratingInfo, boolean isWishlisted) {
        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .isbn13(book.getIsbn13())
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .description(book.getDescription())
                .bookImageUrl(book.getBookImageUrl())
                .averageRating(ratingInfo != null ? ratingInfo.getAverageRating() : null)
                .reviewCount(ratingInfo != null ? ratingInfo.getReviewCount() : 0L)
                .isWishlisted(isWishlisted)
                .build();
    }
}