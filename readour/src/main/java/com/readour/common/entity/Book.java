package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "book")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String isbn10;
    private String isbn13;
    private String additionSymbol;
    private String vol;
    private String bookname;
    private String authors;
    private String publisher;
    private LocalDate publicationDate;
    private Integer publicationYear;
    private String classNo;
    private String classNm;
    @Lob private String description;
    @Lob private String bookImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
