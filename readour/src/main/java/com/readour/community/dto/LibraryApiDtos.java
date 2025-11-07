package com.readour.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class LibraryApiDtos {

    // --- API #16 (도서 검색) DTO ---

    @Getter @Setter @NoArgsConstructor
    public static class SearchResponseWrapper {
        private SearchResponse response;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SearchResponse {
        private int numFound;

        private List<SearchDocWrapper> docs;
    }

    // SearchDoc을 감싸는 래퍼 클래스
    @Getter @Setter @NoArgsConstructor
    public static class SearchDocWrapper {
        private SearchDoc doc;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SearchDoc {
        @JsonProperty("bookname")
        private String bookname;
        @JsonProperty("authors")
        private String authors;
        @JsonProperty("publisher")
        private String publisher;
        @JsonProperty("publication_year")
        private String publicationYear;
        @JsonProperty("isbn13")
        private String isbn13;
        @JsonProperty("bookImageURL")
        private String bookImageURL;
    }

    // --- API #6 (도서 상세 조회) DTO ---
    @Getter @Setter @NoArgsConstructor
    public static class DetailResponseWrapper {
        private DetailResponse response;
    }
    @Getter @Setter @NoArgsConstructor
    public static class DetailResponse {
        private DetailResult detail;
    }
    @Getter @Setter @NoArgsConstructor
    public static class DetailResult {
        private BookInfo book;
    }
    @Getter @Setter @NoArgsConstructor
    public static class BookInfo {
        @JsonProperty("bookname")
        private String bookname;
        @JsonProperty("authors")
        private String authors;
        @JsonProperty("publisher")
        private String publisher;
        @JsonProperty("publication_year")
        private String publicationYear;
        @JsonProperty("isbn")
        private String isbn;
        @JsonProperty("isbn13")
        private String isbn13;
        @JsonProperty("description")
        private String description;
        @JsonProperty("bookImageURL")
        private String bookImageURL;
        @JsonProperty("class_no")
        private String classNo;
        @JsonProperty("class_nm")
        private String classNm;
        @JsonProperty("vol")
        private String vol;
        @JsonProperty("addition_symbol")
        private String additionSymbol;
    }


    // --- API #3 (인기대출도서 조회) DTO ---
    @Getter @Setter @NoArgsConstructor
    public static class PopularBookResponseWrapper {
        private PopularBookResponse response;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PopularBookResponse {
        private int numFound;

        // List<PopularBookDoc> -> List<PopularBookDocWrapper>
        // API #16과 동일한 {"docs": [ {"doc": {...}} ]} 구조를 가정합니다.
        private List<PopularBookDocWrapper> docs;
    }

    // PopularBookDoc을 감싸는 래퍼 클래스
    @Getter @Setter @NoArgsConstructor
    public static class PopularBookDocWrapper {
        private PopularBookDoc doc;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PopularBookDoc {
        @JsonProperty("bookname")
        private String bookname;
        @JsonProperty("authors")
        private String authors;
        @JsonProperty("publisher")
        private String publisher;
        @JsonProperty("publication_year")
        private String publicationYear;
        @JsonProperty("isbn13")
        private String isbn13;
        @JsonProperty("bookImageURL")
        private String bookImageURL;
        @JsonProperty("loan_count")
        private int loanCount;
    }

    // API #11 (도서 소장/대출 여부) DTO
    @Getter @Setter @NoArgsConstructor
    public static class BookExistResponseWrapper {
        private BookExistResponse response;
    }

    @Getter @Setter @NoArgsConstructor
    public static class BookExistResponse {
        private BookExistResult result;
    }

    @Getter @Setter @NoArgsConstructor
    public static class BookExistResult {
        @JsonProperty("hasBook")
        private String hasBook; // "Y" or "N"

        @JsonProperty("loanAvailable")
        private String loanAvailable; // "Y" or "N"
    }

    // API #1 (정보공개 도서관 조회) DTO

    @Getter @Setter @NoArgsConstructor
    public static class LibSearchResponseWrapper {
        private LibSearchResponse response;
    }

    @Getter @Setter @NoArgsConstructor
    public static class LibSearchResponse {
        @JsonProperty("numFound")
        private int numFound;

        @JsonProperty("libs")
        private List<LibWrapper> libs;
    }

    @Getter @Setter @NoArgsConstructor
    public static class LibWrapper {
        @JsonProperty("lib")
        private LibInfo lib;
    }

    @Getter @Setter @NoArgsConstructor
    public static class LibInfo {
        @JsonProperty("libCode")
        private String libCode;

        @JsonProperty("libName")
        private String libName;

        @JsonProperty("address")
        private String address;

        @JsonProperty("homepage")
        private String homepage;
    }
}

