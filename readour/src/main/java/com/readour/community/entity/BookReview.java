package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "book_review",
        uniqueConstraints = @UniqueConstraint(name = "uq_book_user",
                columnNames = {"book_id","user_id"}))
public class BookReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    private Long bookId;
    private Long userId;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
