package com.readour.community.dto;

import com.readour.community.entity.Book;
import com.readour.community.entity.BookHighlight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 서재 - 작성한 하이라이트 DTO")
public class MyHighlightDto {

    @Schema(description = "하이라이트 ID")
    private Long highlightId;

    @Schema(description = "도서 ID")
    private Long bookId;

    @Schema(description = "도서명")
    private String bookname;

    @Schema(description = "책 표지 이미지 URL")
    private String bookImageUrl;

    @Schema(description = "하이라이트 내용 (인용구)")
    private String content;

    @Schema(description = "페이지 번호")
    private Integer pageNumber;

    @Schema(description = "하이라이트 작성 시각")
    private LocalDateTime createdAt;

    public static MyHighlightDto fromEntities(BookHighlight highlight, Book book) {
        return MyHighlightDto.builder()
                .highlightId(highlight.getHighlightId())
                .bookId(highlight.getBookId())
                .bookname(book != null ? book.getBookname() : "삭제되거나 찾을 수 없는 책")
                .bookImageUrl(book != null ? book.getBookImageUrl() : null)
                .content(highlight.getContent())
                .pageNumber(highlight.getPageNumber())
                .createdAt(highlight.getCreatedAt())
                .build();
    }
}