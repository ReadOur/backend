package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post_like")
@IdClass(PostLikeId.class)
public class PostLike {
    @Id private Long postId;
    @Id private Long userId;
    private LocalDateTime createdAt;
}
