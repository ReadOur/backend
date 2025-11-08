package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "book_wishlist")
public class BookWishlist {

    @EmbeddedId
    private BookWishlistId id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}