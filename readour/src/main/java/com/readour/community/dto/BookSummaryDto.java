package com.readour.community.dto;

import com.readour.community.dto.LibraryApiDtos.SearchDoc;
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
    private String bookname;
    private String authors;
    private String publisher;
    private String publicationYear;
    private String isbn13;
    private String bookImageURL;

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
}
