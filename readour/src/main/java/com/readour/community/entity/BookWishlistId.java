package com.readour.community.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class BookWishlistId implements Serializable {
    private Long userId;
    private Long bookId;
}