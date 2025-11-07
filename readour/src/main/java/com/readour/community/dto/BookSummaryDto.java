package com.readour.community.dto;

import com.readour.community.dto.LibraryApiDtos.SearchDoc;
import com.readour.community.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDto {
    private Long bookId;
    private String bookname;
    private String authors;
    private String publisher;
    private String publicationYear;
    private String isbn13;
    private String bookImageURL;
    private Double averageRating;
    private Long reviewCount;

    /**
     * 정보나루 API 응답 객체(SearchDoc)를 BookSummaryDto로 변환합니다.
     */
    public static BookSummaryDto from(SearchDoc doc) {
        return BookSummaryDto.builder()
                .bookname(doc.getBookname())
                .authors(doc.getAuthors())
                .publisher(doc.getPublisher())
                .publicationYear(doc.getPublicationYear())
                .isbn13(doc.getIsbn13())
                .bookImageURL(doc.getBookImageURL())
                .build();
    }

    /**
     * API DTO와 DB Entity/Projection을 조합하여 DTO 생성 (3개 인자)
     * (BookService 148번째 줄에서 이 메서드를 호출하려 하고 있습니다)
     */
    public static BookSummaryDto from(SearchDoc doc, Book existingBook, AverageRatingProjection ratingInfo) {
        return BookSummaryDto.builder()
                .bookId(existingBook != null ? existingBook.getBookId() : null)
                .bookname(doc.getBookname())
                .authors(doc.getAuthors())
                .publisher(doc.getPublisher())
                .publicationYear(doc.getPublicationYear())
                .isbn13(doc.getIsbn13())
                .bookImageURL(doc.getBookImageURL())
                .averageRating(ratingInfo != null ? ratingInfo.getAverageRating() : null)
                .reviewCount(ratingInfo != null ? ratingInfo.getReviewCount() : 0L)
                .build();
    }
}
