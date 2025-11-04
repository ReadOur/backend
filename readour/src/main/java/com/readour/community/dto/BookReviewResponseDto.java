package com.readour.community.dto;

import com.readour.common.entity.User;
import com.readour.community.entity.BookReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "책 리뷰 응답 DTO")
public class BookReviewResponseDto {

    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "책 ID", example = "101")
    private Long bookId;

    @Schema(description = "작성자 ID", example = "201")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "독서광")
    private String authorNickname;

    @Schema(description = "리뷰 내용", example = "정말 감명깊은 책입니다.")
    private String content;

    @Schema(description = "평점", example = "5")
    private Integer rating;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static BookReviewResponseDto fromEntity(BookReview review, User author) {
        return BookReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .bookId(review.getBookId())
                .authorId(review.getUserId())
                .authorNickname(author != null ? author.getNickname() : "알 수 없음")
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}