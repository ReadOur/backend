package com.readour.community.dto;

// 이 파일에서 PopularBookDoc에 대한 import 구문은 제거합니다.
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
public class PopularBookDto {
    private String bookname;
    private String authors;
    private String publisher;
    private String publicationYear;
    private String isbn13;
    private String bookImageURL;
    private int loanCount;

    /**
     * 정보나루 API 응답 객체(PopularBookDoc)를 DTO로 변환합니다.
     */
    // ✅ [수정] 파라미터 타입을 'LibraryApiDtos.PopularBookDoc'로 전체 경로 지정
    public static PopularBookDto from(LibraryApiDtos.PopularBookDoc doc) {
        return PopularBookDto.builder()
                .bookname(doc.getBookname())
                .authors(doc.getAuthors())
                .publisher(doc.getPublisher())
                .publicationYear(doc.getPublicationYear())
                .isbn13(doc.getIsbn13())
                .bookImageURL(doc.getBookImageURL())
                .loanCount(doc.getLoanCount())
                .build();
    }
}

