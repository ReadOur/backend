package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "comment_like")
@IdClass(CommentLikeId.class)
public class CommentLike {
    @Id private Long commentId;
    @Id private Long userId;
    private LocalDateTime createdAt;
}
