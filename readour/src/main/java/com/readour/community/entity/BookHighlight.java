package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "book_highlight")
public class BookHighlight {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long highlightId;
    private Long bookId;
    private Long userId;
    @Lob private String content;
    private Integer pageNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
