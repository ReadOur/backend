package com.readour.community.dto;

import com.readour.community.entity.Book;
import com.readour.community.entity.BookReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 서재 - 작성한 리뷰 DTO")
public class MyReviewDto {

    @Schema(description = "리뷰 ID")
    private Long reviewId;

    @Schema(description = "도서 ID")
    private Long bookId;

    @Schema(description = "도서명")
    private String bookname;

    @Schema(description = "책 표지 이미지 URL")
    private String bookImageUrl;

    @Schema(description = "부여한 평점")
    private Integer rating;

    @Schema(description = "리뷰 내용")
    private String content;

    @Schema(description = "리뷰 작성 시각")
    private LocalDateTime createdAt;

    public static MyReviewDto fromEntities(BookReview review, Book book) {
        return MyReviewDto.builder()
                .reviewId(review.getReviewId())
                .bookId(review.getBookId())
                .bookname(book != null ? book.getBookname() : "삭제되거나 찾을 수 없는 책")
                .bookImageUrl(book != null ? book.getBookImageUrl() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}