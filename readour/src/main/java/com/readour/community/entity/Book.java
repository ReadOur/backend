package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
