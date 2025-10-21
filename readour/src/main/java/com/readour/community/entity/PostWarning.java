package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post_warning")
@IdClass(PostWarningId.class)
public class PostWarning {
    @Id private Long postId;
    @Id private String warning;
}
