package com.readour.community.dto;

import com.readour.community.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 서재 - 위시리스트 항목 DTO")
public class MyWishlistDto {

    @Schema(description = "DB에 저장된 도서 ID")
    private Long bookId;

    @Schema(description = "도서명")
    private String bookname;

    @Schema(description = "저자명")
    private String authors;

    @Schema(description = "책 표지 이미지 URL")
    private String bookImageUrl;

    public static MyWishlistDto fromEntity(Book book) {
        return MyWishlistDto.builder()
                .bookId(book.getBookId())
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .bookImageUrl(book.getBookImageUrl())
                .build();
    }
}