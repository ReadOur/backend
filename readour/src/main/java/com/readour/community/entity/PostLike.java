package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post_like")
public class PostLike {
    @EmbeddedId
    private PostLikeId id;
    // @Id private Long postId;
    // @Id private Long userId;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
